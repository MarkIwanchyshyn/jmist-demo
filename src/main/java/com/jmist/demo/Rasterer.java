package com.jmist.demo;

import ca.eandb.jdcp.job.ParallelizableJob;
import ca.eandb.jdcp.job.ParallelizableJobRunner;
import ca.eandb.jmist.framework.*;
import ca.eandb.jmist.framework.color.CIEXYZ;
import ca.eandb.jmist.framework.color.ColorModel;
import ca.eandb.jmist.framework.color.RGB;
import ca.eandb.jmist.framework.color.Spectrum;
import ca.eandb.jmist.framework.color.rgb.RGBColorModel;
import ca.eandb.jmist.framework.display.CompositeDisplay;
import ca.eandb.jmist.framework.display.ImageFileDisplay;
import ca.eandb.jmist.framework.display.JComponentDisplay;
import ca.eandb.jmist.framework.job.RasterJob;
import ca.eandb.jmist.framework.modifier.ShaderModifier;
import ca.eandb.jmist.framework.random.NRooksRandom;
import ca.eandb.jmist.framework.random.ThreadLocalRandom;
import ca.eandb.jmist.framework.scene.EmptyScene;
import ca.eandb.jmist.framework.scene.ModifierSceneElement;
import ca.eandb.jmist.framework.shader.EmissionShader;
import ca.eandb.jmist.framework.shader.PathTracingShader;
import ca.eandb.jmist.framework.shader.StandardCompositeShader;
import ca.eandb.jmist.framework.shader.image.CameraImageShader;
import ca.eandb.jmist.framework.shader.pixel.AveragingPixelShader;
import ca.eandb.jmist.framework.shader.pixel.RandomPixelShader;
import ca.eandb.jmist.framework.shader.ray.SceneRayShader;
import ca.eandb.jmist.framework.shader.ray.UniformRayShader;
import ca.eandb.jmist.framework.tone.ConstantToneMapperFactory;
import ca.eandb.jmist.framework.tone.LinearToneMapper;
import ca.eandb.jmist.framework.tone.ToneMapperFactory;
import ca.eandb.util.progress.ProgressPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class Rasterer {
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd-HH-mm-ss";

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }
    public String BASEDIR;

    public Scene scene;
    public boolean showProgress;
    public boolean showImage;
    public int samplesPerPixel;
    public int maxDepth;
    public int firstBounceRays;
    public int width;
    public int height;
    public boolean closeOnFinish;
    public String renderName;
    public Spectrum backgroundCol;
    public CIEXYZ whitest;
    public int threads;
    ColorModel cm;
    String fileName;

    public Rasterer(String BASEDIR){
        this.BASEDIR = BASEDIR;
        scene = EmptyScene.INSTANCE;
        showProgress = true;
        showImage = true;
        samplesPerPixel = 10;
        maxDepth = 10;
        firstBounceRays = 5;
        width = 256;
        height = width;
        closeOnFinish = true;
        renderName = "render";
        backgroundCol = RGB.WHITE;
        whitest = CIEXYZ.fromRGB(1, 1, 1);
        threads = Runtime.getRuntime().availableProcessors();
        cm = RGBColorModel.getInstance();

        StringBuilder fName = new StringBuilder();

        fName.append(BASEDIR + "/Renders/");
        fName.append("render-");
        fName.append(now());
        fName.append("-test.png");

        fileName = fName.toString();

    }

    public void render() throws IOException {
        // kinda nasty, but it works
        new File(new File(fileName).getParent()).mkdirs();

        Random random = new ThreadLocalRandom(new NRooksRandom(4000, 2));
        Shader shader = new StandardCompositeShader()
                .addShader(new EmissionShader())
                .addShader(new PathTracingShader(maxDepth, firstBounceRays));
        SceneElement root = new ModifierSceneElement(new ShaderModifier(shader), scene.getRoot());

        RayShader background = new UniformRayShader(backgroundCol);
        RayShader rayShader = new SceneRayShader(root, scene.getLight(), background);

        ToneMapperFactory toneMapperFactory = new ConstantToneMapperFactory(new LinearToneMapper(whitest));


        ImageShader imageShader = new CameraImageShader(scene.getLens(), rayShader);
        PixelShader pixelShader = new AveragingPixelShader(samplesPerPixel, new RandomPixelShader(random, imageShader, cm));


        RasterJob job = null;
        JFrame displayFrame = null;

        RasterJob.Builder rasterBuilder = (RasterJob.Builder) RasterJob.newBuilder();
        rasterBuilder.setColorModel(cm);
        rasterBuilder.setImageSize(width, height);
        rasterBuilder.setTileSize(4,4);
        rasterBuilder.setPixelShader(pixelShader);


        if (showImage) {


            Display componentDisplay = new JComponentDisplay(toneMapperFactory);
            Display display = new CompositeDisplay().addDisplay(componentDisplay).addDisplay(new ImageFileDisplay(fileName, toneMapperFactory));
            rasterBuilder.setDisplay(display);

            displayFrame = new JFrame();
            JScrollPane scroll = new JScrollPane((JComponent) componentDisplay);
            displayFrame.add(scroll, BorderLayout.CENTER);
            displayFrame.pack();
            displayFrame.setSize(400, 300);
            displayFrame.setVisible(true);
        } else {
            Display display = new ImageFileDisplay(fileName, toneMapperFactory);
            rasterBuilder.setDisplay(display);
        }
        job = rasterBuilder.build();
        runJob(job);

        if (showImage && closeOnFinish) {
            displayFrame.setVisible(false);
            displayFrame.dispose();

        }

    }

    public void runJob(ParallelizableJob job) {
        UUID id = UUID.randomUUID();
        File dir = new File(BASEDIR, "Working/" + id.toString());
        runJob(job, dir);
    }

    public void runJob(ParallelizableJob job, File dir) {
        dir.mkdirs();
        if (showProgress) {

            ProgressPanel panel = new ProgressPanel();
            JFrame frame = new JFrame();
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            Runnable runner = new ParallelizableJobRunner(job, dir, threads, panel, panel.createProgressMonitor("rendering test"));
            runner.run();

            frame.setVisible(false);
            frame.dispose();
        } else {
            Runnable runner = new ParallelizableJobRunner(job, dir, threads);
            runner.run();
        }
    }

}
