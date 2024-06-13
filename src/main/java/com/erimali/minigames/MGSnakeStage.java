package com.erimali.minigames;

import com.erimali.cntrygame.ErrorLog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MGSnakeStage extends Stage {
    //problem when horizontally or vertically full size => wrap
    private static final int DEF_SIZE = 12;
    private MGSnake snakeGame;
    private Label labelScore;
    private GridPane gridPane;
    private Rectangle[][] rec;
    private Label labelPlaying;
    private boolean playing;
    private long delay;

    //"222000000222\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n222000000222"
    public MGSnakeStage() {
        this.delay = 200;
        snakeGame = new MGSnake(DEF_SIZE);
        Text textScore = new Text("Score:");
        labelScore = new Label("0");
        Region reg = new Region();
        labelPlaying = new Label("");
        Button buttonHelp = new Button("Help");
        buttonHelp.setFocusTraversable(false);
        buttonHelp.setOnAction(e -> showSnakeHelp());
        HBox hBoxTop = new HBox(8, textScore, labelScore, reg, labelPlaying, buttonHelp);
        HBox.setHgrow(reg, Priority.ALWAYS);
        hBoxTop.setPadding(new Insets(4));
        gridPane = new GridPane();
        rec = snakeGame.initializeFX(gridPane);
        VBox vBox = new VBox(hBoxTop, gridPane);
        Scene scene = new Scene(vBox);
        setScene(scene);
        setTitle("Snake Game");
        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));
        beginGame();
    }

    private void showSnakeHelp() {
        String contentText = "Arrow keys to change direction\nSpace to pause/play\nF for faster speed, S for slower speed";
        Alert alert = new Alert(Alert.AlertType.INFORMATION, contentText);
        alert.setTitle("Snake Help");
        alert.show();
    }

    public void beginGame() {
        this.playing = true;
        new Thread(() -> {
            while (true) {
                if (playing) {
                    boolean ended = snakeGame.move();
                    Platform.runLater(() -> {
                        snakeGame.updateFX(rec);
                        labelScore.setText(String.valueOf(snakeGame.getScore()));
                    });
                    if (ended) {
                        break;
                    }
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    ErrorLog.logError(e);
                    break;
                }
            }
        }).start();
    }

    public static int getDefSize() {
        return DEF_SIZE;
    }

    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case UP:
                if (!snakeGame.isDir(Direction.DOWN))
                    snakeGame.setDir(Direction.UP);
                break;
            case DOWN:
                if (!snakeGame.isDir(Direction.UP))
                    snakeGame.setDir(Direction.DOWN);
                break;
            case LEFT:
                if (!snakeGame.isDir(Direction.RIGHT))
                    snakeGame.setDir(Direction.LEFT);
                break;
            case RIGHT:
                if (!snakeGame.isDir(Direction.LEFT))
                    snakeGame.setDir(Direction.RIGHT);
                break;
            case SPACE:
                pausePlay();
                break;
            case F:
                delay /= 2;
                break;
            case S:
                delay *= 2;
                break;
            default:
                break;
        }
    }

    private void pausePlay() {
        playing = !playing;
        labelPlaying.setText(playing ? "" : "Paused");
    }

    public int getScore() {
        return snakeGame.getScore();
    }
}
