package com.erimali.cntrygame;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.erimali.cntrymilitary.MilUnitData;
import com.erimali.cntrymilitary.Military;
import com.erimali.minigames.MG2048Stage;
import com.erimali.minigames.MGSnakeStage;
import com.erimali.minigames.MGTicTacToe;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.controlsfx.control.*;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

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
        if (list.isEmpty())
            return null;
        index++;
        index %= list.size();
        return get(index);
    }

    public T getDown() {
        if (list.isEmpty())
            return null;
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
    private Label playerNameLabel;
    private Label treasuryLabel;
    private Label manpowerLabel;
    private Label globalRespectLabel;
    private Label dateLabel;
    private ToggleButton pauseButton;
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
    private Node toolBarReg;

    private HBox hTopCenter;
    private Tooltip hTopCenterTooltip;

    private TreeView<MilUnitData> treeUnitTypes;
    private BuildBuildings buildBuildings;
    private TableView<BuildBuildings.BuildBuildingTask> tableViewBuildings;
    private ToggleButton[] toggleButtonsConscriptRate;
    private Technology.MilResearchUnitsStage milResearchUnitsStage;
    private ObservableList<GovPolicy> observableListGovPolicies;
    private CheckListView<GovPolicy> checkListViewGovPolicies;
    private PeaceNegotiationStage peaceNegotiationStage;
    private Button declareWarButton;

    public GameStage(Main application) {
        this.application = application;
        setTitle(Main.APP_NAME + " - Game");
        Main.loadGameIcon(this);
        setOnCloseRequest(e -> close());
        setWidth(1280);
        setHeight(720);
        this.buildBuildings = new BuildBuildings(this);
        this.selectedCountry = -1;
        this.game = new GLogic(this);
        this.map = new WorldMap(this);

        BorderPane gameLayout = createGameLayout();
        changeDateLabel(game.inGDateInfo());
        CommandLine.setGameStage(this);

        gameScene = new Scene(gameLayout);
        setScene(gameScene);
        loadGameStageCSS();
        initFullScreen();
        //Initiate
        game.getWorld().initiateProvinces(map.getMapSVG());
        makeWorldMapWorldUnison();
    }


    //Load game
    public GameStage(Main application, GLogic game) {
        this.application = application;
        setTitle(Main.APP_NAME + " - Game");
        Main.loadGameIcon(this);
        setOnCloseRequest(e -> close());
        setWidth(1280);
        setHeight(720);
        this.buildBuildings = new BuildBuildings(this);
        this.selectedCountry = -1;
        game.setGameStage(this);
        game.startTimer();
        this.game = game;
        this.map = new WorldMap(this);
        game.loadBaseEvents();
        game.loadAllUnitData();
        game.correlateAllUnitData();
        BorderPane gameLayout = createGameLayout();
        changeDateLabel(game.inGDateInfo());
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
        makeWorldMapWorldUnison();
    }

    private void makeWorldMapWorldUnison() {
        if (map != null && game != null) {
            map.makeUpdateTextCountriesNames(game.getWorld().getCountries());
            //!!!!!!!!!!!!!!!!!!!!
            map.bruteForceDijkstraFix();
        }
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
            playerNameLabel.setText(game.getPlayer().getName());
            //map.setPlayerCountry(game.getPlayerId());
            chooseCountryButton.setVisible(false);
            changeDateLabel(game.inGDateInfo());
        }
    }

    private HBox makeGameSpeedHBox() {
        Label speedData = new Label("1.0");
        speedData.setMinWidth(24);
        speedData.setAlignment(Pos.CENTER_RIGHT);
        speedData.setTextAlignment(TextAlignment.RIGHT);
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
        //gameLayout.setPadding(new Insets(8));
        // TOP
        gameLayout.setCenter(new BorderPane());
        this.playerNameLabel = new Label("Select Country");
        this.dateLabel = new Label("Default Date");
        this.pauseButton = new ToggleButton("Play");
        this.paused = true;
        pauseButton.setMinWidth(48);
        pauseButton.setDisable(true);
        //pauseButton.textProperty().bind(Bindings.when(pauseButton.selectedProperty()).then("Pause").otherwise("Play"));
        makeGraphicalPlayPause();
        pauseButton.setOnAction(e -> paused = !paused);
        Text treasuryText = new Text("Treasury $");
        this.treasuryLabel = new Label();
        Text manpowerText = new Text("Manpower ");
        this.manpowerLabel = new Label();
        Text globalRespectText = new Text("Global Respect ");
        this.globalRespectLabel = new Label();
        chooseCountryButton = new Button("Confirm");
        chooseCountryButton.setOnAction(e -> startGame());
        HBox hTopLeft = new HBox(playerNameLabel, chooseCountryButton);
        hTopLeft.setSpacing(8);
        Region regTopCenter = new Region();
        regTopCenter.setMinWidth(16);
        hTopCenter = new HBox(treasuryText, treasuryLabel, regTopCenter, manpowerText, manpowerLabel, globalRespectText, globalRespectLabel);

        HBox hGameSpeed = makeGameSpeedHBox();
        HBox hTopRight = new HBox(pauseButton, dateLabel, hGameSpeed);
        hTopRight.setSpacing(8);

        hTopCenter.setStyle("-fx-alignment: center;");
        hTopCenterTooltip = new Tooltip();
        Tooltip.install(hTopCenter, hTopCenterTooltip);
        BorderPane bpTop = new BorderPane();
        bpTop.setLeft(hTopLeft);
        bpTop.setCenter(hTopCenter);
        bpTop.setRight(hTopRight);
        gameLayout.setTop(bpTop);
        // CENTER
        ZoomableScrollPane scrollPane = map.getScrollPane();
        gameLayout.setCenter(scrollPane);

        // LEFT
        selectedCountryInfo = new Label();
        selectedProvInfo = new Label();
        initVBoxLeftOptions();
        leftGeneral = new TabPane();
        leftGeneral.setMinWidth(240);
        Tab infoTab = new Tab("Info", makeGeneralInfoVBox());
        countryTab = new Tab("Country", countryOptTypes[0]);
        provinceTab = new Tab("Adm-Division", provOptTypes[0]);
        leftGeneral.getTabs().addAll(infoTab, countryTab, provinceTab);
        leftGeneral.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        countryTab.getContent().setVisible(false);
        provinceTab.getContent().setVisible(false);

        gameLayout.setLeft(leftGeneral);

        //BOTTOM
        ToolBar toolBarBottom = makeBottomToolbar();
        gameLayout.setBottom(toolBarBottom);

        //RIGHT
        TabPane tabPaneRight = makeRightTabPane();

        Region regRight = new Region();
        Label label = new Label("Map modes");
        FlowPane mapChoices = makeRightMapModesFlowPane(scrollPane);

        VBox vBoxRight = new VBox(tabPaneRight, regRight, label, mapChoices);
        vBoxRight.setPadding(new Insets(4));
        VBox.setVgrow(regRight, Priority.SOMETIMES);
        VBox.setVgrow(tabPaneRight, Priority.SOMETIMES);

        gameLayout.setRight(vBoxRight);
        SingleSelectionModel<Tab> leftGeneralSelectionModel = leftGeneral.getSelectionModel();
        SingleSelectionModel<Tab> tabPaneRightSelectionModel = tabPaneRight.getSelectionModel();
        gameLayout.setOnKeyPressed(event -> {
            // ` -> commandLine
            switch (event.getCode()) {
                case KeyCode.F1:
                    leftGeneralSelectionModel.select(0);
                    break;
                case KeyCode.F2:
                    leftGeneralSelectionModel.select(1);
                    break;
                case KeyCode.F3:
                    leftGeneralSelectionModel.select(2);
                    break;
                case KeyCode.F5:
                    tabPaneRightSelectionModel.select(0);
                    break;
                case KeyCode.F6:
                    tabPaneRightSelectionModel.select(1);
                    break;
                case KeyCode.F7:
                    tabPaneRightSelectionModel.select(2);
                    break;
                case KeyCode.F8:
                    tabPaneRightSelectionModel.select(3);
                    break;
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

    public void makeGraphicalPlayPause() {
        pauseButton.setText("");
        Glyph play = getGlyph(FontAwesome.Glyph.PLAY);
        Glyph pause = getGlyph(FontAwesome.Glyph.PAUSE);
        pauseButton.graphicProperty().bind(Bindings.when(pauseButton.selectedProperty())
                .then(pause)
                .otherwise(play));
    }

    private SplitPane makeGeneralInfoVBox() {
        selectedCountryInfo.setTextAlignment(TextAlignment.CENTER);
        selectedProvInfo.setTextAlignment(TextAlignment.CENTER);
        SplitPane splitPaneInfo = new SplitPane(selectedCountryInfo, selectedProvInfo);
        splitPaneInfo.setOrientation(Orientation.VERTICAL);

        return splitPaneInfo;
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
    private void makeImgViewButton(ImageView imgView, Button button){
        if(imgView != null){
            button.setGraphic(imgView);
            Tooltip.install(button, new Tooltip(button.getText()));
            button.setText(null);
        }
    }
    private ToolBar makeBottomToolbar() {
        Button gsComputer = new Button("Computer");
        Button gsNews = new Button("News");
        Button gsWorld = new Button("World");
        Button gsOptions = new Button("Settings");
        ImageView imgComputer = loadImgView("img/monitor_with_button.png", 24, 24);
        makeImgViewButton(imgComputer, gsComputer);
        ImageView imgNews = loadImgView("img/design_view_dark.png", 24, 24);
        makeImgViewButton(imgNews, gsNews);
        ImageView imgWorld = loadImgView("img/globe_earth.png", 24, 24);
        makeImgViewButton(imgWorld, gsWorld);
        ImageView imgOptions = loadImgView("img/settings.png", 24, 24);
        makeImgViewButton(imgOptions, gsOptions);

        gsComputer.setOnAction(e -> popupWebDesktop());
        gsNews.setOnAction(e -> popupWebNews());
        gsWorld.setOnAction(e -> popupWorldInfo());
        gsOptions.setOnAction(e -> showGameStageOptions());

        if (GOptions.isDebugMode())
            toolBarReg = new HBox(8, new Text("Prov ID:"), new Label());
        else
            toolBarReg = new Region();
        ToolBar toolBar = new ToolBar(gsComputer, gsNews, toolBarReg, gsWorld, gsOptions);
        HBox.setHgrow(toolBarReg, Priority.ALWAYS);
        return toolBar;
    }

    private void popupWorldInfo() {
        World world = game.getWorld();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("World info");
        Button viewGlobe = new Button("View 3D");
        viewGlobe.setOnAction(e -> popupGlobeViewer(0));
        alert.setGraphic(viewGlobe);
        alert.setHeaderText(world.toString());
        alert.setContentText(world.toStringLongRest());
        alert.show();
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
        ToggleButton[] mapModes = new ToggleButton[mapModesSize];
        for (int i = 0; i < mapModesSize; i++) {
            mapModes[i] = new ToggleButton(WorldMap.getMapModeName(i));
            int finalI = i;
            mapModes[i].setOnAction(event -> map.switchMapMode(finalI));
        }
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(mapModes);
        mapModes[0].setSelected(true);
        //flowPane.getChildren().addAll(zoomIn, zoomOut); // in vBox... but problem
        flowPane.getChildren().addAll(mapModes);

        CheckBox viewNamingLabels = new CheckBox("Labels");
        viewNamingLabels.setSelected(true);
        viewNamingLabels.setOnAction(e -> map.toggleLabelsCountryNamesVisibility());
        flowPane.getChildren().add(viewNamingLabels);
        return flowPane;
    }

    private TabPane makeRightTabPane() {
        TabPane tabPane = new TabPane();

        tabPane.setMinWidth(280);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab[] tabs = new Tab[4];
        tabs[0] = makeTabMilitary();
        tabs[1] = makeTabBuildings();
        tabs[2] = makeTabUnions();
        tabs[3] = makeTabSubjects();

        tabPane.getTabs().addAll(tabs);
        return tabPane;
    }

    private Tab makeTabMilitary() {
        //Label unitInfo = new Label();
        TextArea unitInfo = new TextArea();
        unitInfo.setEditable(false);
        unitInfo.setWrapText(true);
        unitInfo.setPrefWidth(240);
        recruitBuildButton = new Button("Recruit");
        treeUnitTypes = game.makeTreeViewUnitTypesBasic();

        treeUnitTypes.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null && newValue != oldValue) {
                        MilUnitData selUnit = newValue.getValue();
                        unitInfo.setText(selUnit.toStringLong());
                        //unitInfo.setTooltip(new Tooltip(selUnit.getDesc()));
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
        VBox.setVgrow(unitInfo, Priority.ALWAYS);
        recruitBuildButton.setVisible(false);
        Tab tab = new Tab("Military", vBox);

        return tab;
    }

    //2-8 months
    private Tab makeTabBuildings() {
        tableViewBuildings = buildBuildings.makeTableView();
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

    private Tab makeTabSubjects() {


        Tab tab = new Tab("Subjects");
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
        observableListGovPolicies = FXCollections.observableArrayList(
                Arrays.stream(GovPolicy.values()).filter(GovPolicy::isRemovable).collect(Collectors.toList()));
        checkListViewGovPolicies = new CheckListView<>(observableListGovPolicies);
        checkListViewGovPolicies.setPrefHeight(128);
        checkListViewGovPolicies.setPrefWidth(160);
        TitledPane govPolicies = new TitledPane("Government Policies", checkListViewGovPolicies);
        Label conscriptRateLabel = new Label("Conscriptable population");
        toggleButtonsConscriptRate = makeToggleButtonsConscriptRate();

        SegmentedButton conscriptRateSegmentedButton = new SegmentedButton(toggleButtonsConscriptRate);
        milResearchUnitsStage = new Technology.MilResearchUnitsStage(game.getUnitTypes());
        Button researchPanelButton = new Button("Research Panel");
        researchPanelButton.setOnAction(e -> popupMilResearchPanel());
        VBox vBoxMilPol = new VBox(8, conscriptRateLabel, conscriptRateSegmentedButton, researchPanelButton);
        TitledPane milPolicies = new TitledPane("Military Policies/Research", vBoxMilPol);
        formablesPanel = makeVBoxListViewFormables();
        TitledPane formables = new TitledPane("Formables", formablesPanel);
        formables.setExpanded(false);
        //formables.setAnimated(false);
        VBox vBox = new VBox(govPolicies, milPolicies, formables);
        return vBox;
    }

    private void popupMilResearchPanel() {
        milResearchUnitsStage.updateScene(game.getPlayer().getMilitary());
        milResearchUnitsStage.show();
    }

    private VBox makeVBoxCountryOptions() {
        this.peaceNegotiationStage = new PeaceNegotiationStage(this);
        Label optionsWar = new Label("War");
        declareWarButton = new Button("Declare War");
        declareWarButton.setOnAction(e -> declareWarOrPeace());
        declareWarButton.getStyleClass().add("declare-war-button");
        Button sponsorRebels = new Button("Sponsor Rebels");
        sponsorRebels.setOnAction(e -> sponsorRebels());
        sponsorRebels.getStyleClass().add("sponsor-rebels-button");

        Button dipInsultButton = new Button("Diplomatic Insult");
        dipInsultButton.setOnAction(e -> sendDipInsult());
        VBox vboxWar = new VBox(optionsWar, declareWarButton, sponsorRebels, dipInsultButton);
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

        return new VBox(titledPaneWar, titledPaneRelations);
    }

    private void sendDipInsult() {
        game.sendDipInsult(selectedCountry);
        showAlert(Alert.AlertType.INFORMATION, "Diplomatic insult", "We have sent a diplomatic insult to " + CountryArray.getIndexISO2(selectedCountry) + "\nOur relations have worsened.");
    }


    private VBox makeVBoxPlayerProvOptions() {
        Button raiseFunds = new Button("Raise municipal funds");
        Button investInProv = new Button("Invest");
        VBox vBox = new VBox(raiseFunds, investInProv);
        vBox.setSpacing(8);
        TitledPane titledPane = new TitledPane("", vBox);
        titledPane.setAnimated(false);
        return new VBox(titledPane);
    }

    private VBox makeVBoxOtherProvOptions() {

        return new VBox();
    }
    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////
    // if(game.isSubjectOfPlayer(selectedCountry))

    public ToggleButton[] makeToggleButtonsConscriptRate() {
        double[] defRates = Military.getDefPopConscriptionRates();
        ToggleButton[] toggleButtons = new ToggleButton[defRates.length];
        for (int i = 0; i < defRates.length; i++) {
            toggleButtons[i] = new ToggleButton(defRates[i] * 100 + "%");
            int finalI = i;
            toggleButtons[i].setOnAction(e -> game.setMilPopConscriptRate(finalI));
        }
        return toggleButtons;
    }

    public void sendDonation() {
        // selected country -> treasury += input
        //max should be governmentBudget (not spent) / 40
        Double result = showNumberInputDialog("Donation", 1000, game.getPlayer().getTreasury());
        //game.getPlayer().getEconomy().getGDP()/10
        if (result != null && !result.isNaN()) {
            displayNumberInputResult(result);
            game.giveMoney(selectedCountry, result);
            // country send donation method?
            //game.getPlayer().giveMoney(game.getWorldCountries().get(selectedCountry));
        } else if (result != null) {
            showErrorPopup("Insufficient treasury");
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
        updateDeclareWarButtonText();
    }

    public void updateDeclareWarButtonText() {
        declareWarButton.setText(game.getPlayer().isAtWarWith(selectedCountry) ? "Negotiate peace" : "Declare War");
    }

    //being at war with the same country from multiple wars (!!!)
    public void negotiatePeace() {
        //or List<War> in Military.................
        //one party with other
        //if not main-> peace only for self, war continues...
        List<War> activeWars = game.getWarsWith(selectedCountry);
        if (!activeWars.isEmpty()) {
            if (activeWars.size() == 1) {
                peaceNegotiationStage.setDataFromWar(activeWars.getFirst(), game.getPlayer());
            } else {
                War selWar = popupChooseFromList("Which war",
                        "There are a few wars between you and them, which one to negotiate for?", activeWars);
                if(selWar == null)
                    return;
                peaceNegotiationStage.setDataFromWar(selWar, game.getPlayer());
            }
            peaceNegotiationStage.show();
        }
    }

    public <T> T popupChooseFromList(String title, String desc, List<T> choices) {
        if (choices == null || choices.isEmpty()) {
            throw new IllegalArgumentException("Choices list cannot be null || empty");
        }
        ChoiceDialog<T> dialog = new ChoiceDialog<>(choices.getFirst(), choices);
        dialog.setTitle(title);
        dialog.setHeaderText(desc);
        Optional<T> result = dialog.showAndWait();
        return result.orElse(null);
    }

    public void declareWar() {

        // pop up, choose casus belli (casus affects war objectives)
        //allies?
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Declare War - Casus Belli");

        ListView<CasusBelli> cbListView = War.makeListViewValidatable(game, game.getPlayerId(), selectedCountry, CasusBelli.class);
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
                        if (game.declareWar(selectedCountry, cb)) {
                            GameAudio.playShortSound("big-impact.mp3");
                        } else {
                            showAlert(Alert.AlertType.WARNING, "War declaration failed", "We have already a main war with them");
                        }

                    }
                }
        );


        dialog.showAndWait();
    }

    private void startGame() {
        if (selectedCountry < 0 || isPlayingCountry)
            return;
        pauseButton.setDisable(false);
        game.selectPlayer(selectedCountry);
        playerNameLabel.setText(game.getPlayer().getName());// !!!!!!!!
        playerNameLabel.setOnMouseClicked(e -> {
            setSelectedCountry(game.getPlayerId());
            setSelectedProvince(game.getPlayer().getCapitalId());
        });
        countryTab.getContent().setVisible(true);
        provinceTab.getContent().setVisible(true);

        chooseCountryButton.setVisible(false);
        tableViewBuildings.setVisible(true);
        isPlayingCountry = true;
        treasuryLabel.textProperty().bind(GUtils.stringBindingDoubleCurrency(game.getPlayer().getEconomy().treasuryProperty()));//can also be formated!
        manpowerLabel.textProperty().bind(GUtils.stringBindingLong(game.getPlayer().getMilitary().manpowerProperty()));
        globalRespectLabel.textProperty().bind(game.getPlayer().getDiplomacy().globalRespect().asString());
        correlateCheckListViewGovPolicies(game.getPlayer().getGovernment().getPolicies());
        toggleButtonsConscriptRate[game.getPlayer().getMilitary().getPopConscriptionRateIndex()].setSelected(true);
        milResearchUnitsStage.updatePlayer(game.getPlayer().getMilitary());
        changeSelectedCountryInfo();
        changeSelectedProvInfo();
    }

    public void pausePlayDate() {
        if (!isPlayingCountry)
            return;
        paused = !paused;
        pauseButton.setSelected(!paused);
    }


    public void pausePlayDate(boolean b) {
        if (!isPlayingCountry)
            return;
        /*if (paused && !b) {
            paused = false;
        } else if (!paused && b) {
            paused = true;
        }*/
        paused = b;
        pauseButton.setSelected(!paused);
    }

    public void updateTogglePauseButton() {
        pauseButton.setSelected(!paused);
    }

    public void changeDateLabel(String d) {
        dateLabel.setText(d);
    }

    public Label getPlayerNameLabel() {
        return playerNameLabel;
    }

    public Label getDateLabel() {
        return dateLabel;
    }

    public ToggleButton getPauseButton() {
        return pauseButton;
    }

    public Button getChooseCountryButton() {
        return chooseCountryButton;
    }

    public int getSelectedCountry() {
        return selectedCountry;
    }

    public int getSelectedProv() {
        return selectedProv;
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
        if (i < 0) {
            countryTab.getContent().setVisible(false);
            provinceTab.getContent().setVisible(false);

        } else {
            if (prevLeftVBoxInd < 0) {
                countryTab.getContent().setVisible(true);
                provinceTab.getContent().setVisible(true);
            }
            countryTab.setContent(countryOptTypes[i]);
            provinceTab.setContent(provOptTypes[i]);
        }
        prevLeftVBoxInd = i;
    }

    //make more efficient
    public void changeSelectedCountryInfo() {
        Country selC = game.getWorld().getCountry(selectedCountry);
        if (selC == null) {
            String iso2 = CountryArray.getIndexISO2(map.getMapSVG()[selectedProv].getOwnerId());
            selectedCountryInfo.setText(iso2);
            changeLeftVBoxes(-1);

            return;
        }
        selectedCountryInfo.setText(game.toStringCountry(selectedCountry));
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
                updateDeclareWarButtonText();
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

        ListView<RebelType> rbListView = War.makeListViewValidatable(game, game.getPlayerId(), selectedCountry, RebelType.class);
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
                        Double money = showNumberInputDialog("Sponsor Rebels", 1000000, game.getPlayer().getTreasury());
                        if (money == null) {
                            dialog.setHeaderText("Money is a number!");
                            event.consume();
                        } else if (money.isNaN()) {
                            dialog.setHeaderText("Insufficient treasury.");
                        } else {
                            double amount = money;
                            game.addRebelSponsoring(amount, rt, selectedCountry);
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
    public void popupGEvent(BaseEvent gEvent) {
        pausePlayDate(true);
        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(this);
        popupStage.initStyle(StageStyle.UNDECORATED);
        popupStage.setTitle("Game Event");

        Label titleLabel = new Label("Title: " + gEvent.getTitle());

        //Label dateLabel = new Label("Date: " + gEvent.getDate());
        Label dateLabel = new Label("Date: " + game.inGDateInfo());
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

    static class Delta {
        double x, y;
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

    public void setTooltipEcoTop(double lastMonthBalance) {
        hTopCenterTooltip.setText("Last month balance: " + GUtils.doubleToString(lastMonthBalance));
    }

    public void notificationNews(GNews news) {
        Notifications notification = Notifications.create().title(news.getTitle()).text(news.getDesc()).position(Pos.BOTTOM_RIGHT)
                .hideAfter(Duration.seconds(7)).owner(this);//Doesn't show without owner
        notification.show();//show vs showInformation vs showWarning, GNews type (?)

    }

    public void updateMonthly() {
        Country player = game.getPlayer();
        setTooltipEcoTop(player.getEconomy().getLastMonthBalance());
        if (milResearchUnitsStage.isShowing())
            milResearchUnitsStage.updateScene(player.getMilitary());
        game.updateTreeViewUnitTypes();
    }

    public TreeView<MilUnitData> getTreeUnitTypes() {
        return treeUnitTypes;
    }

    public void popupWebNews() {
        File file = new File(GLogic.RESOURCES_PATH + "web/news.html");
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
        File file = new File(GLogic.RESOURCES_PATH + "web/pcdesktop.html");
        String filePath = file.toURI() + "?gameid=" + game.getUniqueId();
        WebView webView = new WebView();
        webView.setContextMenuEnabled(false);
        WebEngine webEngine = webView.getEngine();
        webEngine.setOnAlert(e -> showAlert(e.getData()));
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

    public int popupMGSnake() {
        MGSnakeStage snake = new MGSnakeStage();
        snake.initOwner(this);
        snake.showAndWait();
        return snake.getScore();
    }

    public int popupChess(String cName) {
        TESTING.print(cName);
        return popupChess(CountryArray.getIndexOrInt(cName));
    }

    public int popupChess(int cId) {
        pausePlayDate(true);

        String PLAYER = game.getPlayer().getGovernment().toStringMainRuler();
        String OPPONENT = game.getWorldCountries().get(cId).getGovernment().toStringMainRuler();
        File file = new File(GLogic.RESOURCES_PATH + "web/chess/chess.html");
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

    public void showAlert(String data) {
        showAlert(Alert.AlertType.NONE, "JS Alert", data);
    }

    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.initOwner(this);
        alert.setTitle(title);
        //alert.setHeaderText(game.getPlayerName());
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);

        ButtonType closeButton;
        if (alert.getButtonTypes().contains(ButtonType.CLOSE)) {
            closeButton = ButtonType.CLOSE;
        } else if (alert.getButtonTypes().contains(ButtonType.CANCEL)) {
            closeButton = ButtonType.CANCEL;
        } else {
            closeButton = new ButtonType("Close");
            alert.getButtonTypes().add(closeButton);
        }


        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.setOnCloseRequest(event -> alert.setResult(closeButton));

        alert.show();
    }

    public WorldMap getMap() {
        return map;
    }

    public void setMap(WorldMap map) {
        this.map = map;
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

    private Double showNumberInputDialog(String title, double minSliderVal, double maxSliderVal) {
        if (maxSliderVal < minSliderVal)
            return Double.NaN;
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
            if (toolBarReg instanceof HBox) {
                Node node = ((HBox) toolBarReg).getChildren().getLast();
                if (node instanceof Label) {
                    ((Label) node).setText(String.valueOf(provId));
                }
            }
            selectedProv = provId;
            changeSelectedProvInfo();
        }
    }

    //
    public void changeSelectedProvInfo() {
        AdmDiv a = game.getWorld().getAdmDiv(selectedProv);
        if (a != null) {
            selectedProvInfo.setText(game.getProvInfo(selectedProv));
            int owner = a.getOwnerId();
            if (isPlayingCountry && (game.getPlayerId() == owner || game.isSubjectOfPlayer(owner))) {
                recruitBuildButton.setVisible(true);
                tableViewBuildings.setVisible(true);
                buildBuildings.setFromProv(a);
                a.setValuesFromEnumMapSet(tableViewBuildings);
            } else {
                recruitBuildButton.setVisible(false);
                tableViewBuildings.setVisible(false);
            }
        } else {
            SVGProvince svgProvince = map.getMapSVG()[selectedProv];
            selectedProvInfo.setText(svgProvince.getId());
            recruitBuildButton.setVisible(false);
            tableViewBuildings.setVisible(false);
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
                    playerNameLabel.setText(game.getPlayerName());
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

    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //Take care if switching countries in game
    public void correlateCheckListViewGovPolicies(ObservableMap<GovPolicy, Integer> observableMap) {
        BooleanProperty changedBoolean = new SimpleBooleanProperty(false);
        observableMap.addListener((MapChangeListener<GovPolicy, Integer>) change -> {
            if (change.wasAdded()) {
                GovPolicy addedPolicy = change.getKey();
                changedBoolean.set(true);
                if (!observableListGovPolicies.contains(addedPolicy)) {
                    observableListGovPolicies.add(addedPolicy);
                    checkListViewGovPolicies.getCheckModel().check(addedPolicy);
                } else {
                    checkListViewGovPolicies.getCheckModel().check(addedPolicy);
                }
                changedBoolean.set(false);

            } else if (change.wasRemoved()) {
                GovPolicy removedPolicy = change.getKey();
                if (!removedPolicy.isRemovable()) {
                    observableListGovPolicies.remove(removedPolicy);
                } else {
                    changedBoolean.set(true);
                    checkListViewGovPolicies.getCheckModel().clearCheck(removedPolicy);
                    changedBoolean.set(false);

                }
            }
        });
        ListChangeListener<GovPolicy> listChangeListener = change -> {
            if (!changedBoolean.get()) {
                while (change.next()) {
                    if (change.wasAdded() || change.wasRemoved()) {
                        for (GovPolicy policy : change.getAddedSubList()) {
                            if (policy.isRemovable()) {
                                changedBoolean.set(true);
                                if (!showConfirmationDialogGovPolicy(policy, true)) {
                                    //checkListViewGovPolicies.getCheckModel().clearCheck(policy);
                                }
                            }
                        }
                        for (GovPolicy policy : change.getRemoved()) {
                            if (policy.isRemovable()) {
                                changedBoolean.set(true);
                                if (!showConfirmationDialogGovPolicy(policy, false)) {
                                    //checkListViewGovPolicies.getCheckModel().check(policy);
                                }
                            } else {
                                //changedBoolean.set(true);
                                //checkListViewGovPolicies.getCheckModel().check(policy);
                                //checkListViewGovPolicies.refresh();
                            }
                        }
                    }
                }
                changedBoolean.set(false);
            }
        };
        checkListViewGovPolicies.getCheckModel().getCheckedItems().addListener(listChangeListener);
    }

    public boolean showConfirmationDialogGovPolicy(GovPolicy policy, boolean isChecked) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Confirm " + (isChecked ? "adding" : "removing") + " government policy");

        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        Node okButton = dialog.getDialogPane().lookupButton(confirmButtonType);

        if (isChecked) {
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            Label infoLabel = new Label(policy.getInfo());
            Text priceText = new Text("Price: ");
            Label priceLabel = new Label();

            Label yearsLabel = new Label("Years: ");
            Slider yearsSlider = new Slider(1, 10, 5);
            yearsSlider.setShowTickLabels(true);
            yearsSlider.setShowTickMarks(true);
            yearsSlider.setMinorTickCount(0);
            yearsSlider.setMajorTickUnit(1);
            yearsSlider.setBlockIncrement(1);
            yearsSlider.setSnapToTicks(true);
            DoubleProperty priceProperty = new SimpleDoubleProperty(policy.getPrice() * game.getPlayer().getPopulation() / 2 * yearsSlider.getValue());
            priceLabel.textProperty().bind(GUtils.stringBindingDoubleCurrency(priceProperty));
            okButton.setDisable(!game.canPurchase(priceProperty.getValue()));
            yearsSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                int val = newValue.intValue();
                if (val != oldValue.intValue()) {
                    priceProperty.set(policy.getPrice() * game.getPlayer().getPopulation() / 2 * val);
                }
            });
            priceProperty.addListener((observable, oldValue, newValue) -> {
                boolean hasTreasury = game.canPurchase(newValue.doubleValue());
                okButton.setDisable(!hasTreasury);
            });
            grid.add(infoLabel, 0, 0);
            grid.add(priceText, 0, 1);
            grid.add(priceLabel, 1, 1);
            grid.add(yearsLabel, 0, 2);
            grid.add(yearsSlider, 1, 2);
            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType) {
                    int years = (int) yearsSlider.getValue();
                    game.spendTreasury(priceProperty.getValue());
                    game.getPlayer().getGovernment().getPolicies().put(policy, years);
                    return true;
                }
                checkListViewGovPolicies.getCheckModel().clearCheck(policy);
                checkListViewGovPolicies.refresh();
                return false;
            });
        } else {
            dialog.getDialogPane().setContent(new Label("Do you want to remove the policy: " + policy + "?\nYears left: " + game.getPlayer().getGovernment().getPolicies().get(policy)));
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == confirmButtonType)
                    return true;
                checkListViewGovPolicies.getCheckModel().check(policy);
                checkListViewGovPolicies.refresh();
                return false;
            });
        }

        Optional<Boolean> result = dialog.showAndWait();

        return result.isPresent() && result.get();
    }

    public static Glyph getGlyph(FontAwesome.Glyph angleDoubleDown) {
        return new FontAwesome().create(angleDoubleDown);
    }

}
