package com.erimali.cntryrandom;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RandomGenStage extends Stage {
    private Scene mainScene;
    private Pane randMapPane;
    private BorderPane root;
    // Text Fields for parameters (width, height, number of provinces, etc.)
    //
    private Button genButton;
    // Map Logic
    private RandomizedWorldMap randMap;

    public RandomGenStage() {
        randMapPane = new Pane();
        genButton = new Button("Generate");
        ToolBar bottomToolBar = new ToolBar(genButton);
        genButton.setOnAction(e -> generateRandomMap());
        root = new BorderPane(randMapPane);
        root.setBottom(bottomToolBar);
        mainScene = new Scene(root);

        setScene(mainScene);
    }

    private void generateRandomMap() {
        double mapWidth = 800;
        double mapHeight = 600;

        randMap = new RandomizedWorldMap(mapWidth, mapHeight);

    }
}
