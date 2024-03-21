package com.erimali.minigames;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BreakoutGame extends Application {

	private static final String CSS_RESOURCE_NAME = "breakout.css";
	private static final Config config = Config.getInstance();

	private Scene gameScene;
	final private GameState gameState = new GameState();
	final private InputHandler inputHandler = new InputHandler();
	final private UpdateHandler updateHandler = new UpdateHandler(gameState);
	private AnimationTimer gameLoop;

	@Override
	public void start(Stage window) {
		gameScene = createGameScene();
		gameScene.setOnKeyPressed(inputHandler);
		gameScene.setOnKeyReleased(inputHandler);
		gameLoop = createGameLoop();

		window.setTitle(config.getString("window.title"));
		window.setScene(gameScene);
		window.setResizable(false);
		window.show();

		gameLoop.start();
	}

	public Scene createGameScene() {
		Pane layout = new Pane();
		layout.setPrefSize(config.getDouble("scene.prefWidth"), config.getDouble("scene.prefHeight"));

		layout.getChildren()
				.addAll(gameState.getAllSprites().stream().map(Sprite::getShape).collect(Collectors.toList()));
		gameState.getBricks().addListener((ListChangeListener<Brick>) change -> {
			while (change.next()) {
				if (change.wasRemoved()) {
					layout.getChildren()
							.removeAll(change.getRemoved().stream().map(Brick::getShape).collect(Collectors.toList()));
				}
			}
		});

		Scene scene = new Scene(layout);
		scene.getStylesheets().add(getClass().getResource(CSS_RESOURCE_NAME).toExternalForm());

		return scene;
	}

	private AnimationTimer createGameLoop() {
		return new AnimationTimer() {
			public void handle(long now) {
				updateHandler.update(now, inputHandler.getActiveKeys());
				if (gameState.isGameOver()) {
					this.stop();
				}
			}
		};
	}

	public static void main(String[] args) {
		launch(args);
	}
}

class GameState {
	private static final Config config = Config.getInstance();

	private ObservableList<Brick> bricks;
	private Ball ball;
	private Paddle paddle;
	private boolean gameOver;

	public GameState() {
		init();
	}

	public void init() {
		ball = new Ball();
		paddle = new Paddle();
		bricks = FXCollections.observableArrayList();

		int numColumns = config.getInt("bricks.numColumns");
		int numRows = config.getInt("bricks.numRows");
		int numColors = config.getInt("bricks.numColors");
		Double spacing = config.getDouble("bricks.spacing");
		Double yOffset = config.getDouble("bricks.yOffset");

		for (int x = 0; x < numColumns; x++) {
			for (int y = 0; y < numRows; y++) {
				Brick brick = new Brick();
				Shape brickShape = brick.getShape();

				int brickColorIndex = 1 + (int) Math.floor(y / (numRows * 1.0 / numColors));
				brickShape.getStyleClass().add("color" + brickColorIndex);

				double xPos = x * (brickShape.getLayoutBounds().getWidth() + spacing);
				double yPos = y * (brickShape.getLayoutBounds().getHeight() + spacing) + yOffset;
				brick.setPosition(new Point2D(xPos, yPos));

				bricks.add(brick);
			}
		}
	}

	public ObservableList<Brick> getBricks() {
		return bricks;
	}

	public Ball getBall() {
		return ball;
	}

	public Paddle getPaddle() {
		return paddle;
	}

	public List<Sprite> getAllSprites() {
		List<Sprite> allSprites = new ArrayList<>(bricks);
		allSprites.add(ball);
		allSprites.add(paddle);

		return allSprites;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}
}

class UpdateHandler {

	private static final Config config = Config.getInstance();

	private GameState gameState;

	private final Bounds wallBounds;

	private final Point2D paddleRightVelocity;
	private final Point2D paddleLeftVelocity;

	private static final Point2D LEFT_VECTOR = new Point2D(-1, 0);
	private static final Point2D RIGHT_VECTOR = new Point2D(1, 0);
	private static final Point2D UP_VECTOR = new Point2D(0, 1);
	private static final Point2D DOWN_VECTOR = new Point2D(0, -1);

	public UpdateHandler(GameState gameState) {
		this.gameState = gameState;

		wallBounds = new BoundingBox(config.getDouble("wall.minX"), config.getDouble("wall.minY"),
				config.getDouble("wall.width"), config.getDouble("wall.height"));

		double defaultPaddleSpeed = gameState.getPaddle().getDefaultSpeed();
		paddleLeftVelocity = LEFT_VECTOR.multiply(defaultPaddleSpeed);
		paddleRightVelocity = RIGHT_VECTOR.multiply(defaultPaddleSpeed);
	}

	public void update(long now, Set<KeyCode> activeKeys) {
		applyInputToPaddle(activeKeys);

		updateSpritePositions();

		restrictPaddleMovementByWalls();
		restrictBallMovementByWalls();
		//if ball y > certain
		handlePaddleCollision();
		handleBrickCollision();
	}

	private void applyInputToPaddle(Set<KeyCode> activeKeys) {
		Point2D paddleVelocity = Point2D.ZERO;

		if (activeKeys.contains(KeyCode.LEFT)) {
			paddleVelocity = paddleVelocity.add(paddleLeftVelocity);
		}

		if (activeKeys.contains(KeyCode.RIGHT)) {
			paddleVelocity = paddleVelocity.add(paddleRightVelocity);
		}

		gameState.getPaddle().setVelocity(paddleVelocity);
	}

	private void restrictPaddleMovementByWalls() {
		Paddle paddle = gameState.getPaddle();
		double paddleWidth = paddle.getShape().getLayoutBounds().getWidth();

		if (paddle.getPosition().getX() < wallBounds.getMinX()) {
			paddle.setVelocity(Point2D.ZERO);
			paddle.setPosition(new Point2D(wallBounds.getMinX(), paddle.getPosition().getY()));
		}

		if (paddle.getPosition().getX() + paddleWidth > wallBounds.getMaxX()) {
			paddle.setVelocity(Point2D.ZERO);
			paddle.setPosition(new Point2D(wallBounds.getMaxX() - paddleWidth, paddle.getPosition().getY()));
		}
	}

	private void restrictBallMovementByWalls() {
		Ball ball = gameState.getBall();

		if (ball.getPosition().getX() <= wallBounds.getMinX()) {
			ball.setVelocity(reflect(ball.getVelocity(), RIGHT_VECTOR));
		}

		if (ball.getPosition().getX() + ball.getShape().getLayoutBounds().getWidth() >= wallBounds.getMaxX()) {
			ball.setVelocity(reflect(ball.getVelocity(), LEFT_VECTOR));
		}

		if (ball.getPosition().getY() <= wallBounds.getMinY()) {
			ball.setVelocity(reflect(ball.getVelocity(), DOWN_VECTOR));
		}

		if (ball.getPosition().getY() + ball.getShape().getLayoutBounds().getHeight() >= wallBounds.getMaxY()) {
			gameState.setGameOver(true);
		}
	}

	private void handlePaddleCollision() {
		Ball ball = gameState.getBall();

		if (gameState.getBall().intersects(gameState.getPaddle())) {
			ball.setVelocity(reflect(ball.getVelocity(), UP_VECTOR));
		}
	}

	private void handleBrickCollision() {
		Ball ball = gameState.getBall();
		ObservableList<Brick> bricks = gameState.getBricks();

		for (Iterator<Brick> brickIterator = bricks.iterator(); brickIterator.hasNext();) {
			Brick brick = brickIterator.next();

			if (ball.intersects(brick)) {
				brickIterator.remove();

				// note this is simplistic, would better to find out which side of the brick was
				// hit and do the correct reflection for the side.
				ball.setVelocity(reflect(ball.getVelocity(), DOWN_VECTOR));
			}
		}
	}

	private void updateSpritePositions() {
		gameState.getAllSprites().forEach(Sprite::applyVelocity);
	}

	private Point2D reflect(Point2D vector, Point2D normal) {
		return vector.subtract(normal.multiply(vector.dotProduct(normal) * 2));
	}

}

class InputHandler implements EventHandler<KeyEvent> {
	final private Set<KeyCode> activeKeys = new HashSet<>();

	@Override
	public void handle(KeyEvent event) {
		if (KeyEvent.KEY_PRESSED.equals(event.getEventType())) {
			activeKeys.add(event.getCode());
		} else if (KeyEvent.KEY_RELEASED.equals(event.getEventType())) {
			activeKeys.remove(event.getCode());
		}
	}

	public Set<KeyCode> getActiveKeys() {
		return Collections.unmodifiableSet(activeKeys);
	}
}

class Sprite {
	private Shape shape;
	private Point2D pos;
	private Point2D velocity;
	private double defaultSpeed;

	public Sprite() {
		this(null);
	}

	public Sprite(Shape shape) {
		this(shape, 0, 0);
	}

	public Sprite(Shape shape, double x, double y) {
		this(shape, 0, 0, 0, 0);
	}

	public Sprite(Shape shape, double x, double y, double dx, double dy) {
		this.shape = shape;
		pos = new Point2D(x, y);
		velocity = new Point2D(dx, dy);

		updateShapePos();
	}

	public void applyVelocity() {
		pos = pos.add(velocity);

		updateShapePos();
	}

	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;

		updateShapePos();
	}

	public Point2D getVelocity() {
		return velocity;
	}

	public void setVelocity(Point2D velocity) {
		this.velocity = velocity;
	}

	public Point2D getPosition() {
		return pos;
	}

	public void setPosition(Point2D pos) {
		this.pos = pos;

		updateShapePos();
	}

	public double getDefaultSpeed() {
		return defaultSpeed;
	}

	public void setDefaultSpeed(double defaultSpeed) {
		this.defaultSpeed = defaultSpeed;
	}

	public boolean intersects(Sprite otherSprite) {
		Shape intersection = Shape.intersect(getShape(), otherSprite.getShape());
		Bounds intersectionBounds = intersection.getLayoutBounds();

		return intersectionBounds.getWidth() > 0 || intersectionBounds.getHeight() > 0;
	}

	private void updateShapePos() {
		if (shape != null) {
			shape.relocate(pos.getX(), pos.getY());
		}
	}
}

class Brick extends Sprite {
	private static final Config config = Config.getInstance();

	public Brick() {
		Shape brickShape = new Rectangle(config.getDouble("brick.width"), config.getDouble("brick.height"));
		brickShape.getStyleClass().add("brick");

		setShape(brickShape);
	}
}

class Ball extends Sprite {
	private static final Config config = Config.getInstance();

	private static final Point2D UP_VECTOR = new Point2D(0, 1);

	public Ball() {
		Shape ballShape = new Circle(config.getDouble("ball.diameter") / 2);
		ballShape.getStyleClass().add("ball");

		setShape(ballShape);

		setPosition(new Point2D(config.getDouble("ball.initX"), config.getDouble("ball.initY")));

		setDefaultSpeed(config.getDouble("ball.defaultSpeed"));

		Double defaultDirection = Math.toRadians(config.getDouble("ball.defaultDirection") - 90);
		setVelocity(new Point2D(Math.cos(defaultDirection), Math.sin(defaultDirection)).normalize()
				.multiply(getDefaultSpeed()));
	}
}

class Paddle extends Sprite {
	private static final Config config = Config.getInstance();

	public Paddle() {
		Shape paddleShape = new Rectangle(config.getDouble("paddle.width"), config.getDouble("paddle.height"));
		paddleShape.getStyleClass().add("paddle");

		setShape(paddleShape);

		setPosition(new Point2D(config.getDouble("paddle.initX"), config.getDouble("paddle.initY")));

		setDefaultSpeed(config.getDouble("paddle.defaultSpeed"));
	}
}

class Config {
	public static final String CONFIG_RESOURCE_NAME = "breakout.properties";

	public static final Config instance = new Config();

	Properties configProperties = new Properties();

	public Config() {
		try {
			configProperties.load(Config.class.getResourceAsStream(CONFIG_RESOURCE_NAME));
		} catch (IOException e) {
			System.err.println("Unable to load configuration properties: " + CONFIG_RESOURCE_NAME);
			e.printStackTrace();

			System.exit(-1);
		}
	}

	public static Config getInstance() {
		return instance;
	}

	public double getDouble(String propertyName) {
		String propertyValueString = getMandatoryStringValue(propertyName);
		double value = 0;

		try {
			value = Double.parseDouble(propertyValueString);
		} catch (NumberFormatException e) {
			System.err.println(
					"Property not a valid double value: " + propertyName + " with value: " + propertyValueString);

			System.exit(-1);
		}

		return value;
	}

	public int getInt(String propertyName) {
		String propertyValueString = getMandatoryStringValue(propertyName);
		int value = 0;

		try {
			value = Integer.parseInt(propertyValueString);
		} catch (NumberFormatException e) {
			System.err
					.println("Property not a valid int value: " + propertyName + " with value: " + propertyValueString);

			System.exit(-1);
		}

		return value;
	}

	public String getString(String propertyName) {
		return getMandatoryStringValue(propertyName);
	}

	private String getMandatoryStringValue(String propertyName) {
		String propertyValueString = configProperties.getProperty(propertyName);

		if (propertyValueString == null || propertyValueString.isBlank()) {
			System.err.println("Missing mandatory property: " + propertyName);

			System.exit(-1);
		}

		return propertyValueString;
	}
}