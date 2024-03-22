package com.erimali.cntrygame;

import java.io.File;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GameStage extends Stage {
    // POP UP WHEN FULLSCREEN PROBLEM
    private Label countryName;
    private Label date;
    private Label pausation;
    private Button pause;
    private Button chooseCountryButton;
    protected boolean isPaused;
    protected boolean isPlayingCountry;
    // Map related
    private WorldMap map;
    private GLogic game;
    private int selectedCountry;
    private int selectedProv;

    private Label hoveringCountry;
    private Label selectedCountryInfo;
    private Label selectedProvInfo;

    private Label mgResult;
    //
    private VBox leftCountryOpt;
    private VBox leftProvOpt;
    //
    private Stage commandLineStage;
    private Label infoRelations;
    private Button sendAllianceRequest;

    public GameStage() {
        setTitle(Main.APP_NAME + " - Game Window");
        setOnCloseRequest(e -> close());
        this.selectedCountry = -1;
        BorderPane gameLayout = createGameLayout();
        setWidth(1280);
        setHeight(720);
        setGame(new GLogic(this));
        CommandLine.setGs(this);

        Scene gameScene = new Scene(gameLayout);
        setScene(gameScene);
        gameScene.getStylesheets().add(getClass().getResource("css/gameStage.css").toExternalForm());
        //There can be problems when loading savegame
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

    private BorderPane createGameLayout() {
        BorderPane gameLayout = new BorderPane();
        gameLayout.setPadding(new Insets(10));
        // Customize the game layout
        // TOP
        gameLayout.setCenter(new BorderPane());
        setCountryName(new Label("Select Country"));
        setDate(new Label("Default Date"));
        setPause(new Button("Pause"));
        isPaused = true;
        getPause().setOnAction(e -> pausePlayDate());
        setPausation(new Label("Paused"));

        chooseCountryButton = new Button("Confirm");
        chooseCountryButton.setOnAction(e -> startGame());
        HBox htopleft = new HBox(getCountryName(), chooseCountryButton);
        htopleft.setSpacing(10);
        HBox htopright = new HBox(getPausation(), getPause(), getDate());
        htopright.setSpacing(10);
        Region reg = new Region();

        HBox htop = new HBox(htopleft, reg, htopright);
        HBox.setHgrow(reg, Priority.ALWAYS);
        htop.setSpacing(10);
        htop.setStyle("-fx-background-color: #f0f0f0;");

        gameLayout.setTop(htop);

        // CENTER
        map = new WorldMap(this);
        gameLayout.setCenter(map.start());

        // LEFT
        // close X
        ScrollPane leftScrollPane = new ScrollPane();
        leftScrollPane.setPadding(new Insets(10));
        selectedCountryInfo = new Label();
        selectedProvInfo = new Label();
        leftCountryOpt = makeVBoxCountryOptions();
        leftCountryOpt.setSpacing(10);
        leftProvOpt = makeVBoxMyProvOptions();
        leftProvOpt.setSpacing(10);
        VBox leftGeneral = new VBox(selectedCountryInfo, leftCountryOpt, selectedProvInfo, leftProvOpt);
        leftGeneral.setSpacing(10);

        leftScrollPane.setContent(leftGeneral);
        gameLayout.setLeft(leftScrollPane);

        hoveringCountry = new Label("Bottom text");
        HBox bottomInfo = new HBox(hoveringCountry);

        gameLayout.setBottom(bottomInfo);
        Label toChange = new Label();
        // maybe Button[], setOnAction( i ...);

        Button[] mapModes = new Button[2];

        mapModes[0] = new Button("Default");// change to img
        mapModes[0].setOnAction(event -> {
            map.switchMapMode(0);
        });
        mapModes[1] = new Button("Allies");
        mapModes[1].setOnAction(event -> {
            map.switchMapMode(1);
        });
        HBox mapChoices = new HBox(mapModes[0], mapModes[1]);
        VBox rightInfo = new VBox(toChange, mapChoices);

        // rightScrollPane.setContent(rightInfo);
        // gameLayout.setRight(rightScrollPane);
        gameLayout.setRight(rightInfo);

        gameLayout.setOnKeyPressed(event -> {
            // ` -> commandLine
            if (event.getCode() == KeyCode.BACK_QUOTE) {
                if (commandLineStage.isShowing()) {
                    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    // REDUNDANT?????????????????????????????
                    commandLineStage.hide();
                } else {
                    commandLineStage.show();
                    commandLineStage.requestFocus();
                }
            }
        });

        commandLineStage = makeCommandLineStage();

        return gameLayout;
    }

    private Stage makeCommandLineStage() {
        // Creating the command line popup
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

    private VBox makeVBoxCountryOptions() {

        Label optionsText = new Label("Options");
        Label optionsWar = new Label("War");
        Button declareWar = new Button("Declare War");
        declareWar.setOnAction(e -> declareWar());
        VBox vboxWar = new VBox(optionsWar, declareWar);
        Label preInfoRelations = new Label("Relations ");
        infoRelations = new Label();
        HBox optionsRelations = new HBox(preInfoRelations, infoRelations);
        ToggleButton improveRelations = new ToggleButton("Improve relations");
        improveRelations.getStyleClass().add("custom-toggle-button");
        Tooltip.install(improveRelations, new Tooltip("Start improving relations with country (monthly)"));
        improveRelations.setOnAction(event -> {
            if (improveRelations.isSelected()) {
                // CSS
                game.addImprovingRelations(selectedCountry);
                improveRelations.setText("Stop improving relations");
            } else {
                game.removeImprovingRelations(selectedCountry);
                improveRelations.setText("Improve relations");
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

    private VBox makeVBoxMyProvOptions() {
        Button raiseFunds = new Button("Raise municipal funds");
        Button investInProv = new Button("Invest");
        return new VBox(raiseFunds, investInProv);
    }

    ////////////////////////////////////////////////////
    ////////////////////////////////////////////////////
    // if(game.isSubjectOfPlayer(selectedCountry))
    public VBox makeVBoxSubjectOptions() {
        return null;
    }

    public void sendDonation() {
        // selected country -> treasury += input
        Double result = showNumberInputDialog(10000000);
        //game.getPlayer().getEconomy().getGDP()/10
        if (result != null) {
            displayNumberInputResult(result);
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

        ListView<CasusBelli> cbListView = War.makeListViewCasusBelli(game.getPlayer(), game.getWorldCountries().get(selectedCountry));
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
        map.setPlayerCountry(selectedCountry);
        chooseCountryButton.setVisible(false);
        isPlayingCountry = true;
    }

    public void pausePlayDate() {
        if (!isPlayingCountry)
            return;
        isPaused = !isPaused;
        if (isPaused) {
            pausation.setVisible(true);
            pause.setText("Play");
        } else {
            pausation.setVisible(false);
            pause.setText("Pause");
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

    public void setPausation(Label pausation) {
        this.pausation = pausation;
    }

    public Button getPause() {
        return pause;
    }

    public void setPause(Button pause) {
        this.pause = pause;
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

    //make more efficient
    public void changeSelectedCountryInfo() {
        selectedCountryInfo.setText(game.toStringCountry(selectedCountry));
        if (isPlayingCountry) {
            if (selectedCountry == game.getPlayerId()) {
                if (leftCountryOpt.isVisible())
                    leftCountryOpt.setVisible(false);
                leftProvOpt.setVisible(true);
            } else {
                if (!leftCountryOpt.isVisible())
                    leftCountryOpt.setVisible(true);
                leftProvOpt.setVisible(false);
                //make more efficient
                if (game.isAllyWith(selectedCountry)) {
                    sendAllianceRequest.setText("Break alliance");
                } else {
                    sendAllianceRequest.setText("Alliance request");
                }
                infoRelations.setText(game.getRelationsWith(CountryArray.getIndexISO2(selectedCountry)));
            }
        }

    }

    public void updateSelectedCountryInfo() {
        if (selectedCountry == game.getPlayerId()) {

        } else {

            infoRelations.setText(game.getRelationsWith(CountryArray.getIndexISO2(selectedCountry)));
        }
    }

    // MiniGame
    public void showPopupMGTicTacToe(boolean playerTurn, int difficultyAI) {
        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initOwner(this);

        // Create the root layout for the popup
        GridPane popupRoot = new GridPane();
        Scene popupScene = new Scene(popupRoot, 400, 300);

        // Create the Tic Tac Toe board
        MGTicTacToe ttt = new MGTicTacToe(playerTurn, difficultyAI);
        int size = MGTicTacToe.getSize();
        Button[][] boardButtons = new Button[size][size];
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int currentRow = row; // Create a final or effectively final copy of row
                int currentCol = col; // Create a final or effectively final copy of col

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
        // Set the scene for the popup stage
        popupStage.setScene(popupScene);

        // Show the popup
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
            if (result != MGTicTacToe.getOngoing()) {
                mgResult.setText(ttt.getGameResult());
            }
        }
    }

    // boolean pauseGame (should it be paused while popping up or not)
    public void popupGEvent(GEvent gEvent) {
        // Create a new stage for the popup
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Game Event");

        // Create the content for the popup
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
            optButton[i].setPrefWidth(400); // related to max text?
            optButton[i].setStyle("-fx-background-color: #00FF00;-fx-text-fill: #000000;");
            ;
            // optButton[i].applyCss();
            // HMMMM css not activated?
            int choice = i;
            optButton[i].setOnAction(event -> {
                gEvent.run(choice);
                popupStage.close();
                isPaused = false;
            });
        }
        VBox options = new VBox(optButton);

        options.setSpacing(10);
        VBox popupRoot = new VBox(popupTop, descriptionLabel, options);
        options.setAlignment(Pos.CENTER);

        popupRoot.setSpacing(10);
        StackPane container = new StackPane(popupRoot);
        StackPane.setMargin(popupRoot, new Insets(20));
        Scene popupScene = new Scene(container);

        // Set the scene for the popup stage
        popupStage.setScene(popupScene);
        isPaused = true;
        // Show the popup
        Platform.runLater(() -> popupStage.showAndWait());
    }

    public void popupWebNews() {
        File file = new File("src/main/resources/web/news.html");
        String filePath = file.toURI().toString();

        WebView webView = new WebView();
        webView.getEngine().load(filePath);

        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 800, 600));
        popupStage.setTitle("World News");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
    }

    public void popupChess(String cn) {
        if (!isPaused) {
            pausePlayDate();
        }
        String PLAYER = game.getPlayer().getGovernment().toStringMainRuler();
        String OPPONENT = game.getWorldCountries().get(cn).getGovernment().toStringMainRuler();
        File file = new File("src/main/resources/web/chess/chess.html");
        String filePath = file.toURI().toString() + "?player=" + PLAYER + "&opponent=" + OPPONENT + "&src=countrysim";

        WebView webView = new WebView();
        webView.getEngine().load(filePath);

        Stage popupStage = new Stage();
        popupStage.setScene(new Scene(webView, 1000, 700));
        popupStage.setTitle("Chess Battle");
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.show();
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
        alert.setTitle("Error");
        alert.setHeaderText(null);
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
        TextField inputField = new TextField("0");
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            inputField.setText(String.valueOf(newValue.intValue()));
        });
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
        this.selectedProv = provId;
        this.changeSelectedProvInfo();
    }

    private void changeSelectedProvInfo() {
        selectedProvInfo.setText(game.getProvInfo(selectedProv));
    }

    public void setSelectedCountry(int ownerId) {
        this.selectedCountry = ownerId;
        this.changeSelectedCountryInfo();
    }


    public void correlateProvinces(SVGProvince[] mapSVG) {
        game.getWorld().initiateProvinces(mapSVG);
    }
}
