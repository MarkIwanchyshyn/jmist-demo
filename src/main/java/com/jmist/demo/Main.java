package com.jmist.demo;


import ca.eandb.jmist.framework.*;
import ca.eandb.jmist.framework.color.*;
import ca.eandb.jmist.framework.geometry.ConstructiveSolidGeometry;
import ca.eandb.jmist.framework.geometry.PrimitiveGeometry;
import ca.eandb.jmist.framework.geometry.SubtractionGeometry;
import ca.eandb.jmist.framework.geometry.primitive.BoxGeometry;
import ca.eandb.jmist.framework.geometry.primitive.SphereGeometry;
import ca.eandb.jmist.framework.lens.PinholeLens;
import ca.eandb.jmist.framework.lens.TransformableLens;
import ca.eandb.jmist.framework.material.LambertianMaterial;
import ca.eandb.jmist.framework.material.MirrorMaterial;
import ca.eandb.jmist.framework.material.OpaqueMaterial;
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


    public static void main(String[] args) throws IOException {
        switch (Integer.parseInt(args[0])) {
            case 1:
                shapeTest();
                break;
            case 2:
                highResScene();
                break;
            case 3:
                animateGif();
                break;

        }
    }


    public static void shapeTest() throws IOException {
        Rasterer r = new Rasterer(BASEDIR);
        final BranchSceneElement sceneRoot = new BranchSceneElement();

        final Spheroid spheroid = new Spheroid(100,200);
        // Ray3 ray = new Ray3(new Point3())
        TransformableSceneElement spheroidSE = new TransformableSceneElement(new PrimitiveGeometry() {
            @Override
            public void intersect(Ray3 ray, IntersectionRecorder recorder) {
                Interval interval =  spheroid.intersect(ray);
                if (!interval.isEmpty()) {
                    Point3 p1 = ray.pointAt(interval.minimum());
                    Intersection iFront = super.newIntersection(ray,interval.minimum(),true).setLocation(p1).setNormal(spheroid.gradient(p1));
                    recorder.record(iFront);
                    Point3 p2 = ray.pointAt(interval.maximum());
                    Intersection iBack = super.newIntersection(ray,interval.maximum(),true).setLocation(p2).setNormal(spheroid.gradient(p2));
                    recorder.record(iBack);
                }
            }
            double maxDim = Math.max(spheroid.a(),spheroid.c());
            Sphere boundSphere = new Sphere(spheroid.center(),maxDim);

            public Sphere boundingSphere() {
                return boundSphere;
            }
            Box3 boundBox = new Box3(   spheroid.center().x() - maxDim, spheroid.center().y() - maxDim, spheroid.center().z() - maxDim,
                                        spheroid.center().x() + maxDim, spheroid.center().y() + maxDim, spheroid.center().z() + maxDim);

            public Box3 boundingBox() {
                return boundBox;
            }

            @Override
            protected Basis3 getBasis(GeometryIntersection x) {
                return Basis3.fromW(x.getNormal(), Basis3.Orientation.RIGHT_HANDED);
            }

            @Override
            protected Vector3 getNormal(GeometryIntersection x) {
                return x.getNormal();
            }

            @Override
            protected Basis3 getShadingBasis(GeometryIntersection x) {
                return Basis3.fromW(x.getNormal(), Basis3.Orientation.RIGHT_HANDED);
            }

            @Override
            protected Vector3 getShadingNormal(GeometryIntersection x) {
                return x.getNormal();
            }
        });


        SceneElement sphere1 = new SphereGeometry(new Point3(200,250,250), 150);
        ConstructiveSolidGeometry inter = new SubtractionGeometry();
        inter.addChild(sphere1);

        SceneElement sphere2 = new SphereGeometry(new Point3(250,250,200), 150);
        inter.addChild(sphere2);

        spheroidSE.rotateX(Math.PI/4.0);
        spheroidSE.rotateZ(Math.PI/6.0);
        spheroidSE.translate(new Vector3(250,250,250));

        LambertianMaterial myMat = new LambertianMaterial(r.cm.fromRGB(0.5,0.5,1));
        MaterialSceneElement myshape = new MaterialSceneElement(new MirrorMaterial(r.cm.getWhite()), spheroidSE);
        //sceneRoot.addChild(myshape);
        final Scene cornellBoxScene = new EmptyCornellBoxScene(r.cm);
        sceneRoot.addChild(cornellBoxScene.getRoot());

        sceneRoot.addChild(new MaterialSceneElement(myMat, inter));
        Scene scene = new Scene() {
            public Light getLight() {
                return cornellBoxScene.getLight();
            }

            public SceneElement getRoot() {
                return sceneRoot;
            }

            public Lens getLens() {
                return cornellBoxScene.getLens();
            }

            public Animator getAnimator() {
                return cornellBoxScene.getAnimator();
            }

            public Sphere boundingSphere() {
                return sceneRoot.boundingSphere();
            }

            public Box3 boundingBox() {
                return sceneRoot.boundingBox();
            }
        };
        r.scene = scene;
        r.samplesPerPixel = 5;
        r.firstBounceRays = 2;
        r.render();
    }

    public static void highResScene() throws IOException {
        Rasterer r = new Rasterer(BASEDIR);
        r.scene = new CornellBoxScene(r.cm);
        r.samplesPerPixel = 500;
        r.maxDepth = 20;
        r.firstBounceRays = 30;
        r.width = 512;
        r.height = 512;
        r.render();
    }

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

        Rasterer rasterer = new Rasterer(BASEDIR);
        rasterer.width = 128;
        rasterer.height = 128;
        //rasterer.firstBounceRays = 1;
        rasterer.showProgress = false;
        rasterer.showImage =false;
        float fps = 20;
        int len = 40;
        for (int i = 0; i < len; i++) {
            System.out.println("Started " + i + " out of " + len);
            final Scene cornellBoxScene = new CornellBoxScene(rasterer.cm);
            final BranchSceneElement sceneRoot = new BranchSceneElement();


            TransformableSceneElement myBox = new TransformableSceneElement(new BoxGeometry(Box3.UNIT));
            myBox.scale(100.0);

            myBox.rotateY(Math.PI / 8.0);
            myBox.rotateX(Math.PI / 8.0);
            myBox.rotateY(i *2.0 * Math.PI / (float)len);

            //myBox.translate(new Vector3(278.0, 273.0, 100.0));
            myBox.translate(new Vector3(100.0, 300.0, 100.0));


            //SPLITSMaterial myMat = new LambertianMaterial(cm.fromRGB(0,0,1));
            //SPLITSMaterial myMat = new MirrorMaterial(RGB.BLACK);
            ca.eandb.jmist.framework.Material myMat = new OpaqueMaterial() {
                @Override
                public ScatteredRay scatter(SurfacePoint x, Vector3 v, boolean adjoint, WavelengthPacket lambda, double ru, double rv, double rj) {
                    //System.out.println("Hit mirror");
                    Ray3 r = new Ray3(x.getPosition(), Optics.reflect(v, x.getShadingNormal()));
                    //Painter p = new
                    return new ScatteredRay(r, RGB.WHITE.sample(lambda), ScatteredRay.Type.SPECULAR, 1.0, false);

                }
            };
            MaterialSceneElement myBox2 = new MaterialSceneElement(myMat, myBox);

            sceneRoot.addChild(myBox2);
            sceneRoot.addChild(cornellBoxScene.getRoot());

            //TransformableLens lens = new TransformableLens( new FisheyeLens());
            final TransformableLens lens = new TransformableLens(PinholeLens.fromHfovAndAspect(Math.PI / 2, 1.0));

            lens.rotateY(Math.PI);
            double Xmove = Math.sin((double)i *2.0 * Math.PI / (double)len) * 100.0;
            double Zmove = Math.cos((double)i *2.0 * Math.PI / (double)len) * 80.0;

            lens.translate(new Vector3(278.0 + Xmove, 273.0, -200.0 + Zmove));

            Scene scene = new Scene() {

                public Light getLight() {
                    return cornellBoxScene.getLight();
                }

                public SceneElement getRoot() {
                    return sceneRoot;
                }

                public Lens getLens() {
                    return lens;
                }

                public Animator getAnimator() {
                    return cornellBoxScene.getAnimator();
                }

                public Sphere boundingSphere() {
                    return sceneRoot.boundingSphere();
                }

                public Box3 boundingBox() {
                    return sceneRoot.boundingBox();
                }
            };

            StringBuilder fileName = new StringBuilder();

            fileName.append(BASEDIR + "/Renders/SubAnim/");
            fileName.append("Anim-");
            fileName.append(startTime);
            fileName.append("-"+i);
            fileName.append("-test.png");

            rasterer.fileName = fileName.toString();
            rasterer.scene = scene;
            rasterer.render();


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
