package com.erimali.cntryrandom;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RandomGenStage extends Stage {
    private Scene mainScene;
    private ScrollPane scrollPane;
    private Pane randMapPane;
    private BorderPane root;
    // Text Fields for parameters (width, height, number of provinces, etc.)
    private VBox rightVBox;
    private TextField textFieldWidth;
    private TextField textFieldHeight;
    private TextField textFieldTotalCells;

    // ToolBar
    private Button genButton;
    private Button saveSVGButton;
    // Map Logic
    private RandWorldMap randMap;

    public RandomGenStage() {
        genButton = new Button("Generate");
        saveSVGButton = new Button("Save SVG");
        ToolBar bottomToolBar = new ToolBar(genButton, saveSVGButton);
        genButton.setOnAction(e -> generateRandomMap());
        saveSVGButton.setOnAction(e -> saveAsSVG());

        rightVBox = new VBox();
        scrollPane = new ScrollPane();
        root = new BorderPane(scrollPane);
        root.setBottom(bottomToolBar);
        root.setRight(rightVBox);
        mainScene = new Scene(root);

        setScene(mainScene);
    }

    private void saveAsSVG() {
        // pop up where to save

    }

    private void generateRandomMap() {
        double mapWidth = 800;
        double mapHeight = 600;

        randMap = new RandWorldMap(mapWidth, mapHeight);
        randMapPane = RandMapFX.createPaneFX(randMap);
        scrollPane.setContent(randMapPane);
    }
}
