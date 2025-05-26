package com.erimali.cntryrandom;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Random;

enum Direction {
    UP, RIGHT, DOWN, LEFT;

    public static Direction random() {
        return values()[(int) (Math.random() * values().length)];
    }
}

class StarSymbol extends Polygon {
    public StarSymbol(Color color) {
        double size = 100;
        double centerX = size / 2;
        double centerY = size / 2;
        double outerRadius = size / 2;
        double innerRadius = outerRadius / 2.5;

        for (int i = 0; i < 10; i++) {
            double angle = Math.toRadians(i * 36 - 90); // Top point -> upwards
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            getPoints().addAll(x, y);
        }

        setFill(color);
        setStroke(color);
    }
}

class Triangle extends Polygon {

    public Triangle(Color color, Direction direction) {
        double size = 100;
        double halfSize = size / 2;

        //Pointing up
        getPoints().addAll(
                0.0, 0.0,   // Left point
                size, 0.0,       // Right point
                halfSize, size   // Bottom point
        );

        // Apply the correct rotation based on the direction
        switch (direction) {
            case DOWN:
                setRotate(180); // Pointing down
                break;
            case LEFT:
                setRotate(90); // Pointing left
                break;
            case RIGHT:
                setRotate(-90); // Pointing right
                break;
            case UP:
            default:

                break;
        }

        setFill(color);
    }
}

public class RandomFlagGeneratorFX extends Application {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;
    private static final Random rand = new Random();

    private Pane flagPane;
    private List<Shape> symbols;


    @Override
    public void start(Stage primaryStage) {
        flagPane = generateFlag(); // Generate the flag
        BorderPane root = new BorderPane();
        root.setCenter(flagPane);
        Button nextFlag = new Button();
        nextFlag.setOnAction(e -> {
            flagPane = generateFlag();
            root.setCenter(flagPane);
        });
        root.setBottom(new HBox(nextFlag));
        Scene scene = new Scene(root, WIDTH, HEIGHT + 100);

        primaryStage.setTitle("Random Flag Generator");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Export options
        //saveAsPNG("random_flag.png");
        //saveAsSVG("random_flag.svg");
    }

    private Pane generateFlag() {
        Pane pane = new Pane();
        int layoutType = rand.nextInt(2); // 0 = Horizontal, 1 = Vertical // Make 2&3 diagonal, could make with triangle from left to right
        int stripeCount = 1 + rand.nextInt(3); // 1 to 3 stripes

        for (int i = 0; i < stripeCount; i++) {
            Color color = getRandomColor();
            Rectangle stripe;

            if (layoutType == 0) { // Horizontal stripes
                stripe = new Rectangle(0, i * ((double) HEIGHT / stripeCount), WIDTH, (double) HEIGHT / stripeCount);
            } else { // Vertical stripes
                stripe = new Rectangle(i * ((double) WIDTH / stripeCount), 0, (double) WIDTH / stripeCount, HEIGHT);
            }

            stripe.setFill(color);
            pane.getChildren().add(stripe);
        }
        Shape symbol = getRandomSymbol();
        if (symbol != null) {
            fitSymbol(symbol, WIDTH, HEIGHT, WIDTH * 0.5, HEIGHT * 0.5);
            pane.getChildren().add(symbol);
        }
        return pane;
    }

    private Shape getRandomSymbol() {
        int choice = rand.nextInt(4); // Pick a random symbol type
        Color color = getRandomColor();
        switch (choice) {
            case 1:
                return new Circle(50, color);
            case 2:
                return new Triangle(color, Direction.random());
            case 3:
                return new StarSymbol(color);
            default:
                return null;
        }
    }

    private Color getRandomColor() {
        return Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()); // Random RGB colors
    }

    private void saveAsPNG(String filename) {
        WritableImage image = flagPane.snapshot(null, null);
        File file = new File(filename);
        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
            ImageIO.write(bufferedImage, "png", file);
            System.out.println("PNG saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving PNG: " + e.getMessage());
        }
    }

    // Not necessary
    private void saveAsSVG(String filename) {
        StringBuilder svg = new StringBuilder();
        svg.append("<svg width='").append(WIDTH).append("' height='").append(HEIGHT).append("' xmlns='http://www.w3.org/2000/svg'>\n");

        int layoutType = rand.nextInt(2);
        int stripeCount = 1 + rand.nextInt(3);

        for (int i = 0; i < stripeCount; i++) {
            Color color = getRandomColor();
            String hexColor = String.format("#%02X%02X%02X",
                    (int) (color.getRed() * 255),
                    (int) (color.getGreen() * 255),
                    (int) (color.getBlue() * 255));

            if (layoutType == 0) { // Horizontal stripes
                svg.append(String.format("<rect x='0' y='%d' width='%d' height='%d' fill='%s'/>\n",
                        i * (HEIGHT / stripeCount), WIDTH, HEIGHT / stripeCount, hexColor));
            } else { // Vertical stripes
                svg.append(String.format("<rect x='%d' y='0' width='%d' height='%d' fill='%s'/>\n",
                        i * (WIDTH / stripeCount), WIDTH / stripeCount, HEIGHT, hexColor));
            }
        }

        svg.append("</svg>");

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(svg.toString());
            System.out.println("SVG saved: " + new File(filename).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error saving SVG: " + e.getMessage());
        }
    }

    public static void fitSymbol(Shape symbol, double flagWidth, double flagHeight, double targetWidth, double targetHeight) {
        double originalWidth = symbol.getBoundsInLocal().getWidth();
        double originalHeight = symbol.getBoundsInLocal().getHeight();

        if (originalWidth == 0 || originalHeight == 0) return; // Prevent division by zero

        // Scale factors to fit within target dimensions
        double scaleX = targetWidth / originalWidth;
        double scaleY = targetHeight / originalHeight;
        double scale = Math.min(scaleX, scaleY); // Maintain aspect ratio

        // Apply uniform scaling
        symbol.setScaleX(scale);
        symbol.setScaleY(scale);

        // Recalculate adjusted width & height after scaling
        double adjustedWidth = originalWidth * scale;
        double adjustedHeight = originalHeight * scale;

        // Center the symbol inside the flag
        double centerX = (flagWidth - adjustedWidth) / 2 - symbol.getBoundsInLocal().getMinX() * scale;
        double centerY = (flagHeight - adjustedHeight) / 2 - symbol.getBoundsInLocal().getMinY() * scale;

        symbol.setTranslateX(centerX);
        symbol.setTranslateY(centerY);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
