package com.jmist.demo;


import ca.eandb.jmist.framework.*;
import ca.eandb.jmist.framework.color.*;
import ca.eandb.jmist.framework.geometry.ConstructiveSolidGeometry;
import ca.eandb.jmist.framework.geometry.SubtractionGeometry;
import ca.eandb.jmist.framework.geometry.primitive.BoxGeometry;
import ca.eandb.jmist.framework.geometry.primitive.SphereGeometry;
import ca.eandb.jmist.framework.geometry.primitive.SpheroidGeometry;
import ca.eandb.jmist.framework.lens.PinholeLens;
import ca.eandb.jmist.framework.lens.TransformableLens;
import ca.eandb.jmist.framework.material.LambertianMaterial;
import ca.eandb.jmist.framework.material.MirrorMaterial;
import ca.eandb.jmist.framework.scene.*;
import ca.eandb.jmist.math.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Main {

    static String BASEDIR = System.getProperty("user.dir");

    static Rasterer r = new Rasterer(BASEDIR);

    public static void main(String[] args) throws IOException {
        if (args.length >= 2 && args[1].equals("hide")) {
	    r.showProgress = false;
	    r.showImage = false;
	}	
	    for (String s : args){
		System.out.println(s);
	}
	switch (Integer.parseInt(args[0])) {
            case 1:
                shapeTest();
                break;
            case 2:
                CSGdemo();
                break;
            case 3:
                highResScene();
                break;
            case 4:
                animateGif();
                break;
        }
    }

    /**
     * Renders a reflective spheroid
     */
    public static void shapeTest() throws IOException {
        final BranchSceneElement sceneRoot = new BranchSceneElement();

        final Spheroid spheroid = new Spheroid(100,200);
        TransformableSceneElement spheroidSE = new TransformableSceneElement(new SpheroidGeometry(spheroid));

        spheroidSE.rotateX(Math.PI/4.0);
        spheroidSE.rotateZ(Math.PI/6.0);
        spheroidSE.translate(new Vector3(250,250,250));


        MaterialSceneElement myshape = new MaterialSceneElement(new MirrorMaterial(r.cm.getWhite()), spheroidSE);
        sceneRoot.addChild(myshape);

        final Scene cornellBoxScene = new EmptyCornellBoxScene(r.cm);
        sceneRoot.addChild(cornellBoxScene.getRoot());


        r.scene = new AbstractScene() {
            public Light getLight() {
                return cornellBoxScene.getLight();
            }

            public SceneElement getRoot() {
                return sceneRoot;
            }

            public Lens getLens() {
                return cornellBoxScene.getLens();
            }
        };
        r.samplesPerPixel = 5;
        r.firstBounceRays = 2;
        r.render();
    }

    /**
     * Demonstrates Constructive Solid Geometry
     */
    public static void CSGdemo() throws IOException {
        final BranchSceneElement sceneRoot = new BranchSceneElement();

        SceneElement sphere1 = new SphereGeometry(new Point3(200,250,250), 150);
        ConstructiveSolidGeometry inter = new SubtractionGeometry();
        inter.addChild(sphere1);

        SceneElement sphere2 = new SphereGeometry(new Point3(250,250,200), 150);
        inter.addChild(sphere2);


        LambertianMaterial myMat = new LambertianMaterial(r.cm.fromRGB(0.5,0.5,1));
        sceneRoot.addChild(new MaterialSceneElement(myMat, inter));


        final Scene cornellBoxScene = new EmptyCornellBoxScene(r.cm);
        sceneRoot.addChild(cornellBoxScene.getRoot());

        r.scene = new AbstractScene() {
            public Light getLight() {
                return cornellBoxScene.getLight();
            }

            public SceneElement getRoot() {
                return sceneRoot;
            }

            public Lens getLens() {
                return cornellBoxScene.getLens();
            }
        };
        r.render();
    }

    /**
     * A Cornell Box scene run to convergence
     */
    public static void highResScene() throws IOException {
        r.scene = new CornellBoxScene(r.cm);
        r.samplesPerPixel = 500;
        r.maxDepth = 20;
        r.firstBounceRays = 30;
        r.width = 512;
        r.height = 512;
        r.render();
    }

    /**
     * A simple animation with a rotating box and moving camera
     */
    public static void animateGif() throws IOException {
        StringBuilder outRend = new StringBuilder();

        String startTime = now();

        outRend.append(BASEDIR + "/Renders/");
        outRend.append("Anim-");
        outRend.append(startTime);
        outRend.append("-test2.gif");
        ImageOutputStream output =
                new FileImageOutputStream(new File(outRend.toString()));
        GifSequenceWriter writer = null;

        r.width = 128;
        r.height = 128;
        r.showProgress = false;
        r.showImage =false;
        float fps = 20;
        int len = 40;
        for (int i = 0; i < len; i++) {
            System.out.println("Started " + i + " out of " + len);
            final Scene cornellBoxScene = new CornellBoxScene(r.cm);
            final BranchSceneElement sceneRoot = new BranchSceneElement();


            TransformableSceneElement myBox = new TransformableSceneElement(new BoxGeometry(Box3.UNIT));
            myBox.scale(100.0);

            myBox.rotateY(Math.PI / 8.0);
            myBox.rotateX(Math.PI / 8.0);
            myBox.rotateY(i *2.0 * Math.PI / (float)len);

            myBox.translate(new Vector3(100.0, 300.0, 100.0));


            Material myMat = new MirrorMaterial(RGB.WHITE);
            MaterialSceneElement myBox2 = new MaterialSceneElement(myMat, myBox);

            sceneRoot.addChild(myBox2);
            sceneRoot.addChild(cornellBoxScene.getRoot());

            final TransformableLens lens = new TransformableLens(PinholeLens.fromHfovAndAspect(Math.PI / 2, 1.0));

            lens.rotateY(Math.PI);
            double Xmove = Math.sin((double)i *2.0 * Math.PI / (double)len) * 100.0;
            double Zmove = Math.cos((double)i *2.0 * Math.PI / (double)len) * 80.0;

            lens.translate(new Vector3(278.0 + Xmove, 273.0, -100.0 + Zmove));

            Scene scene = new AbstractScene() {

                public Light getLight() {
                    return cornellBoxScene.getLight();
                }

                public SceneElement getRoot() {
                    return sceneRoot;
                }

                public Lens getLens() {
                    return lens;
                }
            };

            StringBuilder fileName = new StringBuilder();

            fileName.append(BASEDIR + "/Renders/SubAnim/");
            fileName.append("Anim-");
            fileName.append(startTime);
            fileName.append("-"+i);
            fileName.append("-test.png");

            r.fileName = fileName.toString();
            r.scene = scene;
            r.render();

            BufferedImage nextImage = ImageIO.read(new File(fileName.toString()));

            if (writer == null) {
                 writer = new GifSequenceWriter(output, nextImage.getType(), (int)(1000.0/fps), true);
            }
            writer.writeToSequence(nextImage);

        }
        writer.close();
        output.close();
    }

    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd-HH-mm-ss";

    public static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }


}
