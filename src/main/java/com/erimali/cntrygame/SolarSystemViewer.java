package com.erimali.cntrygame;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SolarSystemViewer extends Application {

    private final String RES_PATH = "file:" + GLogic.RESOURCES_PATH + "map/";

    private class Planet {
        String name;
        double radius;
        double orbitRadius;
        double selfRotateTime;
        double orbitPeriodDays;
        String texture;
        Sphere sphere;
        double angle = 0;

        public Planet(String name, double radius, double orbitRadius, double selfRotateTime, double orbitPeriodDays, String texture) {
            this.name = name;
            this.radius = radius;
            this.orbitRadius = orbitRadius;
            this.selfRotateTime = selfRotateTime;
            this.orbitPeriodDays = orbitPeriodDays;
            this.texture = texture;
        }
    }

    private final Planet[] planets = {
            new Planet("Sun", 50, 0, 25, 0, "2k_sun.jpg"),
            new Planet("Mercury", 5, 80, 58.6, 88, "2k_mercury.jpg"),
            new Planet("Venus", 8, 110, -243, 225, "2k_venus_surface.jpg"),
            new Planet("Earth", 10, 140, 1, 365, "2k_earth_daymap.jpg"),
            new Planet("Mars", 9, 180, 1.03, 687, "2k_mars.jpg"),
            new Planet("Jupiter", 20, 240, 0.41, 4333, "2k_jupiter.jpg"),
            new Planet("Saturn", 18, 300, 0.45, 10759, "2k_saturn.jpg"),
            new Planet("Uranus", 15, 360, -0.72, 30687, "2k_uranus.jpg"),
            new Planet("Neptune", 15, 420, 0.67, 60190, "2k_neptune.jpg"),
            new Planet("Pluto", 5, 480, -6.39, 90560, "2k_pluto.jpg")
    };

    private final Group root3D = new Group();
    private final Group planetsGroup = new Group();
    private final double TIME_SCALE = 0.1;

    @Override
    public void start(Stage stage) {
        SubScene scene3D = createSolarSystemScene();
        StackPane stack = new StackPane(scene3D);
        stack.setBackground(new Background(new BackgroundImage(
                new Image(RES_PATH + "2k_stars_milky_way.jpg", true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

        Scene scene = new Scene(stack, 1280, 1000, true, SceneAntialiasing.BALANCED);
        stage.setScene(scene);
        stage.setTitle("Solar System Viewer");
        stage.show();

        animatePlanets();
        scene.setOnKeyPressed(this::handleKey);
    }

    private SubScene createSolarSystemScene() {
        for (Planet p : planets) {
            createPlanet(p);
        }

        root3D.getChildren().add(planetsGroup);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1000);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setFieldOfView(35);

        SubScene subScene = new SubScene(root3D, 1280, 1000, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        return subScene;
    }

    private void createPlanet(Planet planet) {
        planet.sphere = new Sphere(planet.radius);
        PhongMaterial mat = new PhongMaterial();
        try {
            mat.setDiffuseMap(new Image(RES_PATH + planet.texture, true));
        } catch (Exception e) {
            System.err.println("Failed to load texture for " + planet.name);
        }
        planet.sphere.setMaterial(mat);

        planetsGroup.getChildren().add(planet.sphere);

        // Spin the planet
        RotateTransition selfRotate = new RotateTransition(Duration.seconds(Math.abs(planet.selfRotateTime)), planet.sphere);
        selfRotate.setAxis(Rotate.Y_AXIS);
        selfRotate.setByAngle(360 * Math.signum(planet.selfRotateTime));
        selfRotate.setCycleCount(Animation.INDEFINITE);
        selfRotate.setInterpolator(Interpolator.LINEAR);
        selfRotate.play();
    }

    private void animatePlanets() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), e -> {
            for (Planet planet : planets) {
                if (planet.orbitRadius > 0) {
                    planet.angle += 360.0 / (planet.orbitPeriodDays * TIME_SCALE);
                    double rad = Math.toRadians(planet.angle);
                    double x = planet.orbitRadius * Math.cos(rad);
                    double z = planet.orbitRadius * Math.sin(rad);
                    planet.sphere.setTranslateX(x);
                    planet.sphere.setTranslateZ(z);
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void handleKey(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
