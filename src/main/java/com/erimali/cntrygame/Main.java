package com.erimali.cntrygame;

import com.erimali.compute.EriScriptGUI;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;

public class Main extends Application {
    protected static final String APP_NAME = "Strategical Geopolitics Simulator";
    private static Image gameIcon;
    private static Image settingsIcon;

    static {
        InputStream inputStream = GameStage.class.getResourceAsStream("img/gameIcon.png");
        if (inputStream != null)
            gameIcon = new Image(inputStream);

    }

    private Stage primaryStage;
    private Stage gameStage;
    private SVGPath trashIcon;

    @Override
    public void start(Stage primaryStage) {
        trashIcon = WorldMap.loadSVGPath("img/trash.svg");
        if (trashIcon != null) {
            trashIcon.setStrokeWidth(2);
            trashIcon.setStroke(Color.BLACK);
            trashIcon.setFill(Color.TRANSPARENT);
        }
        GOptions.loadGOptions();
        SaveGame.loadSaveGamePaths();

        this.primaryStage = primaryStage;

        primaryStage.setTitle(APP_NAME + " - Main Menu");
        loadGameIcon(primaryStage);
        Button startButton = createButton("New Game", this::startGame);
        Button loadButton = createButton("Load Game", this::loadGame);

        Button optionsButton = createButton("Options", this::openOptions);
        Button exitButton = createButton("Exit", this::exitApplication);
        Text extraContent = new Text("Extra content");
        extraContent.setStyle("-fx-font-style: italic; -fx-fill: white;");

        Button esGUI = createButton("EriScript GUI", this::openEriScriptGUI);
        Button disclaimerButton = createButton("Disclaimer", this::openDisclaimer);

        VBox menuLayout = createMenuLayout(startButton, loadButton, optionsButton, exitButton, extraContent, esGUI, disclaimerButton);

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
        root.setBottom(trashIcon);
        root.setPadding(new Insets(40));
        return root;
    }

    private void startGame() {
        if (gameStage == null) {
            gameStage = new GameStage(this);
        }
        closePrimaryOpenGame();
    }

    private void openOptions() {
        Stage optStage = new Stage();
        optStage.setTitle("Options");

        VBox optLayout = createOptionsLayout(optStage);
        Scene optScene = new Scene(optLayout);
        optStage.setScene(optScene);
        optStage.initModality(Modality.APPLICATION_MODAL);

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
        Label sliderCurrently = new Label(String.valueOf(GOptions.getVolumeInt()));
        HBox volumeHBox = new HBox(volume, sliderCurrently);
        Slider slider = new Slider(0, 100, GOptions.getVolumeInt());

        slider.setMajorTickUnit(10.0);

        slider.setShowTickMarks(true);
        // slider.setSnapToTicks(true);
        slider.setShowTickLabels(true);
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            sliderCurrently.setText(String.valueOf(newValue.intValue()));
            GOptions.setVolume(newValue.intValue());
        });

        CheckBox allowCLI = new CheckBox("Allow Command-Line Interface (Cheats)");
        allowCLI.setSelected(GOptions.isAllowCLI());
        allowCLI.setOnAction(event -> {
            GOptions.setAllowCLI(allowCLI.isSelected());
        });
        CheckBox trGEvent = new CheckBox("Allow translating certain Game Event texts");
        trGEvent.setSelected(GOptions.isTranslateGEvent());
        trGEvent.setOnAction(event -> {
            GOptions.setTranslateGEvent(trGEvent.isSelected());
        });
        CheckBox allowMods = new CheckBox("Allow Mods");
        allowMods.setSelected(GOptions.isAllowMods());
        allowMods.setOnAction(event -> {
            GOptions.setAllowMods(allowMods.isSelected());
        });
        Button changeModsDir = new Button("Change Mods Dir");
        changeModsDir.setOnAction(e -> GOptions.changeDirectoryPath(optionsStage));
        CheckBox debugMode = new CheckBox("Debug Mode");
        Tooltip.install(debugMode, new Tooltip("Show selected country and province ID."));
        debugMode.setSelected(GOptions.isDebugMode());
        debugMode.setOnAction(event -> {
            GOptions.setDebugMode(debugMode.isSelected());
        });
        Button closeButton = new Button("Save & Close");
        closeButton.setOnAction(e -> optionsStage.close());

        optionsLayout.getChildren().addAll(fullScreen, volumeHBox, slider, allowCLI, trGEvent, allowMods, changeModsDir, debugMode, closeButton);

        return optionsLayout;
    }

    private void exitApplication() {
        primaryStage.close();

        if (gameStage != null) {
            gameStage.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void openEriScriptGUI() {
        Stage eriScriptGUI = new EriScriptGUI();
        eriScriptGUI.show();
    }

    public void loadGame() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Load save-game");
        ListView<String> listView = makeSaveGameListView();
        dialog.getDialogPane().setContent(listView);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Select");
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    String save = listView.getSelectionModel().getSelectedItem();
                    if (save == null) {
                        dialog.setHeaderText("Pick a save-game");
                        event.consume();
                    } else {
                        GLogic loadGame = SaveGame.loadGame(save);
                        if (gameStage == null && loadGame != null) {
                            gameStage = new GameStage(this, loadGame);
                            closePrimaryOpenGame();
                        }
                    }
                }
        );

        dialog.showAndWait();
    }

    public void closePrimaryOpenGame() {
        primaryStage.close();
        gameStage.show();
    }

    public void closeGameOpenPrimary() {
        gameStage.close();
        gameStage = null;
        primaryStage.show();
    }

    private ListView<String> makeSaveGameListView() {
        ListView<String> listView = new ListView<>(SaveGame.saves);
        listView.setCellFactory(param -> new ListCell<>() {
            private final Button removeButton = new Button(trashIcon == null ? "Delete" : null, trashIcon);

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    removeButton.setOnAction(event -> {
                        String fileName = getItem();
                        if (SaveGame.deleteSaveGame(fileName))
                            getListView().getItems().remove(fileName);
                    });
                    setGraphic(removeButton);
                }
            }
        });
        return listView;
    }

    public static void loadGameIcon(Stage stage) {
        stage.getIcons().add(gameIcon);

    }
}
