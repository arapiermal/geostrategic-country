package com.erimali.cntrygame;


import javafx.animation.*;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GlobeViewer extends Stage {
    private double GLOBE_RADIUS[] = {200, 100, 280};
    private double VIEWPORT_SIZE = 600;
    private double[] ROTATE_SECS = {30, 360, 30};

    private double MAP_WIDTH = 2048 / 2d;
    private double MAP_HEIGHT = 1024 / 2d;
    private String[] GLOBE_NAMES = {"Earth", "Moon", "Sun"};
    private String[] DIFFUSE_MAP = {getResourceLoc("2k_earth_daymap.jpg"),
            getResourceLoc("2k_moon.jpg"),
            getResourceLoc("2k_sun.jpg")};
    private String[] NORMAL_MAP = {getResourceLoc("2k_earth_normal_map.jpg")};
    private String[] SPECULAR_MAP = {getResourceLoc("2k_earth_specular_map.jpg")};

    private String getResourceLoc(String im) {
        return GLogic.RESOURCESPATH + "map/" + im;
    }

    private Group buildScene(int type) {
        Sphere globe = new Sphere(GLOBE_RADIUS[type]);
        globe.setTranslateX(VIEWPORT_SIZE / 2d);
        globe.setTranslateY(VIEWPORT_SIZE / 2d);

        PhongMaterial globeMaterial = new PhongMaterial();
        try {
            globeMaterial.setDiffuseMap(new Image("file:" + DIFFUSE_MAP[type], MAP_WIDTH, MAP_HEIGHT, true, true));
        } catch (Exception e) {

        }
        try {
            globeMaterial.setBumpMap(new Image("file:" + NORMAL_MAP[type], MAP_WIDTH, MAP_HEIGHT, true, true));
        } catch (Exception e) {
        }
        try {
            globeMaterial.setSpecularMap(new Image("file:" + SPECULAR_MAP[type], MAP_WIDTH, MAP_HEIGHT, true, true));
        } catch (Exception e) {

        }
        globe.setMaterial(globeMaterial);

        return new Group(globe);
    }

    public GlobeViewer(int type) {
        Group group = buildScene(type);
        StackPane stackPane = new StackPane(group);
        Image backgroundImage = new Image("file:" + getResourceLoc("2k_stars_milky_way.jpg"));
        BackgroundImage backgroundImg = new BackgroundImage(backgroundImage, null, null, null, null);
        //Background background = new Background(new BackgroundFill(Color.BLACK, null, null));
        Background background = new Background(backgroundImg);
        stackPane.setBackground(background);

        Scene scene = new Scene(stackPane, VIEWPORT_SIZE, VIEWPORT_SIZE, true, SceneAntialiasing.BALANCED);
        scene.setCamera(new PerspectiveCamera());
        setScene(scene);
        setTitle(GLOBE_NAMES[type]);
        rotateAroundYAxis(group, type).play();
    }

    private RotateTransition rotateAroundYAxis(Node node, int type) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(ROTATE_SECS[type]), node);
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }

}