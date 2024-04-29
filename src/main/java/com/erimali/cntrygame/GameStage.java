package com.erimali.cntrygame;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;

import com.erimali.cntrymilitary.MilUnitData;
import com.erimali.minigames.MG2048Stage;
import javafx.application.Platform;
import javafx.beans.value.ObservableIntegerValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

class LimitedSizeList<T> {
    private final int maxSize;
    private final LinkedList<T> list;
    private int index;

    public LimitedSizeList(int maxSize) {
        this.maxSize = maxSize;
        this.list = new LinkedList<>();
    }

    public void add(T el) {
        if (list.size() >= maxSize) {
            list.removeLast();
        }
        list.addFirst(el);
        //reset
        index = list.size() - 1;
    }

    //iterator for speed?
    public T get(int i) {
        if (i < 0 || i >= list.size())
            return null;
        return list.get(i);
    }

    public T getUp() {
        index++;
        index %= list.size();
        return get(index);
    }

    public T getDown() {
        index--;
        if (index < 0)
            index = list.size() - 1;
        return get(index);
    }
}

public class GameStage extends Stage {
    // POP UP WHEN FULLSCREEN PROBLEM
    private final Main application;
    private final Scene gameScene;
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

    private Label selectedCountryInfo;
    private Label selectedProvInfo;

    private Label mgResult;
    //Our country/prov -> 0, Our subjects country/prov, Others' country/prov (taking care if they are subjects themselves)
    //subject in general is like other country...
    private VBox[] countryOptTypes;
    private VBox[] provOptTypes;
    //
    private int prevLeftVBoxInd;
    private TabPane leftGeneral;
    private Tab countryTab;
    private Tab provinceTab;
    //
    private ToggleButton improveRelations;
    private Stage commandLineStage;
    private LimitedSizeList<String> lastCommands;
    private Label infoRelations;
    private Button sendAllianceRequest;

    private Stage gsOptionsStage;
    private Scene[] gsOptionsScenes;
    private TextField saveTextField;
    private Button recruitBuildButton;
    private UnionStage unionStage;
    private VBox formablesPanel;

    public GameStage(Main application) {
        this.application = application;
        setTitle(Main.APP_NAME + " - Game");
        Main.loadGameIcon(this);
        setOnCloseRequest(e -> close());
        setWidth(1280);
        setHeight(720);
        this.selectedCountry = -1;
        this.map = new WorldMap(this);
        this.game = new GLogic(this);

        BorderPane gameLayout = createGameLayout();
        changeDate(game.inGDateInfo());
        CommandLine.setGameStage(this);

        gameScene = new Scene(gameLayout);
        setScene(gameScene);
        loadGameStageCSS();
        initFullScreen();
        //Initiate
        game.getWorld().initiateProvinces(map.getMapSVG());
    }

    //Load game
    public GameStage(Main application, GLogic game) {
        this.application = application;
        setTitle(Main.APP_NAME + " - Game");
        Main.loadGameIcon(this);
        setOnCloseRequest(e -> close());
        setWidth(1280);
        setHeight(720);
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

        CommandLine.setGameStage(this);
        gameScene = new Scene(gameLayout);
        setScene(gameScene);
        loadGameStageCSS();
        initFullScreen();

        //Correlate
        game.getWorld().correlateProvinces(map.getMapSVG());
    }

    private void loadGameStageCSS() {
        URL cssURL = getClass().getResource("css/gameStage.css");
        if (cssURL != null)
            gameScene.getStylesheets().add(cssURL.toExternalForm());
    }

    private void initFullScreen() {

        this.setFullScreen(GOptions.isFullScreen());
        this.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        //this.setFullScreenExitHint("F11 to toggle fullscreen");
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
        speedUp.setPrefHeight(16);
        speedUp.setPrefWidth(26);
        speedUp.setStyle("-fx-background-color: lightblue; -fx-shape: 'M 0 50 L 25 0 L 50 50 Z'");
        speedUp.setOnAction(e -> {
            game.increaseSpeed();
            speedData.setText(String.valueOf(game.getSpeed()));
        });

        Button speedDown = new Button("-");
        speedDown.setPrefHeight(16);
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
        ZoomableScrollPane scrollPane = map.start();
        gameLayout.setCenter(scrollPane);

        // LEFT
        // close X
        //ScrollPane leftScrollPane = new ScrollPane();
        //leftScrollPane.setMinWidth(240);
        selectedCountryInfo = new Label();
        selectedProvInfo = new Label();
        initVBoxLeftOptions();
//selectedCountryInfo
        //selectedProvInfo
        leftGeneral = new TabPane();
        leftGeneral.setMinWidth(240);
        countryTab = new Tab("Country", countryOptTypes[0]);
        provinceTab = new Tab("Adm-Division", provOptTypes[0]);
        leftGeneral.getTabs().addAll(countryTab, provinceTab);
        leftGeneral.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        countryTab.getContent().setVisible(false);
        provinceTab.getContent().setVisible(false);

        //leftScrollPane.setContent(leftGeneral);
        gameLayout.setLeft(leftGeneral);

        //Bottom
        //hoveringCountry = new Label("Hovering");
        //HBox hBottomLeft = new HBox(hoveringCountry);

        ToolBar toolBarBottom = makeBottomToolbar();
        //Region regBottom = new Region();
        //HBox bottom = new HBox(hBottomLeft, regBottom, toolBarBottom);
        //HBox.setHgrow(regBottom, Priority.ALWAYS);

        gameLayout.setBottom(toolBarBottom);
        // maybe Button[], setOnAction( i ...);


        TabPane tabPaneRight = makeRightTabPane();

        Region regRight = new Region();
        Label label = new Label("Map modes");
        FlowPane mapChoices = makeRightMapModesFlowPane(scrollPane);

        VBox vBoxRight = new VBox(tabPaneRight, regRight, label, mapChoices);
        vBoxRight.setPadding(new Insets(4));
        VBox.setVgrow(regRight, Priority.SOMETIMES);
        VBox.setVgrow(tabPaneRight, Priority.SOMETIMES);

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
                    //if fullscreen stage problem
                    break;
                case KeyCode.F11:
                    setFullScreen(GOptions.toggleFullScreen());
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

    private ImageView loadImgView(String path, double height, double width) {
        try {
            URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                ImageView imgView = new ImageView(imgURL.toExternalForm());
                imgView.setFitHeight(height);
                imgView.setFitWidth(width);
                return imgView;
            } else {
                ErrorLog.logError("INVALID IMG PATH: " + path);
                return null;
            }
        } catch (Exception e) {
            ErrorLog.logError(e);
            return null;
        }
    }

    private ToolBar makeBottomToolbar() {
        Button gsComputer = new Button("Computer");
        Button gsNews = new Button("News");
        Button gsOptions = new Button("Settings");
        ImageView imgComputer = loadImgView("img/monitor_with_button.png", 24, 24);
        if (imgComputer != null) {
            gsComputer.setGraphic(imgComputer);
            Tooltip.install(gsComputer, new Tooltip(gsComputer.getText()));
            gsComputer.setText(null);
        }
        ImageView imgNews = loadImgView("img/design_view_dark.png", 24, 24);
        if (imgNews != null) {
            gsNews.setGraphic(imgNews);
            Tooltip.install(gsNews, new Tooltip(gsNews.getText()));
            gsNews.setText(null);
        }
        ImageView imgSettings = loadImgView("img/settings.png", 24, 24);
        if (imgSettings != null) {
            gsOptions.setGraphic(imgSettings);
            Tooltip.install(gsOptions, new Tooltip(gsOptions.getText()));
            gsOptions.setText(null);
        }

        gsComputer.setOnAction(e -> popupWebDesktop());
        gsNews.setOnAction(e -> popupWebNews());
        gsOptions.setOnAction(e -> showGameStageOptions());
        Region tReg = new Region();
        ToolBar toolBar = new ToolBar(gsComputer, gsNews, tReg, gsOptions);
        HBox.setHgrow(tReg, Priority.ALWAYS);
        return toolBar;
    }

    private FlowPane makeRightMapModesFlowPane(ZoomableScrollPane scrollPane) {
        FlowPane flowPane = new FlowPane(Orientation.HORIZONTAL, 10, 10);
        flowPane.setPrefWidth(240);
        /*
        Button zoomIn = new Button();
        zoomIn.setGraphic(WorldMap.loadSVGPath("img/zoom_in.svg"));
        zoomIn.setOnAction(e -> scrollPane.zoomIn());
        Button zoomOut = new Button();
        zoomOut.setGraphic(WorldMap.loadSVGPath("img/zoom_out.svg"));
        zoomOut.setOnAction(e -> scrollPane.zoomOut());

         */
        int mapModesSize = WorldMap.getMaxMapModes();
        Button[] mapModes = new Button[mapModesSize];
        for (int i = 0; i < mapModesSize; i++) {
            mapModes[i] = new Button(WorldMap.getMapModeName(i));
            int finalI = i;
            mapModes[i].setOnAction(event -> map.switchMapMode(finalI));
        }
        //flowPane.getChildren().addAll(zoomIn, zoomOut);
        flowPane.getChildren().addAll(mapModes);

        return flowPane;
    }

    private TabPane makeRightTabPane() {
        TabPane tabPane = new TabPane();

        tabPane.setMinWidth(280);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab[] tabs = new Tab[3];
        tabs[0] = makeTabMilitary();
        tabs[1] = makeTabBuildings();
        tabs[2] = makeTabUnions();


        tabPane.getTabs().addAll(tabs);
        return tabPane;
    }

    private TableView<BuildBuildings.BuildBuilding> tableViewBuildings;

    private Tab makeTabMilitary() {
        Label unitInfo = new Label();
        recruitBuildButton = new Button("Recruit");
        TreeView<MilUnitData> treeUnitTypes = game.makeTreeViewUnitTypes();

        treeUnitTypes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && newValue != oldValue) {
                        MilUnitData selUnit = newValue.getValue();
                        unitInfo.setText(selUnit.toStringLong());
                        unitInfo.setTooltip(new Tooltip(selUnit.getDesc()));
                        recruitBuildButton.setText(selUnit.isVehicle() ? "Build" : "Recruit");
                    }
                }
        );
        recruitBuildButton.setOnAction(e -> {
            TreeItem<MilUnitData> selectedItem = treeUnitTypes.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                MilUnitData selUnit = selectedItem.getValue();
                game.makeMilUnit(game.getPlayerId(), selectedProv, selUnit);
            }
        });
        VBox vBox = new VBox(treeUnitTypes, recruitBuildButton, unitInfo);
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

    private ListView<String> listViewUnions;

    public String getSelectedUnion() {
        return listViewUnions.getSelectionModel().getSelectedItem();
    }

    public Union getSelectedUnionFromWorld() {
        String sel = getSelectedUnion();
        if (sel != null)
            return game.getWorld().getUnion(sel);
        return null;
    }

    private Tab makeTabUnions() {
        unionStage = new UnionStage();
        unionStage.initOwner(this);
        unionStage.initModality(Modality.NONE);
        Button joinButton = new Button();
        Button openPanel = new Button("Open Panel");
        openPanel.setVisible(false);
        listViewUnions = UnionStage.makeListViewUnionsString(game.getWorld().getUnions());

        listViewUnions.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.equals(oldValue)) {
                        Union u = game.getWorld().getUnion(newValue);
                        if (map.getMapMode() == 2) {
                            map.paintMapUnions(u);
                        }
                        if (u.containsCountry(game.getPlayerId())) {
                            joinButton.setText("Leave");
                            openPanel.setVisible(true);
                        } else {
                            joinButton.setText("Join");
                            openPanel.setVisible(false);
                        }
                    }
                }
        );

        joinButton.setOnAction(e -> {
            String selectedItem = listViewUnions.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Union u = game.getWorld().getUnion(selectedItem);
                if (u.containsCountry(game.getPlayerId())) {
                    if (u.applyToLeave(game.getPlayerId())) {

                        joinButton.setText("Join");
                        openPanel.setVisible(false);
                    }
                } else {
                    if (u.applyToJoin(game.getPlayerId())) {

                        joinButton.setText("Leave");
                        openPanel.setVisible(true);
                    }
                }
            }
        });
        openPanel.setOnAction(e -> {
            String selectedItem = listViewUnions.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Union u = game.getWorld().getUnion(selectedItem);
                if (u.containsCountry(game.getPlayerId())) {
                    unionStage.setFromUnion(u);
                    unionStage.show();
                }
            }
        });

        HBox hBox = new HBox(joinButton, openPanel);
        VBox vBox = new VBox(listViewUnions, hBox);
        Tab tab = new Tab("Unions", vBox);
        return tab;
    }

    private Scene makeGSOptionsDefScene() {
        Button saveOptions = new Button("Save");
        saveOptions.setOnAction(e -> {
            saveTextField.setText(getCurrDefSaveGameName());
            gsOptionsStage.setScene(gsOptionsScenes[1]);
        });

        Button exitToMainButton = new Button("Exit to main menu");
        exitToMainButton.setOnAction(e -> exitToMain());
        VBox vBox = new VBox(10, saveOptions, exitToMainButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(20));
        Scene scene = new Scene(vBox, 400, 300);
        return scene;
    }

    private void exitToMain() {
        if (isPlayingCountry) {
            String autosave = "autosave-" + getCurrDefSaveGameName();
            if (SaveGame.alertConfirmation("Save", "Save as " + autosave + " before closing?")) {
                SaveGame.saveGame(autosave, game);
            }
        }
        application.closeGameOpenPrimary();
    }

    private void changeGSOptionsScene(int i) {
        if (i >= 0 && i < gsOptionsScenes.length)
            gsOptionsStage.setScene(gsOptionsScenes[i]);
        else if (i == -1) {
            //go to Main...
            // -2 for save (String currentSaveName) & exit
        }
    }

    public String getCurrDefSaveGameName() {
        return isPlayingCountry ? game.getPlayer().getIso2() + "-" + game.inGDateInfo('_') : "";
    }

    private Scene makeSaveSceneOptions() {
        saveTextField = new TextField();
        Button saveSubmit = new Button("Enter");
        saveTextField.setPrefWidth(240);
        saveSubmit.setPrefWidth(60);
        HBox hBox = new HBox(saveTextField, saveSubmit);
        HBox.setHgrow(saveTextField, Priority.ALWAYS);
        ListView<String> listView = new ListView<>(SaveGame.saves);
        VBox vBox = new VBox(listView, hBox); //add listview
        VBox.setVgrow(listView, Priority.ALWAYS);
        saveTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveGameFromTextField();
            }
        });
        saveSubmit.setOnAction(event -> saveGameFromTextField());
        saveTextField.setPromptText("Save-game name");
        Scene scene = new Scene(vBox);

        return scene;
    }

    private void saveGameFromTextField() {
        if (!saveTextField.getText().isBlank()) {
            try {
                String save = saveTextField.getText();
                SaveGame.saveGame(save, game);
                saveTextField.setText("");
            } catch (Exception e) {
            }
        }
    }

    private void showGameStageOptions() {
        gsOptionsStage.show();
        gsOptionsStage.requestFocus();
    }

    private void initVBoxLeftOptions() {
        this.countryOptTypes = new VBox[2];
        countryOptTypes[0] = makeVBoxPlayerOptions();
        //.setSpacing(10);
        countryOptTypes[1] = makeVBoxCountryOptions();

        this.provOptTypes = new VBox[2];
        provOptTypes[0] = makeVBoxPlayerProvOptions();
        provOptTypes[1] = makeVBoxOtherProvOptions();
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
        for (Scene scene : gsOptionsScenes) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    WindowEvent.fireEvent(stage, new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                    //event.consume();
                }
            });
        }
        return stage;
    }

    private Stage makeCommandLineStage() {
        lastCommands = new LimitedSizeList<>(10);
        // Autocomplete for already entered commands (?) !ControlsFX!
        TextField commandLine = new TextField();
        Button commandSubmit = new Button("Enter");
        commandLine.setPrefWidth(240);
        commandSubmit.setPrefWidth(60);
        HBox commandHBox = new HBox(commandLine, commandSubmit);
        Label commandResult = new Label();
        VBox commandVBox = new VBox(commandHBox, commandResult);
        HBox.setHgrow(commandLine, Priority.ALWAYS);
        commandLine.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.ENTER) {
                executeCommand(commandLine, commandResult);
            } else if (keyCode == KeyCode.ESCAPE) {
                commandLineStage.hide();
            } else if (keyCode == KeyCode.UP) {
                commandLine.setText(lastCommands.getUp());
            } else if (keyCode == KeyCode.DOWN) {
                commandLine.setText(lastCommands.getDown());
            }
        });
        commandSubmit.setOnAction(event -> executeCommand(commandLine, commandResult));
        commandLine.setPromptText("Type your command here");

        Stage commandLineStage = new Stage();
        commandLineStage.setTitle("Command Line");
        commandLineStage.initModality(Modality.APPLICATION_MODAL);
        commandLineStage.initOwner(this);
        commandLineStage.initStyle(StageStyle.UTILITY);
        commandLineStage.setScene(new Scene(commandVBox));
        return commandLineStage;
    }

    private void executeCommand(TextField commandLine, Label commandResult) {
        try {
            String commandText = commandLine.getText();
            if (!commandText.isBlank()) {
                String res = CommandLine.execute(commandText);
                lastCommands.add(commandText);
                commandResult.setText(res);
                commandLine.setText("");
            }
        } catch (Exception e) {
            commandResult.setText(e.toString());
        }
    }

    private VBox makeVBoxPlayerOptions() {
        formablesPanel = makeVBoxListViewFormables();
        TitledPane formables = new TitledPane("Formables", formablesPanel);
        formables.setAnimated(false);
        VBox vBox = new VBox(selectedCountryInfo, formables);

        return vBox;
    }

    private VBox makeVBoxCountryOptions() {
        Label optionsWar = new Label("War");
        Button declareWar = new Button("Declare War");
        declareWar.setOnAction(e -> declareWarOrPeace());
        declareWar.getStyleClass().add("declare-war-button");
        Button sponsorRebels = new Button("Sponsor Rebels");
        sponsorRebels.setOnAction(e -> sponsorRebels());
        sponsorRebels.getStyleClass().add("sponsor-rebels-button");

        VBox vboxWar = new VBox(optionsWar, declareWar, sponsorRebels);
        vboxWar.setSpacing(8);
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
        sendAllianceRequest.getStyleClass().add("alliance-request-button");
        sendAllianceRequest.setOnAction(e -> {
            sendAllianceRequest();
        });
        Button sendDonation = new Button("Send donation");
        sendDonation.setOnAction(e -> {
            sendDonation();
        });
        VBox vboxRelations = new VBox(optionsRelations, improveRelations, sendAllianceRequest, sendDonation);
        vboxRelations.setSpacing(8);
        TitledPane titledPaneWar = new TitledPane("War", vboxWar);
        titledPaneWar.setAnimated(false);
        TitledPane titledPaneRelations = new TitledPane("Relations", vboxRelations);
        titledPaneRelations.setAnimated(false);

        return new VBox(selectedCountryInfo, titledPaneWar, titledPaneRelations);
    }


    private VBox makeVBoxPlayerProvOptions() {
        Button raiseFunds = new Button("Raise municipal funds");
        Button investInProv = new Button("Invest");
        VBox vBox = new VBox(raiseFunds, investInProv);
        vBox.setSpacing(8);
        TitledPane titledPane = new TitledPane("", vBox);
        titledPane.setAnimated(false);
        return new VBox(selectedProvInfo, titledPane);
    }

    private VBox makeVBoxOtherProvOptions() {
        return new VBox(selectedProvInfo);
    }
    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////
    // if(game.isSubjectOfPlayer(selectedCountry))


    public void sendDonation() {
        // selected country -> treasury += input
        //max should be governmentBudget (not spent) / 40
        Double result = showNumberInputDialog("Donation", 1000, 10000000);
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
            if (game.breakAlliance(selectedCountry)) {
                showAlert(Alert.AlertType.CONFIRMATION, "Broken alliance", game.getCountry(selectedCountry).getName() + " is not our ally any longer.");
                sendAllianceRequest.setText("Alliance request");
            } else {
                showAlert(Alert.AlertType.CONFIRMATION, "Couldn't break alliance", game.getCountry(selectedCountry).getName() + " is still our ally.");
            }
        } else {
            if (game.sendAllianceRequest(selectedCountry)) {
                showAlert(Alert.AlertType.CONFIRMATION, "Accepted alliance request", game.getCountry(selectedCountry).getName() + " has accepted our alliance request.");
                sendAllianceRequest.setText("Break alliance");
            } else {
                showAlert(Alert.AlertType.WARNING, "Denied alliance request", game.getCountry(selectedCountry).getName() + " has denied our alliance request.");
            }
        }
    }

    public void sendMakeSubjectRequest() {

    }

    private void declareWarOrPeace() {
        if (game.getPlayer().isAtWarWith(selectedCountry)) {
            negotiatePeace();
        } else {
            declareWar();
        }
    }

    public void negotiatePeace() {

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

        countryTab.getContent().setVisible(true);
        provinceTab.getContent().setVisible(true);

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
        countryTab.setContent(countryOptTypes[i]);
        provinceTab.setContent(provOptTypes[i]);
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
                        Double money = showNumberInputDialog("Sponsor Rebels", 1000000, 100000000);
                        if (money == null) {
                            dialog.setHeaderText("Money is a number!");
                            event.consume();
                        } else {
                            double amount = money.doubleValue();
                            //TESTING.print(amount);
                            //game.sponsorRebels(rt, selectedCountry, money);
                            GameAudio.playShortSound("low-impact.mp3");
                        }
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
            Tooltip.install(optButton[i], tooltip);
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


    static class Delta {
        double x, y;
    }


    public void popupWebNews() {
        File file = new File(GLogic.RESOURCESPATH + "web/news.html");
        String filePath = file.toURI() + "?gameid=" + game.getUniqueId();

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
        String filePath = file.toURI() + "?gameid=" + game.getUniqueId();
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine webEngine = webView.getEngine();
        webEngine.load(filePath);
        webEngine.setUserAgent("countrysim"); //in javascript navigator.userAgent
        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 800, 600));
        popupStage.setTitle("Desktop");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public int popupMG2048() {
        MG2048Stage mg2048 = new MG2048Stage();
        mg2048.initOwner(this);
        mg2048.showAndWait();
        return mg2048.getScore();
    }

    public int popupChess(String cName) {
        TESTING.print(cName);
        return popupChess(CountryArray.getIndexOrInt(cName));
    }

    public int popupChess(int cId) {
        pausePlayDate(true);

        String PLAYER = game.getPlayer().getGovernment().toStringMainRuler();
        String OPPONENT = game.getWorldCountries().get(cId).getGovernment().toStringMainRuler();
        File file = new File(GLogic.RESOURCESPATH + "web/chess/chess.html");
        String filePath = file.toURI() + "?player=" + PLAYER + "&opponent=" + OPPONENT + "&src=countrysim";

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(filePath);
        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 1000, 700));
        popupStage.setTitle("Chess Battle");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.showAndWait();
        pausePlayDate(false);
        //Platform.runLater(popupStage::showAndWait);
        String result = (String) webEngine.executeScript("document.getElementById('javaResult').textContent");
        return parseChessResult(result);
    }

    public int parseChessResult(String in) {
        if (in.startsWith("checkmate")) {
            String colorLost = in.substring(10);
            TESTING.print(colorLost);
            if (colorLost.equalsIgnoreCase("White")) {
                return -8;
            } else if (colorLost.equalsIgnoreCase("Black")) {
                return 8;
            } else {
                return -128;
            }
        } else if (in.equalsIgnoreCase("draw")) {
            return 0;
        } else if (in.equalsIgnoreCase("cancellable")) {
            return -32;
        } else {
            return -256;
        }
    }

    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.initOwner(this);
        alert.setTitle(title);
        //alert.setHeaderText(game.getPlayerName());
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        // Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        // stage.getIcons().add(new javafx.scene.image.Image("icon.png"));
        // alert.getDialogPane().getStyleClass().add("custom-style-class");
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
        popupStage.initOwner(this);
        popupStage.show();

    }


    private void showErrorPopup(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Double showNumberInputDialog(double maxSliderVal) {
        return showNumberInputDialog("Number Input", 0, maxSliderVal);
    }

    private Double showNumberInputDialog(String title) {
        return showNumberInputDialog(title, 100000, game.getPlayer().getTreasury());
    }

    private Double showNumberInputDialog(String title, double minSliderVal, double maxSliderVal) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        Slider slider = new Slider(minSliderVal, maxSliderVal, 0);
        Label label = new Label("Enter amount:");
        TextField inputField = new TextField(Double.toString(minSliderVal));
        slider.valueProperty().addListener((observable, oldValue, newValue) -> inputField.setText(String.valueOf(newValue.intValue())));
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialogStage.close());

        VBox vbox = new VBox(10, slider, label, inputField, okButton);
        //vbox.setStyle("-fx-padding: 10px;");
        vbox.setPadding(new Insets(10));
        vbox.setPrefSize(200, 150);

        dialogStage.setScene(new Scene(vbox));
        dialogStage.setTitle(title);
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

    public VBox makeVBoxListViewFormables() {
        //only if is Available...
        ListView<CFormable> listViewFormables = new ListView<>(game.getPlayerFormables());
        listViewFormables.setPrefWidth(160);
        listViewFormables.setPrefHeight(240);
        //Check Requirements (if evenSubjects)
        //Form -> on form success -> show bonuses
        Label requirements = new Label();
        Button form = new Button("Form");
        //form.setVisible(false);
        listViewFormables.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && !newValue.equals(oldValue)) {
                        requirements.setText(newValue.toStringRequirements(game.getWorld().getProvinces(), game.getWorld().getInitialProvinces()));
                    }
                }
        );

        form.setOnAction(e -> {
            CFormable selectedItem = listViewFormables.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                if (selectedItem.formCountry(game.getWorld(), game.getPlayer())) {
                    countryName.setText(game.getPlayerName());
                    game.getPlayer().setIsFormed(selectedItem);
                    game.getPlayerFormables().remove(selectedItem);
                    requirements.setText("");
                    showAlert(Alert.AlertType.INFORMATION, "Formation success", "You formed the country of: " + selectedItem + ". \nBenefits: " + selectedItem.toStringCommands());
                } else {
                    showAlert(Alert.AlertType.WARNING, "Cannot form", "You cannot form it yet.");
                }

            }
        });

        VBox vBox = new VBox(listViewFormables, requirements, form);
        return vBox;
    }

    public void setSelectedCountry(int ownerId) {
        selectedCountry = ownerId;
        changeSelectedCountryInfo();
    }

    public void setSelectedCountry(String owner) {
        selectedCountry = CountryArray.getIndex(owner);
        changeSelectedCountryInfo();
    }

    public void correlateProvinces(SVGProvince[] mapSVG) {
        game.getWorld().initiateProvinces(mapSVG);
    }
}
