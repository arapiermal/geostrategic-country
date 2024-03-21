package com.erimali.minigames;

import com.erimali.cntrygame.TESTING;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

// baseVal^11
public class MG2048 {
	private static final int DEF_SIZE = 4;
	private static final int DEF_BASEVAL = 2;
	private int baseVal;
	private int board[][];
	private int size;
	private int score;

	public MG2048() {
		this.size = DEF_SIZE;
		this.baseVal = DEF_BASEVAL;
		this.board = new int[size][size];
		initBoard();
	}

	public MG2048(int size) {
		this.size = size;
		this.baseVal = DEF_BASEVAL;
		this.board = new int[size][size];
		initBoard();
	}

	public MG2048(int size, int baseVal) {
		this.size = size;
		this.baseVal = baseVal;
		this.board = new int[size][size];
		initBoard();
	}

	public static void main(String[] args) {
		MG2048 g = new MG2048(4);
		TESTING.print(g);
		g.move(Direction.DOWN);
		TESTING.print(g);
		g.move(Direction.RIGHT);
		TESTING.print(g);
	}

	public void initBoard() {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				board[r][c] = 0;
			}
		}
		genBlock();
		genBlock();
	}

	public boolean move(Direction dir) {
		moveBlocks(dir);
		return genBlockRand();
	}

	public void genBlock() {
		outerLoop: for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				if (board[r][c] == 0) {
					board[r][c] = baseVal;
					break outerLoop; // WOW
					// or just return;
				}
			}
		}
	}

	public boolean genBlockRand() {
		if (isFull()) {
			if (hasValidMovesWhenFull())
				return false;
			else
				return true; // game ended
		}
		int r, c;
		do {
			r = (int) (Math.random() * size);
			c = (int) (Math.random() * size);
		} while (board[r][c] != 0);
		board[r][c] = Math.random() < 0.8 ? baseVal : baseVal * baseVal;
		return false;
	}

	public boolean isFull() {
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				if (board[r][c] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean hasValidMovesWhenFull() {
		for (int r = 0; r < size - 1; r++) {
			for (int c = 0; c < size - 1; c++) {
				if (board[r][c] == board[r + 1][c]) {
					return true;
				}
				if (board[r][c] == board[r][c + 1]) {
					return true;
				}
			}
		}
		return false;
	}

	public void moveBlocks(Direction dir) {
		int x;
		int y;
		switch (dir) {
		case UP:
			x = 0;
			y = -1;
			// from uppermost to lowermost
			for (int r = 1; r < size; r++) {
				for (int c = 0; c < size; c++) {
					if (board[r][c] != 0) {
						moveBlock(r, c, y, x);
					}
				}
			}
			break;
		case RIGHT:
			x = 1;
			y = 0;
			for (int r = 0; r < size; r++) {
				for (int c = size - 2; c > -1; c--) {
					if (board[r][c] != 0) {
						moveBlock(r, c, y, x);
					}
				}
			}
			break;
		case DOWN:
			x = 0;
			y = 1;
			for (int r = size - 2; r > -1; r--) {
				for (int c = 0; c < size; c++) {
					if (board[r][c] != 0) {
						moveBlock(r, c, y, x);
					}
				}
			}
			break;
		case LEFT:
			x = -1;
			y = 0;
			for (int r = 0; r < size; r++) {
				for (int c = 1; c < size; c++) {
					if (board[r][c] != 0) {
						moveBlock(r, c, y, x);
					}
				}
			}
			break;
		default:
			x = 0;
			y = 0;
			break;
		}

	}

	public void moveBlock(int r, int c, int dirY, int dirX) {
		if ((r == 0 && dirY == -1) || (r == size - 1 && dirY == 1))
			return;
		else if ((c == 0 && dirX == -1) || (c == size - 1 && dirX == 1))
			return;
		int val = board[r][c];
		boolean added = false;
		if (dirX != 0) {
			int col = c + dirX;
			while (col > -1 && col < size) {
				if (board[r][col] == 0)
					col += dirX;
				else if (val == board[r][col]) {
					board[r][col] += val;
					board[r][c] = 0;
					added = true;
					break;
				}
				// just else?
				else if (board[r][col] != 0) {
					int colNew = col - dirX;
					if (colNew != c) {
						board[r][colNew] = val;
						board[r][c] = 0;
					}
					break;
				}
			}
			if (board[r][col - dirX] == 0 && !added) {
				board[r][col - dirX] = val;
				board[r][c] = 0;
			}
		} else if (dirY != 0) {
			int row = r + dirY;
			while (row > -1 && row < size) {
				if (board[row][c] == 0)
					row += dirY;
				else if (val == board[row][c]) {
					board[row][c] += val;
					board[r][c] = 0;
					added = true;
					break;
				} else if (board[row][c] != 0) {
					int rowNew = row - dirY;
					if (rowNew != r) {
						board[rowNew][c] = val;
						board[r][c] = 0;
					}
					break;
				}
			}
			if (board[row - dirY][c] == 0 && !added) {
				board[row - dirY][c] = val;
				board[r][c] = 0;
			}
		}
		if (added) {
			score += val + val;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int r = 0; r < board.length; r++) {
			for (int c = 0; c < board[r].length; c++) {
				sb.append(String.valueOf(board[r][c])).append("\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public static int log2(int val) {
		if (val <= 0)
			return 0;
		int l = 0;
		while (val > 1) {
			val /= 2;
			l++;
		}
		return l;
	}

	public static int logAny(int val, int any) {
		if (val <= 0)
			return 0;
		return (int) (Math.log(val) / Math.log(any));
	}

	private static final Color EMPTY_COLOR = Color.WHITE;
	public static final Color[] DEFAULT_COLORS = { Color.rgb(238, 228, 218), // 2
			Color.rgb(237, 224, 200), // 4
			Color.rgb(242, 177, 121), // 8
			Color.rgb(245, 149, 99), // 16
			Color.rgb(246, 124, 95), // 32
			Color.rgb(246, 94, 59), // 64
			Color.rgb(237, 207, 114), // 128
			Color.rgb(237, 204, 97), // 256
			Color.rgb(237, 200, 80), // 512
			Color.rgb(237, 197, 63), // 1024
			Color.rgb(237, 194, 46), // 2048
			// Additional colors for higher values can be added here
	};

	public static Color getColor(int value) {
		return DEFAULT_COLORS[log2(value) - 1];
	}

	public Tile[][] initFX(GridPane gp) {
		Tile[][] tiles = new Tile[size][size];
		for (int r = 0; r < size; r++) {
			for (int c = 0; c < size; c++) {
				Tile tile = new Tile(board[r][c]);
				tiles[r][c] = tile;
				gp.add(tile.getRectangle(), c, r);
				gp.add(tile.getLabel(), c, r);
			}
		}
		return tiles;
	}

	public void updateFX(Tile[][] tiles) {
		for (int r = 0; r < size; r++) {
			for (int c = 0; c < size; c++) {
				tiles[r][c].updateValue(board[r][c]);
				;

			}
		}
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int maxVal() {
		return (int) Math.pow(baseVal, 11);
	}

	public static Color getEmptyColor() {
		return EMPTY_COLOR;
	}
}

class Tile {
	private int value;
	private Rectangle rectangle;
	private Label label;

	public Tile(int value) {
		this.value = value;
		this.rectangle = new Rectangle(100, 100); // Customize size and color
		this.label = createLabel();
		if (value == 0)
			rectangle.setFill(MG2048.getEmptyColor());
		else
			rectangle.setFill(MG2048.getColor(value));
	}

	private Label createLabel() {
		Label newLabel = new Label(Integer.toString(value));
		newLabel.setFont(new Font(24)); // Customize font size
		// newLabel.setAlignment(Pos.CENTER);
		return newLabel;
	}

	public void updateValue(int newValue) {
		value = newValue;
		label.setText(Integer.toString(value));
		if (newValue == 0)
			rectangle.setFill(MG2048.getEmptyColor());
		else
			rectangle.setFill(MG2048.getColor(value));
	}

	public int getValue() {
		return value;
	}

	public Rectangle getRectangle() {
		return rectangle;
	}

	public Label getLabel() {
		return label;
	}
}