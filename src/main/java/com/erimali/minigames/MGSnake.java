package com.erimali.minigames;

import java.util.LinkedList;
import java.util.List;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Point {
	int x;
	int y;

	Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point op = (Point) o;
			return this.x == op.x && this.y == op.y;
		}
		return false;
	}

	@Override
	public String toString() {
		return this.x + " " + this.y;
	}
}

class Snake {
	List<Point> segments;

	Snake(int x, int y) {
		segments = new LinkedList<Point>();
		segments.add(new Point(x, y));
	}

	Point getHead() {
		return segments.get(0);
	}

	Point getEnd() {
		return segments.get(segments.size() - 1);
	}

	void addHead(int x, int y) {
		segments.add(0, new Point(x, y));
	}

	Point removeEnd() {
		return segments.remove(segments.size() - 1);
	}

	int getLength() {
		return segments.size();
	}

	boolean isPart(int x, int y) {
		return segments.contains(new Point(x, y));
	}

	boolean isPart(Point p) {
		return segments.contains(p);
	}
}

public class MGSnake {
	private static final int EMPTY = 0;
	private static final int PLAYER = 5;
	private static final int APPLE = 6;
	private static final int BLOCK = 1;
	private static final int STOPBLOCK = 2;
	private int size;
	private int blockNumber;
	private int X, Y;
	private Snake snake;
	//private Color snakeHColor;
	//private Color snakeBColor;
	private int[][] grid;
	private boolean changedDir;
	private Direction nextDir; // less glitchy

	private static final Direction DEFDIR = Direction.RIGHT;
	private Direction dir;

	public MGSnake(int size) {
		this.size = size;
		this.grid = new int[size][size];
		initializeGrid();

	}

	public MGSnake(int size, String grid) {
		this.size = size;
		this.grid = new int[size][size];
		initializeGrid(grid);
	}

	public void initializeGrid() {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				grid[i][j] = 0;
			}
		}
		X = size / 2;
		Y = size / 2;
		this.snake = new Snake(X, Y);
		grid[X][Y] = PLAYER;
		putApple();
		dir = DEFDIR;
	}

	public void initializeGrid(String in) {
		//in = in.replaceAll("\\s+", "");
		in = in.replaceAll("[\\s&&[^\n]]+", "");
		String[] s = in.split("\n");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				int type = s[j].charAt(i) - '0';
				grid[i][j] = type;
				if(type > 0&&type<3)
					blockNumber++;
			}
		}
		X = size / 2;
		Y = size / 2;
		this.snake = new Snake(X, Y);
		grid[X][Y] = PLAYER;
		putApple();
		dir = DEFDIR;
	}

	public boolean move(int i) {
		while (i > 0)
			if (move())
				return true;
			else
				i--;
		return false;
	}

	public boolean move() {
		changedDir = false;
		int x = X, y = Y;
		switch (dir) {
		case RIGHT:
			x = (X + 1) % size;
			break;
		case UP:
			y = (Y - 1 + size) % size;
			break;
		case LEFT:
			x = (X - 1 + size) % size;
			break;
		case DOWN:
			y = (Y + 1) % size;
			break;
		}
		if (grid[x][y] == STOPBLOCK) {
			return false;
		} else {
			X = x;
			Y = y;
		}
		if (grid[X][Y] == APPLE) {
			grid[X][Y] = PLAYER;
			snake.addHead(X, Y);
			return putApple();
		} else if (grid[X][Y] == PLAYER || grid[X][Y] == BLOCK) {
			return true;
		} else {
			Point end = snake.removeEnd();
			grid[end.x][end.y] = EMPTY;
			grid[X][Y] = PLAYER;
			snake.addHead(X, Y);
			return false;
		}
	}

	public boolean putApple() {
		Point p = genApple();
		if (p == null) {
			return true;// fully completed
		} else {
			grid[p.x][p.y] = APPLE;
			return false;
		}
	}

	public Point genApple() {
		if (snake.getLength() == size * size - blockNumber) {
			return null;
		} else {
			Point p;
			int x, y;
			do {
				x = (int) (Math.random() * size);
				y = (int) (Math.random() * size);
				p = new Point(x, y);
			} while (snake.isPart(p) || grid[x][y] != EMPTY);
			return p;
		}
	}

	public int getX() {
		return X;
	}

	public void setX(int x) {
		X = x;
	}

	public int getY() {
		return Y;
	}

	public void setY(int y) {
		Y = y;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				// !!!!!!!!!!!!!!!!!!! j i
				sb.append(grid[j][i]).append(" ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		MGSnake mg = new MGSnake(8);
		mg.move();
		mg.move();

		System.out.println(mg.toString());

	}

	public Rectangle[][] initializeFX(GridPane gp) {
		gp.getChildren().clear(); // Clear the existing children from the GridPane
		Rectangle[][] rec = new Rectangle[size][size];
		int cellSize = 32; // Adjust this to your desired cell size

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				int cellValue = grid[j][i]; // Access the cell's value in the grid

				Rectangle cellRect = new Rectangle(cellSize, cellSize);
				rec[i][j] = cellRect;
				if (cellValue == EMPTY) {
					rec[i][j].setFill(Color.WHITE);
				} else if (cellValue == PLAYER) {
					rec[i][j].setFill(Color.DARKGREEN);
				} else if (cellValue == APPLE) {
					rec[i][j].setFill(Color.RED);
				} else if (cellValue == BLOCK) {
					rec[i][j].setFill(Color.BLACK);
				} else if (cellValue == STOPBLOCK) {
					rec[i][j].setFill(Color.BLUE);
				}
				gp.add(cellRect, i, j); // Add the rectangle to the GridPane at (j, i)
			}
		}
		return rec;
	}

	public void updateFX(Rectangle[][] rec) {
		// can update based on last thing happened for efficiency?
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				int cellValue = grid[i][j];
				if (cellValue == EMPTY) {
					rec[i][j].setFill(Color.WHITE);
				} else if (cellValue == PLAYER) {
					if (X == i && Y == j)
						rec[i][j].setFill(Color.DARKGREEN);
					else
						rec[i][j].setFill(Color.GREEN);
				} else if (cellValue == APPLE) {
					rec[i][j].setFill(Color.RED);
				} else if (cellValue == BLOCK) {
					rec[i][j].setFill(Color.BLACK);
				} else if (cellValue == STOPBLOCK) {
					rec[i][j].setFill(Color.BLUE);
				}
			}
		}
	}

	public Direction getDir() {
		return dir;
	}

	public boolean isDir(Direction dir) {
		return this.dir.equals(dir);
	}

	public void setDir(Direction dir) {
		if (changedDir)
			return;
		this.dir = dir;
		changedDir = true;
	}
}
