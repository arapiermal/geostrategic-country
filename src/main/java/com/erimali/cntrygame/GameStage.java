package com.erimali.cntrygame;

import java.io.File;
import java.net.URL;

import com.erimali.cntrymilitary.MilUnitData;
import javafx.application.Platform;
import javafx.beans.value.ObservableIntegerValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import javax.tools.Tool;

public class GameStage extends Stage {
    // POP UP WHEN FULLSCREEN PROBLEM
    private Label countryName;
    private Label date;
    private Label pausation;
    private Button pauseButton;
    private Button chooseCountryButton;
    protected boolean paused;
    protected boolean isPlayingCountry;
    // Map related
    private WorldMap map;
    private GLogic game;
    private int selectedCountry;
    private int selectedProv;

    //Alternative ?
    private ObservableIntegerValue selCountry;
    private ObservableIntegerValue selProv;

    private Label hoveringCountry;
    private Label selectedCountryInfo;
    private Label selectedProvInfo;

    private Label mgResult;
    //Our country/prov -> 0, Our subjects country/prov, Others' country/prov (taking care if they are subjects themselves)
    //subject in general is like other country...
    private VBox[] countryOptTypes;
    private VBox[] provOptTypes;
    //
    private int prevLeftVBoxInd;
    private VBox leftGeneral;
    //
    private ToggleButton improveRelations;
    private Stage commandLineStage;
    private Label infoRelations;
    private Button sendAllianceRequest;

    private Stage gsOptionsStage;
    private Scene[] gsOptionsScenes;
    private TextField saveTextField;
    private Button recruitBuildButton;

    public GameStage() {
        setTitle(Main.APP_NAME + " - Game");
        setOnCloseRequest(e -> close());
        this.selectedCountry = -1;
        this.map = new WorldMap(this);
        this.game = new GLogic(this);

        BorderPane gameLayout = createGameLayout();
        setWidth(1280);
        setHeight(720);
        changeDate(game.inGDateInfo());
        CommandLine.setGameStage(this);

        Scene gameScene = new Scene(gameLayout);
        setScene(gameScene);
        URL cssGameScene = getClass().getResource("css/gameStage.css");
        if(cssGameScene != null)
            gameScene.getStylesheets().add(cssGameScene.toExternalForm());
        //Initiate
        game.getWorld().initiateProvinces(map.getMapSVG());

        this.setFullScreen(GOptions.isFullScreen());
        // showPopupMGTicTacToe(false,2);
        /*
         * GEvent gEvent = new GEvent("Title", new GDate("1/1/2024"), "Hello there", new
         * String[] { "Opt 1", "Opt 2", "Opt 3" }, new String[] { "Opt 1", "Opt 2",
         * "Opt 3" }); popupGEvent(gEvent);
         */
        // popupWebNews();
    }

    //Load game
    public GameStage(GLogic game) {
        setTitle(Main.APP_NAME + " - Game");
        setOnCloseRequest(e -> close());//...
        this.selectedCountry = -1;
        game.setGameStage(this);
        game.startTimer();
        this.map = new WorldMap(this);
        this.game = game;
        game.loadAllUnitData();
        game.correlateAllUnitData();
        BorderPane gameLayout = createGameLayout();
        changeDate(game.inGDateInfo());
        updateGameLayout();
        CommandLine.setCountries(game.getWorldCountries());
        CommandLine.setPlayerCountry(game.getPlayerId());
        setWidth(1280);
        setHeight(720);


        CommandLine.setGameStage(this);
        Scene gameScene = new Scene(gameLayout);
        setScene(gameScene);
        gameScene.getStylesheets().add(getClass().getResource("css/gameStage.css").toExternalForm());
        //Correlate
        game.getWorld().correlateProvinces(map.getMapSVG());

        this.setFullScreen(GOptions.isFullScreen());
    }

    private void updateGameLayout() {
        isPlayingCountry = game.getPlayerId() >= 0;
        if (isPlayingCountry) {
            countryName.setText(game.getPlayer().getName());
            //map.setPlayerCountry(game.getPlayerId());
            chooseCountryButton.setVisible(false);
            changeDate(game.inGDateInfo());
        }
    }

    private HBox makeGameSpeedHBox() {
        Label speedData = new Label("1.0");
        Text speedDataInfo = new Text("days/sec");
        Button speedUp = new Button("+");
        speedUp.setPrefHeight(18);
        speedUp.setPrefWidth(26);
        speedUp.setStyle("-fx-background-color: lightblue; -fx-shape: 'M 0 50 L 25 0 L 50 50 Z'");
        speedUp.setOnAction(e -> {
            game.increaseSpeed();
            speedData.setText(String.valueOf(game.getSpeed()));
        });

        Button speedDown = new Button("-");
        speedDown.setPrefHeight(18);
        speedDown.setPrefWidth(26);
        speedDown.setStyle("-fx-background-color: lightblue; -fx-shape: 'M 0 0 L 25 50 L 50 0 Z'");
        speedDown.setOnAction(e -> {
            game.decreaseSpeed();
            speedData.setText(String.valueOf(game.getSpeed()));
        });

        VBox vBox = new VBox(speedUp, speedDown);
        vBox.setSpacing(4);
        return new HBox(vBox, speedData, speedDataInfo);
    }

    private BorderPane createGameLayout() {
        BorderPane gameLayout = new BorderPane();
        gameLayout.setPadding(new Insets(8));
        // TOP
        gameLayout.setCenter(new BorderPane());
        this.countryName = new Label("Select Country");
        this.date = new Label("Default Date");
        this.pauseButton = new Button("Play");
        this.paused = true;
        this.pauseButton.setOnAction(e -> pausePlayDate());
        this.pausation = new Label("Paused");

        chooseCountryButton = new Button("Confirm");
        chooseCountryButton.setOnAction(e -> startGame());
        HBox hTopLeft = new HBox(getCountryName(), chooseCountryButton);
        hTopLeft.setSpacing(10);

        HBox hGameSpeed = makeGameSpeedHBox();
        HBox hTopRight = new HBox(pausation, pauseButton, date, hGameSpeed);
        hTopRight.setSpacing(10);
        Region regTop = new Region();

        HBox htop = new HBox(hTopLeft, regTop, hTopRight);
        HBox.setHgrow(regTop, Priority.ALWAYS);
        htop.setSpacing(10);
        htop.setStyle("-fx-background-color: #f0f0f0;");

        gameLayout.setTop(htop);

        // CENTER
        gameLayout.setCenter(map.start());

        // LEFT
        // close X
        ScrollPane leftScrollPane = new ScrollPane();
        leftScrollPane.setPadding(new Insets(10));
        /////
        leftScrollPane.setMinWidth(240);
        selectedCountryInfo = new Label();
        selectedProvInfo = new Label();
        initVBoxLeftOptions();

        leftGeneral = new VBox(selectedCountryInfo, countryOptTypes[0], selectedProvInfo, provOptTypes[0]);
        leftGeneral.getChildren().get(1).setVisible(false);
        leftGeneral.getChildren().get(3).setVisible(false);
        leftGeneral.setSpacing(10);

        leftScrollPane.setContent(leftGeneral);
        gameLayout.setLeft(leftScrollPane);
//////////////////////////////////////////////////////////////
        //Bottom
        //hoveringCountry = new Label("Hovering");
        //HBox hBottomLeft = new HBox(hoveringCountry);

        Button gsComputer = new Button("Computer");
        Button gsOptions = new Button("Settings");
        try {
            URL imgSrcComputer = getClass().getResource("img/monitor_with_button.png");
            if(imgSrcComputer != null) {
                ImageView imgViewSettings = new ImageView(imgSrcComputer.toExternalForm());
                imgViewSettings.setFitHeight(24);
                imgViewSettings.setFitWidth(24);
                gsComputer.setGraphic(imgViewSettings);
                gsComputer.setText(null);
            }
            URL imgSrcSettings = getClass().getResource("img/settings.png");
            if(imgSrcSettings != null) {
                ImageView imgViewSettings = new ImageView(imgSrcSettings.toExternalForm());
                imgViewSettings.setFitHeight(24);
                imgViewSettings.setFitWidth(24);
                gsOptions.setGraphic(imgViewSettings);
                gsOptions.setText(null);
            }
        } catch(Exception e){

        }
        gsComputer.setOnAction(e -> popupWebDesktop());
        gsOptions.setOnAction(e -> showGameStageOptions());
        Region tReg = new Region();
        ToolBar toolBarBottom = new ToolBar(gsComputer,tReg,gsOptions);
        HBox.setHgrow(tReg, Priority.ALWAYS);
        //Region regBottom = new Region();
        //HBox bottom = new HBox(hBottomLeft, regBottom, toolBarBottom);
        //HBox.setHgrow(regBottom, Priority.ALWAYS);

        gameLayout.setBottom(toolBarBottom);
        // maybe Button[], setOnAction( i ...);


        TabPane tabPaneRight = makeRightTabPane();

        Region regRight = new Region();
        Label label = new Label("Map modes");
        FlowPane mapChoices = makeRightMapModesFlowPane();

        VBox vBoxRight = new VBox(tabPaneRight, regRight, label, mapChoices);
        vBoxRight.setPadding(new Insets(4));
        VBox.setVgrow(regRight, Priority.ALWAYS);
        VBox.setVgrow(tabPaneRight, Priority.ALWAYS);

        // rightScrollPane.setContent(rightInfo);
        // gameLayout.setRight(rightScrollPane);
        gameLayout.setRight(vBoxRight);

        gameLayout.setOnKeyPressed(event -> {
            // ` -> commandLine
            switch (event.getCode()) {
                case KeyCode.BACK_QUOTE:
                    commandLineStage.show();
                    commandLineStage.requestFocus();
                    break;
                case KeyCode.ESCAPE:
                    showGameStageOptions();

                    break;
            }
        });

        commandLineStage = makeCommandLineStage();
        gsOptionsScenes = new Scene[2];
        gsOptionsScenes[0] = makeGSOptionsDefScene();
        gsOptionsScenes[1] = makeSaveSceneOptions();
        gsOptionsStage = makeGSOptionsStage();
        return gameLayout;
    }

    private FlowPane makeRightMapModesFlowPane() {
        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL, 10, 10);
        flowPane.setPrefWidth(240);
        Button[] mapModes = new Button[2];
        //enum MapModes (?)
        mapModes[0] = new Button("Default");// change to img
        mapModes[0].setOnAction(event -> {
            map.switchMapMode(0);
        });
        mapModes[1] = new Button("Allies");
        mapModes[1].setOnAction(event -> {
            map.switchMapMode(1);
        });

        flowPane.getChildren().addAll(mapModes);

        return flowPane;
    }

    private TabPane makeRightTabPane() {
        TabPane tabPane = new TabPane();

        tabPane.setMinWidth(280);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab[] tabs = new Tab[2];
        tabs[0] = makeTabMilitary();
        tabs[1] = makeTabBuildings();
        //tabPane.setStyle("-fx-tab-min-width: 0;");

        tabPane.getTabs().addAll(tabs);
        return tabPane;
    }

    private TableView<BuildBuildings.BuildBuilding> tableViewBuildings;

    private Tab makeTabMilitary() {
        Label unitInfo = new Label();
        recruitBuildButton = new Button("Recruit");
        TreeView<MilUnitData> treeUnitTypes = game.makeTreeViewUnitTypes();

        treeUnitTypes.setOnMouseClicked(e -> {
            TreeItem<MilUnitData> selectedItem = treeUnitTypes.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                MilUnitData selUnit = selectedItem.getValue();
                unitInfo.setText(selUnit.toStringLong());
                recruitBuildButton.setText(selUnit.isVehicle() ? "Build" : "Recruit");
            }
        });
        recruitBuildButton.setOnAction(e -> {
            TreeItem<MilUnitData> selectedItem = treeUnitTypes.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                MilUnitData selUnit = selectedItem.getValue();
                game.makeMilUnit(game.getPlayerId(), selectedProv, selUnit);
            }
        });
        VBox vBox = new VBox(treeUnitTypes,recruitBuildButton, unitInfo);
        VBox.setVgrow(unitInfo, Priority.ALWAYS); //
        recruitBuildButton.setVisible(false);
        Tab tab = new Tab("Military", vBox);

        return tab;
    }

    //2-8 months
    private Tab makeTabBuildings() {
        tableViewBuildings = BuildBuildings.makeTableView();
        tableViewBuildings.setVisible(false);

        Tab tab = new Tab("Buildings", tableViewBuildings);

        return tab;
    }

    private Scene makeGSOptionsDefScene() {
        Button saveOptions = new Button("Save");
        saveOptions.setOnAction(e -> {
            saveTextField.setText(getCurrDefSaveGameName());
            gsOptionsStage.setScene(gsOptionsScenes[1]);
        });

        Button exitToMain = new Button("Exit to main menu");
        VBox vBox = new VBox(10, saveOptions, exitToMain);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(20));
        Scene scene = new Scene(vBox, 400, 300);

        return scene;
    }

    private void changeGSOptionsScene(int i) {
        if (i >= 0 && i < gsOptionsScenes.length)
            gsOptionsStage.setScene(gsOptionsScenes[i]);
        else if (i == -1) {
            //go to Main...
            // -2 for save (String currentSaveName) & exit
        }
    }
    public String getCurrDefSaveGameName(){
        return isPlayingCountry ? game.getPlayer().getIso2() +"-" + game.inGDateInfo('_') : "";
    }
    private Scene makeSaveSceneOptions() {
        saveTextField = new TextField();
        Button saveSubmit = new Button("Enter");
        saveTextField.setPrefWidth(240);
        saveSubmit.setPrefWidth(60);
        HBox hBox = new HBox(saveTextField, saveSubmit);
        ListView<String> listView = new ListView<>(SaveGame.saves);
        VBox vBox = new VBox(listView, hBox); //add listview

        saveTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!saveTextField.getText().isBlank()) {
                    try {
                        String save = saveTextField.getText();
                        SaveGame.saveGame(save, game);

                        saveTextField.setText("");
                    } catch (Exception e) {
                    }
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                gsOptionsStage.hide();
            }
        });
        saveSubmit.setOnAction(event -> {
            if (!saveTextField.getText().isBlank()) {
                try {
                    String save = saveTextField.getText();
                    SaveGame.saveGame(save, game);
                    saveTextField.setText("");
                } catch (Exception e) {
                }
            }
        });
        saveTextField.setPromptText("Save-game name");
        Scene scene = new Scene(vBox);

        return scene;
    }

    private void showGameStageOptions() {
        gsOptionsStage.show();
        gsOptionsStage.requestFocus();
    }

    private void initVBoxLeftOptions() {
        this.countryOptTypes = new VBox[3];
        countryOptTypes[0] = makeVBoxPlayerOptions();
        //.setSpacing(10);
        countryOptTypes[1] = makeVBoxCountryOptions();
        countryOptTypes[2] = makeVBoxSubjectOptions();

        this.provOptTypes = new VBox[3];
        provOptTypes[0] = makeVBoxPlayerProvOptions();
        provOptTypes[1] = makeVBoxOtherProvOptions();
        provOptTypes[2] = makeVBoxSubjectProvOptions();
    }

    private Stage makeGSOptionsStage() {
        Stage stage = new Stage();
        stage.setTitle("Options");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(this);
        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(gsOptionsScenes[0]);
        stage.setOnCloseRequest(e -> {
            Scene scene = stage.getScene();
            if (scene.equals(gsOptionsScenes[0])) {
                stage.close();
            } else if (scene.equals(gsOptionsScenes[1])) {
                changeGSOptionsScene(0);
                e.consume();
            }
        });
        return stage;
    }
    //or keep variable outside
    //private int optStageScene;

    private Stage makeCommandLineStage() {
        // Autocomplete for already entered commands (?) !ControlsFX!
        TextField commandLine = new TextField();
        Button commandSubmit = new Button("Enter");
        commandLine.setPrefWidth(240);
        commandSubmit.setPrefWidth(60);
        HBox commandHBox = new HBox(commandLine, commandSubmit);
        Label commandResult = new Label();
        VBox commandVBox = new VBox(commandHBox, commandResult);

        commandLine.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (!commandLine.getText().isBlank()) {
                    try {
                        String res = CommandLine.execute(commandLine.getText());
                        commandResult.setText(res);
                        commandLine.setText("");
                    } catch (Exception e) {
                        commandResult.setText(e.toString());
                    }
                }
            } else if (event.getCode() == KeyCode.ESCAPE) {
                commandLineStage.hide();
            }
        });
        commandSubmit.setOnAction(event -> {
            if (!commandLine.getText().isBlank()) {
                try {
                    String res = CommandLine.execute(commandLine.getText());
                    commandResult.setText(res);
                    commandLine.setText("");
                } catch (Exception e) {
                    commandResult.setText(e.toString());
                }
            }
        });
        commandLine.setPromptText("Type your command here");

        Stage commandLineStage = new Stage();
        commandLineStage.setTitle("Command Line");
        commandLineStage.initModality(Modality.APPLICATION_MODAL);
        commandLineStage.initOwner(this);
        commandLineStage.initStyle(StageStyle.UTILITY);
        commandLineStage.setScene(new Scene(commandVBox));
        return commandLineStage;
    }

    private VBox makeVBoxPlayerOptions() {
        VBox vBox = new VBox();

        return vBox;
    }

    public VBox makeVBoxSubjectOptions() {
        VBox vBox = new VBox();

        return vBox;
    }

    private VBox makeVBoxCountryOptions() {

        Label optionsText = new Label("Options");
        Label optionsWar = new Label("War");
        Button declareWar = new Button("Declare War");
        declareWar.setOnAction(e -> declareWar());
        declareWar.getStyleClass().add("declare-war-button");
        Button sponsorRebels = new Button("Sponsor Rebels");
        sponsorRebels.setOnAction(e -> sponsorRebels());
        sponsorRebels.getStyleClass().add("sponsor-rebels-button");

        VBox vboxWar = new VBox(optionsWar, declareWar, sponsorRebels);

        Label preInfoRelations = new Label("Relations ");
        infoRelations = new Label();
        HBox optionsRelations = new HBox(preInfoRelations, infoRelations);
        improveRelations = new ToggleButton("Improve relations");
        improveRelations.getStyleClass().add("improve-relations-toggle-button");
        Tooltip.install(improveRelations, new Tooltip("Start improving relations with country (monthly)"));
        improveRelations.setOnAction(event -> {
            if (improveRelations.isSelected()) {
                // CSS
                game.addImprovingRelations(selectedCountry);
                //improveRelations.setText("Stop improving relations");
            } else {
                game.removeImprovingRelations(selectedCountry);
                //improveRelations.setText("Improve relations");
            }
        });

        sendAllianceRequest = new Button("Alliance request");
        sendAllianceRequest.setOnAction(e -> {
            sendAllianceRequest();
        });
        Button sendDonation = new Button("Send donation");
        sendDonation.setOnAction(e -> {
            sendDonation();
        });
        VBox vboxRelations = new VBox(optionsRelations, improveRelations, sendAllianceRequest, sendDonation);
        return new VBox(optionsText, vboxWar, vboxRelations);
    }

    private VBox makeVBoxPlayerProvOptions() {
        Button raiseFunds = new Button("Raise municipal funds");
        Button investInProv = new Button("Invest");
        return new VBox(raiseFunds, investInProv);
    }

    private VBox makeVBoxOtherProvOptions() {
        VBox vBox = new VBox();
        return vBox;
    }

    private VBox makeVBoxSubjectProvOptions() {
        VBox vBox = new VBox();
        return vBox;
    }
    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////
    // if(game.isSubjectOfPlayer(selectedCountry))


    public void sendDonation() {
        // selected country -> treasury += input
        //max should be governmentBudget (not spent) / 40
        Double result = showNumberInputDialog(10000000);
        //game.getPlayer().getEconomy().getGDP()/10
        if (result != null) {
            displayNumberInputResult(result);
            game.giveMoney(selectedCountry, result);
            // country send donation method?
            //game.getPlayer().giveMoney(game.getWorldCountries().get(selectedCountry));
        } else {
            showErrorPopup("Invalid input");
        }
    }

    public void sendAllianceRequest() {
        if (game.isAllyWith(selectedCountry)) {


            sendAllianceRequest.setText("Alliance request");
        } else {

            if (game.sendAllianceRequest(selectedCountry)) {
                sendAllianceRequest.setText("Break alliance");
            } else {

            }
        }
    }

    public void declareWar() {
        // pop up, choose casus belli (casus affects war objectives)
        //allies?
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Declare War - Casus Belli");

        ListView<CasusBelli> cbListView = War.makeListViewValidatable(game.getPlayer(), game.getWorldCountries().get(selectedCountry), CasusBelli.class);
        dialog.getDialogPane().setContent(cbListView);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    CasusBelli cb = cbListView.getSelectionModel().getSelectedItem();
                    if (cb == null) {
                        dialog.setHeaderText("Pick a casus belli.");
                        event.consume();
                    } else {
                        game.declareWar(selectedCountry, cb);
                        GameAudio.playShortSound("big-impact.mp3");
                    }
                }
        );


        dialog.showAndWait();
    }

    private void startGame() {
        if (selectedCountry < 0 || isPlayingCountry)
            return;
        game.selectPlayer(selectedCountry);
        countryName.setText(game.getPlayer().getName());// !!!!!!!!
        leftGeneral.getChildren().get(1).setVisible(true);
        leftGeneral.getChildren().get(3).setVisible(true);
        chooseCountryButton.setVisible(false);
        tableViewBuildings.setVisible(true);
        isPlayingCountry = true;
        changeSelectedCountryInfo();
        changeSelectedProvInfo();
    }

    public void pausePlayDate() {
        if (!isPlayingCountry)
            return;
        paused = !paused;
        if (paused) {
            pausation.setVisible(true);
            pauseButton.setText("Play");
        } else {
            pausation.setVisible(false);
            pauseButton.setText("Pause");
        }
    }

    public void pausePlayDate(boolean b) {
        if (!isPlayingCountry)
            return;
        if (paused && !b) {
            paused = false;
            pausation.setVisible(true);
            pauseButton.setText("Play");
        } else if (!paused && b) {
            paused = true;
            pausation.setVisible(false);
            pauseButton.setText("Pause");
        }
    }

    public void changeDate(String d) {
        date.setText(d);
    }

    public void changeCountryName(String cName) {
        countryName.setText(cName);
    }

    public void changeHoveringOverCountry(String cName) {
        hoveringCountry.setText(cName);
    }

    public Label getCountryName() {
        return countryName;
    }

    public void setCountryName(Label countryName) {
        this.countryName = countryName;
    }

    public Label getDate() {
        return date;
    }

    public void setDate(Label date) {
        this.date = date;
    }

    public Label getPausation() {
        return pausation;
    }

    public Button getPauseButton() {
        return pauseButton;
    }


    public Button getChooseCountryButton() {
        return chooseCountryButton;
    }

    public void setChooseCountryButton(Button chooseCountryButton) {
        this.chooseCountryButton = chooseCountryButton;
    }

    public int getSelectedCountry() {
        return selectedCountry;
    }

    public GLogic getGame() {
        return game;
    }

    public void setGame(GLogic game) {
        this.game = game;
    }

    public void changeLeftVBoxes(int i) {
        if (prevLeftVBoxInd == i)
            return;
        prevLeftVBoxInd = i;
        leftGeneral.getChildren().set(1, countryOptTypes[i]);
        leftGeneral.getChildren().set(3, provOptTypes[i]);
    }

    //make more efficient
    public void changeSelectedCountryInfo() {
        selectedCountryInfo.setText(game.toStringCountry(selectedCountry));
        Country selC = game.getWorld().getCountry(selectedCountry);
        if (selC == null)
            return;
        if (isPlayingCountry) {
            if (selectedCountry == game.getPlayerId()) {

                changeLeftVBoxes(0);

            } else {
                if (game.isSubjectOfPlayer(selectedCountry)) {
                    //Most stuff are same/similar...
                    //changeLeftVBoxes(2); save for extra beyond regular here

                    sendAllianceRequest.setText("Release subject"); // Change action...
                } else if (game.isAllyWith(selectedCountry)) {
                    sendAllianceRequest.setText("Break alliance");

                } else {
                    sendAllianceRequest.setText("Alliance request");

                }
                changeLeftVBoxes(1);

                //make more efficient

                infoRelations.setText(game.getRelationsWith(selectedCountry));
                improveRelations.setSelected(game.isImprovingRelations(selectedCountry));
            }
        }

    }

    public void updateSelectedCountryInfo() {
        if (selectedCountry == game.getPlayerId()) {

        } else {

            infoRelations.setText(game.getRelationsWith(selectedCountry));
        }
    }

    public void sponsorRebels() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sponsor rebels");

        ListView<RebelType> rbListView = War.makeListViewValidatable(game.getPlayer(), game.getWorldCountries().get(selectedCountry), RebelType.class);
        dialog.getDialogPane().setContent(rbListView);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    RebelType rt = rbListView.getSelectionModel().getSelectedItem();
                    if (rt == null) {
                        dialog.setHeaderText("Pick a rebel type.");
                        event.consume();
                    } else {
                        Double money = showNumberInputDialog(1000000, 100000000);
                        if (money == null) {
                            dialog.setHeaderText("Money is a number!");
                            event.consume();
                        }
                        double amount = money.doubleValue();
                        TESTING.print(amount);
                        //game.sponsorRebels(rt, selectedCountry, money);
                        GameAudio.playShortSound("low-impact.mp3");
                    }
                }
        );


        dialog.showAndWait();
    }

    // MiniGame
    public void showPopupMGTicTacToe(boolean playerTurn, int difficultyAI) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(this);

        GridPane popupRoot = new GridPane();
        Scene popupScene = new Scene(popupRoot, 400, 300);

        MGTicTacToe ttt = new MGTicTacToe(playerTurn, difficultyAI);
        int size = MGTicTacToe.getSize();
        Button[][] boardButtons = new Button[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int currentRow = row;
                int currentCol = col;

                Button button = new Button();
                button.setMinSize(100, 100);
                button.setOnAction(e -> handleButtonClickTicTacToe(currentRow, currentCol, boardButtons, ttt));
                boardButtons[row][col] = button;
                popupRoot.add(button, col, row);
            }
        }
        // Maybe in the class actual
        mgResult = new Label("Player's turn");
        popupRoot.add(mgResult, size, 0);
        if (!playerTurn) {
            int rowAI = ttt.getLastOpponentMove()[0];
            int colAI = ttt.getLastOpponentMove()[1];
            boardButtons[rowAI][colAI].setText(ttt.boardPieceToString(rowAI, colAI));
        }
        popupStage.setScene(popupScene);

        popupStage.showAndWait();
    }

    private void handleButtonClickTicTacToe(int row, int col, Button[][] boardButtons, MGTicTacToe ttt) {
        int result = ttt.play(row, col);
        boardButtons[row][col].setText(ttt.boardPieceToString(row, col));
        if (result == MGTicTacToe.getOngoing()) {
            result = ttt.moveAI();
            int rowAI = ttt.getLastOpponentMove()[0];
            int colAI = ttt.getLastOpponentMove()[1];
            boardButtons[rowAI][colAI].setText(ttt.boardPieceToString(rowAI, colAI));
            if (result != MGTicTacToe.getOngoing()) {
                mgResult.setText(ttt.getGameResult());
            }
        } else if (result == MGTicTacToe.getInvalid()) {
            mgResult.setText("INVALID MOVE");
        } else {
            mgResult.setText(ttt.getGameResult());
        }
    }

    // boolean pauseGame (should it be paused while popping up or not) canBePaused
    public void popupGEvent(GEvent gEvent) {
        pausePlayDate(true);
        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(this);
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.setTitle("Game Event");

        Label titleLabel = new Label("Title: " + gEvent.getTitle());
        Label dateLabel = new Label("Date: " + gEvent.getDate());
        Label descriptionLabel = new Label("Description\n" + game.parseGEventText(gEvent.getDescription()));
        Region reg = new Region();

        HBox popupTop = new HBox(titleLabel, reg, dateLabel);
        HBox.setHgrow(reg, Priority.ALWAYS);

        String[] opt = gEvent.getOptions();
        Button[] optButton = new Button[opt.length];
        for (int i = 0; i < opt.length; i++) {
            optButton[i] = new Button(game.parseGEventText(opt[i]));
            Tooltip tooltip = new Tooltip(gEvent.getCommands()[i]);
            Tooltip.install(optButton[i],tooltip);
            optButton[i].setPrefWidth(400); // related to max text?
            optButton[i].setStyle("-fx-background-color: #00FF00;-fx-text-fill: #000000;");
            ;
            // optButton[i].applyCss();
            // HMMMM css not activated?
            int choice = i;
            optButton[i].setOnAction(event -> {
                pausePlayDate(false);
                gEvent.run(choice);
                popupStage.close();
            });
        }
        VBox options = new VBox(optButton);

        options.setSpacing(10);
        VBox popupRoot = new VBox(popupTop, descriptionLabel, options);
        options.setAlignment(Pos.CENTER);

        popupRoot.setSpacing(10);
        StackPane container = new StackPane(popupRoot);
        StackPane.setMargin(popupRoot, new Insets(20));
        Scene popupScene = makeGEventScene(container, popupStage);


        popupStage.setScene(popupScene);
        Platform.runLater(popupStage::showAndWait);
    }

    private Scene makeGEventScene(StackPane container, Stage popupStage) {
        Scene popupScene = new Scene(container);
        final Delta dragDelta = new Delta();
        popupScene.setOnMousePressed(mouseEvent -> {
            dragDelta.x = popupStage.getX() - mouseEvent.getScreenX();
            dragDelta.y = popupStage.getY() - mouseEvent.getScreenY();
        });
        popupScene.setOnMouseDragged(mouseEvent -> {
            popupStage.setX(mouseEvent.getScreenX() + dragDelta.x);
            popupStage.setY(mouseEvent.getScreenY() + dragDelta.y);
        });
        return popupScene;
    }

    static class Delta { double x, y; }


    public void popupWebNews() {
        File file = new File(GLogic.RESOURCESPATH + "web/news.html");
        String filePath = file.toURI().toString();

        WebView webView = new WebView();
        webView.getEngine().load(filePath);

        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 800, 600));
        popupStage.setTitle("World News");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public void popupWebDesktop() {
        File file = new File(GLogic.RESOURCESPATH + "web/pcdesktop.html");
        String filePath = file.toURI().toString();

        WebView webView = new WebView();
        webView.getEngine().load(filePath);

        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 800, 600));
        popupStage.setTitle("Desktop");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }
    public int popupChess(String cName) {
        pausePlayDate(true);

        String PLAYER = game.getPlayer().getGovernment().toStringMainRuler();
        String OPPONENT = game.getWorldCountries().get(cName).getGovernment().toStringMainRuler();
        File file = new File(GLogic.RESOURCESPATH + "web/chess/chess.html");
        String filePath = file.toURI() + "?player=" + PLAYER + "&opponent=" + OPPONENT + "&src=countrysim";

        WebView webView = new WebView();
        webView.getEngine().load(filePath);
        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 1000, 700));
        popupStage.setTitle("Chess Battle");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.showAndWait();
        pausePlayDate(false);
        //Platform.runLater(popupStage::showAndWait);
        String result = (String) webView.getEngine().executeScript("document.getElementById('javaResult').textContent");
        return parseChessResult(result);
    }
    public int parseChessResult(String in){
        if(in.startsWith("checkmate")){
            String colorLost = in.substring(10);
            TESTING.print(colorLost);
            if(colorLost.equalsIgnoreCase("White")){
                return -8;
            } else if(colorLost.equalsIgnoreCase("Black")){
                return 8;
            } else{
                return -128;
            }
        } else if(in.equalsIgnoreCase("draw")){
            return 0;
        } else if(in.equalsIgnoreCase("cancellable")){
            return -32;
        } else{
            return -256;
        }
    }
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.initOwner(this);
        alert.setTitle(title);
        alert.setHeaderText(game.getPlayerName());
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        // Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        // stage.getIcons().add(new javafx.scene.image.Image("your_icon.png"));
        // alert.getDialogPane().getStyleClass().add("your-custom-style-class");
        ButtonType closeButton = new ButtonType("Close");
        alert.getButtonTypes().add(closeButton);
        //ButtonType.CLOSE
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.setOnCloseRequest(event -> alert.setResult(closeButton));

        alert.showAndWait();
    }

    public WorldMap getMap() {
        return map;
    }

    public void setMap(WorldMap map) {
        this.map = map;
    }

    ///////////////////////
    public void popupUnionPanel(String unionName) {
        Union union = game.getWorld().getUnions().get(unionName);

    }

    public void popupGlobeViewer(int type) {
        Stage popupStage = new GlobeViewer(type);
    }


    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Double showNumberInputDialog(double maxSliderVal) {
        return showNumberInputDialog(0, maxSliderVal);
    }

    private Double showNumberInputDialog(double minSliderVal, double maxSliderVal) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        Slider slider = new Slider(minSliderVal, maxSliderVal, 0);

        Label label = new Label("Enter amount:");
        TextField inputField = new TextField(Double.toString(minSliderVal));
        slider.valueProperty().addListener((observable, oldValue, newValue) -> inputField.setText(newValue.toString()));
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialogStage.close());

        VBox vbox = new VBox(10, slider, label, inputField, okButton);
        vbox.setStyle("-fx-padding: 10px;");
        vbox.setPrefSize(200, 150);

        dialogStage.setScene(new Scene(vbox));
        dialogStage.setTitle("Number Input");
        dialogStage.showAndWait();

        try {
            return Double.parseDouble(inputField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void displayNumberInputResult(double number) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmed");
        //alert.setHeaderText(null);
        alert.setContentText("You entered: " + number);
        alert.showAndWait();
    }

    public void setSelectedProvince(int provId) {
        if (selectedProv != provId) {
            selectedProv = provId;
            changeSelectedProvInfo();
        }

    }

    //
    public void changeSelectedProvInfo() {
        AdmDiv a = game.getWorld().getAdmDiv(selectedProv);
        selectedProvInfo.setText(game.getProvInfo(selectedProv));
        if (a != null) {
            int owner = a.getOwnerId();
            if (isPlayingCountry && (game.getPlayerId() == owner || game.isSubjectOfPlayer(owner))) {
                recruitBuildButton.setVisible(true);
                tableViewBuildings.setVisible(true);
                BuildBuildings.setFromProv(a);
                a.setValuesFromEnumMapSet(tableViewBuildings);
            } else {
                recruitBuildButton.setVisible(false);
                tableViewBuildings.setVisible(false);
            }
        }
    }

    public void setSelectedCountry(int ownerId) {
        selectedCountry = ownerId;
        changeSelectedCountryInfo();
    }


    public void correlateProvinces(SVGProvince[] mapSVG) {
        game.getWorld().initiateProvinces(mapSVG);
    }
}
