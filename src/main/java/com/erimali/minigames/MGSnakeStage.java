package com.erimali.minigames;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MGSnakeStage extends Application {
	
	//problem when horizontally or vertically full size => wrap
	private static final int SIZE = 12; 
	private MGSnake snake; 
	private GridPane gridPane;
	private Rectangle rec[][];
	private boolean paused;
	private long delay;
	@Override
	public void start(Stage primaryStage) {
		snake = new MGSnake(SIZE, "222000000222\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n222000000222");

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
					if(ended) {
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
			if (!snake.isDir(Direction.DOWN))
				snake.setDir(Direction.UP);
			break;
		case DOWN:
			if (!snake.isDir(Direction.UP))
				snake.setDir(Direction.DOWN);
			break;
		case LEFT:
			if (!snake.isDir(Direction.RIGHT))
				snake.setDir(Direction.LEFT);
			break;
		case RIGHT:
			if (!snake.isDir(Direction.LEFT))
				snake.setDir(Direction.RIGHT);
			break;
		case SPACE:
			pause();
			break;
		case F:
			delay/=2;
			break;
		case S:
			delay*=2;
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
