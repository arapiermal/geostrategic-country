package com.erimali.minigames;

import com.erimali.cntrygame.CommandLine;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MG2048Stage extends Stage {
    private final Label score;
    private MG2048 minigame;
    private GridPane gridPane;
    private Tile[][] tiles;

    public MG2048Stage() {
        minigame = new MG2048();
        gridPane = new GridPane();
        score = new Label("0");
        Text scoreExtra = new Text(" points");
        HBox scoreBox = new HBox(score, scoreExtra);
        VBox vBox = new VBox(scoreBox, gridPane);
        tiles = minigame.initFX(gridPane);

        Scene scene = new Scene(vBox);
        this.setScene(scene);
        this.setTitle("X^11 Game");
        scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));
    }

    public int getScore() {
        return minigame.getScore();
    }

    private void handleKeyPress(KeyCode code) {

        boolean ended = false;
        switch (code) {
            case UP:
                ended = minigame.move(Direction.UP);
                break;
            case DOWN:
                ended = minigame.move(Direction.DOWN);
                break;
            case LEFT:
                ended = minigame.move(Direction.LEFT);
                break;
            case RIGHT:
                ended = minigame.move(Direction.RIGHT);
                break;
            default:
                break;
        }
        if (ended) {
            CommandLine.execute("ALERT INFORMATION GAME ENDED");
        }
        //or reverse
        minigame.updateFX(tiles);
        score.setText(String.valueOf(minigame.getScore()));
    }
}
