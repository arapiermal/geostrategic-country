package com.erimali.minigames;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class BackgroundImageExample extends Application {

    private double VIEWPORT_WIDTH = 800;
    private double VIEWPORT_HEIGHT = 600;

    @Override
    public void start(Stage stage) {
        // Create the content for the center of the StackPane
        StackPane centerContent = new StackPane();
        // Add your content to the centerContent StackPane
        // ...

        // Load the background image
        Image backgroundImage = new Image("file:resources/map/2k_stars_milky_way.jpg");
        // Create a BackgroundImage with the desired image, repeat, position, and size
        BackgroundImage backgroundImg = new BackgroundImage(
            backgroundImage,
            BackgroundRepeat.NO_REPEAT, // or BackgroundRepeat.REPEAT if you want to repeat the image
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,   // or specify other positions as desired
            new BackgroundSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, false, false, false, false)
        );

        // Set the BackgroundImage to the centerContent StackPane
        centerContent.setBackground(new Background(backgroundImg));

        // Create the main scene using the StackPane
        Scene scene = new Scene(centerContent, VIEWPORT_WIDTH, VIEWPORT_HEIGHT, Color.BLACK);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
