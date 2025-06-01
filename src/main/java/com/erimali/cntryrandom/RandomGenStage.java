package com.erimali.cntryrandom;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

enum VoronoiEnum {
    BASIC, JITTERED;
}

public class RandomGenStage extends Stage {
    private Scene mainScene;
    private ScrollPane scrollPane;
    private Pane randMapPane;
    private WritableImage perlinMapWriteableImage;
    private ImageView perlinMapImageView;
    private BorderPane root;
    // Text Fields for parameters (width, height, number of provinces, etc.)
    private VBox rightVBox;
    private ComboBox<VoronoiEnum> voronoiComboBox;
    private TextField textFieldWidth;
    private TextField textFieldHeight;
    private TextField textFieldTotalCells;

    private Spinner<Integer> relaxSpinner;
    // ToolBar
    private Button genButton;
    private Button relaxButton;
    private Button saveSVGButton;
    private ToggleButton showPerlinMapToggle;
    private Button savePNGButton;
    private Button genProvNamesButton;

    // Map Logic
    private RandWorldMap randMap;

    public RandomGenStage() {
        UsefulColors.loadUsefulColors();

        genButton = new Button("Generate");
        relaxButton = new Button("Relax");
        saveSVGButton = new Button("Save SVG");
        showPerlinMapToggle = new ToggleButton("Terrain (Perlin)");
        savePNGButton = new Button("Save PNG (Perlin)");
        genProvNamesButton = new Button("Generate Province Names");
        ToolBar bottomToolBar = new ToolBar(genButton, relaxButton, saveSVGButton, showPerlinMapToggle, savePNGButton, genProvNamesButton);
        genButton.setOnAction(e -> generateRandomMap());
        relaxButton.setOnAction(e -> relaxMap());
        saveSVGButton.setOnAction(e -> saveAsSVG());
        showPerlinMapToggle.setOnAction(e -> toggleTerrainMap());
        savePNGButton.setOnAction(e -> savePerlinAsPNG());
        genProvNamesButton.setOnAction(e -> generateProvinceNames());
        voronoiComboBox = new ComboBox<>();
        voronoiComboBox.getItems().setAll(VoronoiEnum.values());

        relaxSpinner = new Spinner<>(2, 10, 2);
        rightVBox = new VBox(new Label("Voronoi Type:"), voronoiComboBox,
                new Label("Lloyd Iterations:"), relaxSpinner);
        scrollPane = new ScrollPane();

        root = new BorderPane(scrollPane);
        root.setBottom(bottomToolBar);
        root.setRight(rightVBox);
        mainScene = new Scene(root, 1000, 680);

        setScene(mainScene);
    }

    private void savePerlinAsPNG() {
        // pop up where to save
        if (randMap == null) {
            showAlert("No map generated", "Please generate a map before saving.");
            return;
        } else if (perlinMapWriteableImage == null) {
            this.perlinMapWriteableImage = PerlinMapFX.genPerlinMapImage(randMap);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PNG Terrain Map");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));

        File file = fileChooser.showSaveDialog(this);

        if (file != null) {
            PerlinMapFX.saveImage(perlinMapWriteableImage, file);

        }
    }

    private void toggleTerrainMap() {

        if (showPerlinMapToggle.isSelected()) {
            if (perlinMapImageView == null && randMap != null) {
                perlinMapImageView = genImageViewPerlin(randMap);
            }
            scrollPane.setContent(perlinMapImageView);
        } else {
            scrollPane.setContent(randMapPane);

        }
    }

    private ImageView genImageViewPerlin(RandWorldMap randMap) {
        if (perlinMapWriteableImage == null) {
            this.perlinMapWriteableImage = PerlinMapFX.genPerlinMapImage(randMap);
        }
        return new ImageView(perlinMapWriteableImage);

    }

    private void generateProvinceNames() {
        if (randMap != null) {
            randMap.generateProvinceNames();
        }
    }

    private void saveAsSVG() {
        // pop up where to save
        if (randMap == null) {
            showAlert("No map generated", "Please generate a map before saving.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save SVG Map");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG Files", "*.svg"));

        File file = fileChooser.showSaveDialog(this);

        if (file != null) {

            String svgContent = SVGSave.generateSVG(randMap.getMapWidth(), randMap.getMapHeight(), randMap.getZones());
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(svgContent);
                showAlert("Success", "Map saved successfully to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Error", "Failed to save file:\n" + e.getMessage());
            }
        }
    }

    private void generateRandomMap() {
        VoronoiEnum chosenVoronoi = voronoiComboBox.getValue();
        if (chosenVoronoi == null) {
            showAlert("Invalid Voronoi Type", "Please pick a Voronoi method from the dropdown list in the right.");
            return;
        }
        double mapWidth = 800;
        double mapHeight = 600;

        randMap = new RandWorldMap(mapWidth, mapHeight);
        if (chosenVoronoi.equals(VoronoiEnum.BASIC)) {
            randMap.basicVoronoi();
        } else if (chosenVoronoi.equals(VoronoiEnum.JITTERED)) {
            randMap.jitteredVoronoi();
        }
        randMap.generateAll();
        randMapPane = RandMapFX.createPaneFX(randMap);
        scrollPane.setContent(randMapPane);
        perlinMapImageView = null;
    }

    private void relaxMap() {
        if (randMap != null) {
            int iterations = relaxSpinner.getValue();
            randMap.getVoronoi().relax(iterations);
            randMap.generateAll();
            randMapPane = RandMapFX.createPaneFX(randMap);
            scrollPane.setContent(randMapPane);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
