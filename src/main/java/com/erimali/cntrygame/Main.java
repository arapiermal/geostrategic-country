package com.erimali.cntrygame;

import com.erimali.compute.EriScriptGUI;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    protected static final String APP_NAME = "Strategical Geopolitics Simulator";
    private Stage primaryStage;
    private Stage gStage;

    @Override
    public void start(Stage primaryStage) {
        GOptions.loadGOptions();
        this.primaryStage = primaryStage;
        //primaryStage.initStyle(StageStyle.UNDECORATED); //Removes borders
        primaryStage.setTitle(APP_NAME + " - Main Menu");

        Button startButton = createButton("Start Game", this::startGame);
        Button optionsButton = createButton("Options", this::openOptions);
        Button exitButton = createButton("Exit", this::exitApplication);
        Text extraContent = new Text("Extra content");
        extraContent.setStyle("-fx-font-style: italic; -fx-fill: white;");

        Button esGUI = createButton("EriScript GUI", this::openEriScriptGUI);
        Button discl = createButton("Disclaimer", this::openDisclaimer);

        VBox menuLayout = createMenuLayout(startButton, optionsButton, exitButton, extraContent, esGUI, discl);


        BorderPane root = createRootContainer(menuLayout);

        //
        InputStream inputStreamImg = getClass().getResourceAsStream("img/Blue_Marble_2002.png");

        if (inputStreamImg != null) {
            Image bgImage = new Image(inputStreamImg);
            ImageView imageView = new ImageView(bgImage);
            imageView.fitWidthProperty().bind(primaryStage.widthProperty());
            imageView.fitHeightProperty().bind(primaryStage.heightProperty());

            root.setBackground(new Background(new BackgroundImage(
                    bgImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, true, true)
            )));

        }

        Scene scene = new Scene(root, 1280, 720);
        //scene.getStylesheets().add(getClass().getResource("css/main.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setOnAction(e -> action.run());
        return button;
    }

    private VBox createMenuLayout(Node... nodes) {
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(nodes);

        return menuLayout;
    }

    private BorderPane createRootContainer(VBox centerLayout) {
        BorderPane root = new BorderPane();
        root.setCenter(centerLayout);
        root.setPadding(new Insets(40));
        return root;
    }

    private void startGame() {
        if (gStage == null) {
            gStage = new GameStage();
        }
        primaryStage.close(); // Close the first stage

        gStage.show();
    }

    private void openOptions() {
        Stage optStage = new Stage();
        optStage.setTitle("Options");

        VBox optLayout = createOptionsLayout(optStage);
        Scene optScene = new Scene(optLayout, 400, 300);
        optStage.setScene(optScene);
        optStage.initModality(Modality.APPLICATION_MODAL);

        // Main Menu disabled while options open
        primaryStage.getScene().getRoot().setDisable(true);

        optStage.setOnHidden(e -> {
            primaryStage.getScene().getRoot().setDisable(false);
            primaryStage.toFront();
            GOptions.saveToFile();
        });
        optStage.showAndWait();
    }

    private void openDisclaimer() {
        Disclaimer.generateDisclaimer().show();
    }

    private VBox createOptionsLayout(Stage optionsStage) {
        VBox optionsLayout = new VBox(20);
        optionsLayout.setAlignment(Pos.CENTER);
        optionsLayout.setPadding(new Insets(40));

        CheckBox fullScreen = new CheckBox("Fullscreen");
        fullScreen.setSelected(GOptions.isFullScreen());

        fullScreen.setOnAction(event -> {
            GOptions.setFullScreen(fullScreen.isSelected());
        });
        Text volume = new Text("Volume = ");
        Label sliderCurrently = new Label(String.valueOf(GOptions.getVolume()));
        HBox volumeHBox = new HBox(volume, sliderCurrently);
        Slider slider = new Slider(0, 100, GOptions.getVolume());

        slider.setMajorTickUnit(10.0);

        slider.setShowTickMarks(true);
        // slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderCurrently.setText(String.valueOf(newValue.intValue()));
            GOptions.setVolume(newValue.intValue());
        });
        CheckBox trGEvent = new CheckBox("Allow translating certain Game Event texts");
        trGEvent.setSelected(GOptions.isTranslateGEvent());
        trGEvent.setOnAction(event -> {
            GOptions.setTranslateGEvent(trGEvent.isSelected());
        });
        Button closeButton = new Button("Save & Close");
        closeButton.setOnAction(e -> optionsStage.close());

        optionsLayout.getChildren().addAll(fullScreen, volumeHBox, slider, trGEvent, closeButton);

        return optionsLayout;
    }

    private void exitApplication() {
        primaryStage.close();

        if (gStage != null) {
            gStage.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Stage getGs() {
        return gStage;
    }

    public void setGs(Stage gStage) {
        this.gStage = gStage;
    }

    private void openEriScriptGUI() {
        Stage eriScriptGUI = new EriScriptGUI();
        eriScriptGUI.show();
    }
}
