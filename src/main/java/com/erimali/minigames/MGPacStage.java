package com.erimali.minigames;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MGPacStage extends Application {

	// problem when horizontally or vertically full size => wrap
	private static final int SIZE = 12;
	private MGPac snake;
	private GridPane gridPane;
	private Circle rec[][];
	private boolean paused;
	private long delay;

	@Override
	public void start(Stage primaryStage) {
		snake = new MGPac(SIZE,
				"222000000222\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n222000000222");

		gridPane = new GridPane();
		rec = snake.initializeFX(gridPane);

		Scene scene = new Scene(gridPane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Snake Game");
		primaryStage.show();
		scene.setOnKeyPressed(event -> handleKeyPress(event.getCode()));
		this.delay = 200;
		new Thread(() -> {
			while (true) {
				if (paused) {

				} else {
					boolean ended = snake.move();
					snake.updateFX(rec);
					if (ended) {
						break;
					}
				}
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void handleKeyPress(KeyCode code) {
		switch (code) {
		case UP:
			snake.setDir(Direction.UP);
			break;
		case DOWN:
			snake.setDir(Direction.DOWN);
			break;
		case LEFT:
			snake.setDir(Direction.LEFT);
			break;
		case RIGHT:
			snake.setDir(Direction.RIGHT);
			break;
		case SPACE:
			pause();
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

	private void pause() {
		paused = !paused;
	}

	public static void main(String[] args) {
		launch(args);
	}

}
