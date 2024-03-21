package com.erimali.minigames;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

class Pac {
	int health;
	int points;

	Pac(int health) {
		this.health = health;
	}

	public void addPoints(int i) {
		this.points += i;
	}

	public int getPoints() {
		return this.points;
	}
}

public class MGPac {
	private static final int EMPTY = 0;
	private static final int PLAYER = 5;
	private static final int POINT = 6;
	private static final int BLOCK = 1;
	private static final int STOPBLOCK = 2;
	private static final int ENEMY = 7;

	private int size;
	private int blockNumber;
	private int X, Y;
	private Pac pac;
	private int[][] grid;
	private boolean changedDir;
	private Direction nextDir; // less glitchy

	private static final Direction DEFDIR = Direction.RIGHT;
	private Direction dir;
	private int pointsLeft;

	public MGPac(int size) {
		this.size = size;
		this.grid = new int[size][size];
		initializeGrid();
	}

	public MGPac(int size, String grid) {
		this.size = size;
		this.grid = new int[size][size];
		initializeGrid(grid);
	}

	public void initializeGrid() {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				grid[i][j] = POINT;
			}
		}
		this.pointsLeft = grid.length * grid[0].length;
		X = size / 2;
		Y = size / 2;
		this.pac = new Pac(3);
		grid[X][Y] = PLAYER;
		putApple();
		dir = DEFDIR;
	}

	public void initializeGrid(String in) {
		in = in.replaceAll("[\\s&&[^\n]]+", "");
		String[] s = in.split("\n");
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				int type = s[j].charAt(i) - '0';
				if (type == 0) {
					grid[i][j] = POINT;
					this.pointsLeft++;
				} else {
					grid[i][j] = type;
					if (type > 0 && type < 3)
						blockNumber++;
				}
			}
		}
		X = size / 2;
		Y = size / 2;
		this.pac = new Pac(3);
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
			grid[X][Y] = EMPTY;
			grid[x][y] = PLAYER;
			X = x;
			Y = y;
		}
		if (grid[X][Y] == POINT) {
			pac.addPoints(1);
			return pointsLeft == 0;
		} else if (grid[X][Y] == ENEMY || grid[X][Y] == BLOCK) {
			return true;
		} else {
			return false;
		}
	}

	public boolean putApple() {
		Point p = genApple();
		if (p == null) {
			return true;// fully completed //ERROR?
		} else {
			grid[p.x][p.y] = POINT;
			return false;
		}
	}

	public Point genApple() {
		if (pac.getPoints() == size * size - blockNumber) {
			return null; // WON
		} else {
			Point p;
			int x, y;
			do {
				x = (int) (Math.random() * size);
				y = (int) (Math.random() * size);
				// !!
			} while (grid[x][y] != EMPTY);

			return new Point(x, y);
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
		MGPac mg; // = new MGPac(8);
		String tst = "222000000222\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n200000000002\n222000000222";
		mg = new MGPac(12, tst);

		mg.move();
		mg.move();

		System.out.println(mg.toString());

	}

	public Circle[][] initializeFX(GridPane gp) {
		gp.getChildren().clear(); // Clear the existing children from the GridPane
		Circle[][] rec = new Circle[size][size];
		int cellSize = 16; // Adjust this to your desired cell size

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				int cellValue = grid[j][i]; // Access the cell's value in the grid

				Circle cellRect = new Circle(cellSize);
				rec[i][j] = cellRect;
				if (cellValue == EMPTY) {
					rec[i][j].setFill(Color.WHITE);
				} else if (cellValue == PLAYER) {
					rec[i][j].setFill(Color.DARKGREEN);
				} else if (cellValue == POINT) {
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

	public void updateFX(Circle[][] rec) {
		// can update based on last thing happened for efficiency?
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid.length; j++) {
				int cellValue = grid[i][j];
				// can be range from 1 to 4 for ex.
				if (cellValue == EMPTY) {
					rec[i][j].setFill(Color.WHITE);
				} else if (cellValue == PLAYER) {
					rec[i][j].setFill(Color.DARKGREEN);
				} else if (cellValue == POINT) {
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
