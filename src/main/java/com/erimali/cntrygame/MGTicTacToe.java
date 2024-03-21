package com.erimali.cntrygame;

import java.util.Random;

public class MGTicTacToe {
	private static final int SIZE = 3;
	private static final int MIDDLE = SIZE / 2;
	private static final int EMPTY = 0;
	private static final int PLAYER = 1;
	private static final int OPPONENT = -1;
	private static final int ONGOING = -2;
	private static final int DRAW = 0;
	private static final int INVALID = 100;
	private static final String X = "X";
	private static final String O = "O";
	private static final String SPACE = " ";
	private int[][] board;
	private int[] lastPlayerMove = { -1, -1 };
	private int[] lastOpponentMove = { -1, -1 };
	private String gameResult;
	public boolean hasFinished;
	// private boolean playerIsX;
	private boolean playerTurn;
	private boolean turn;
	private int difficultyAI;

	public MGTicTacToe() {
		board = new int[SIZE][SIZE];
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				board[row][col] = EMPTY;
			}
		}
		this.playerTurn = true;
		this.turn = true;
	}

	public MGTicTacToe(boolean playerTurn, int difficultyAI) {
		board = new int[SIZE][SIZE];
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				board[row][col] = EMPTY;
			}
		}
		this.playerTurn = playerTurn;
		this.turn = true;
		this.difficultyAI = difficultyAI;
		if(!playerTurn)
			moveAI();
	}

	public int play(int row, int col) {
		if (hasFinished || row < 0 || row >= SIZE || col < 0 || col >= SIZE)
			return INVALID;
		if (board[row][col] == EMPTY) {
			board[row][col] = turn ? PLAYER : OPPONENT;
			if (playerTurn) {
				lastPlayerMove[0] = row;
				lastPlayerMove[1] = col;
			}
			playerTurn = !playerTurn;
			turn = !turn;
		} else {
			return INVALID;
		}
		int check = checkGame(row, col);
		if(check != ONGOING) {
			hasFinished = true;
			switch(check) {
			case DRAW:
				setGameResult("DRAW");
				break;
			case PLAYER:
				setGameResult(X + " WON");
				break;
			case OPPONENT:
				setGameResult(O + " WON");
				break;
			}
		}
		return check;
	}

	public int playCheckAll(int row, int col, boolean isPlayer) {
		if (row < 0 || row >= SIZE || col < 0 || col >= SIZE)
			return INVALID;
		if (board[row][col] == EMPTY) {
			board[row][col] = isPlayer ? PLAYER : OPPONENT;
		} else {
			return INVALID;
		}
		return checkGame();
	}

	public int checkGame(int row, int col) {
		int result = checkRow(row);
		if (result != ONGOING) {
			
			return result;
		}
		result = checkCol(col);
		if (result != ONGOING) {
			return result;
		}
		if (row == col) {
			result = checkDiagonal(true);
			if (result != ONGOING) {
				return result;
			}
		}
		if (SIZE - 1 - row == col) {
			result = checkDiagonal(false);
			if (result != ONGOING) {
				return result;
			}
		}
		if (isFull()) {
			return DRAW;
		}
		return ONGOING;
	}

	public int checkGame() {
		int result;
		for (int row = 0; row < SIZE; row++) {
			result = checkRow(row);
			if (result != ONGOING) {
				return result;
			}

		}
		for (int col = 0; col < SIZE; col++) {
			result = checkCol(col);
			if (result != ONGOING) {
				return result;
			}
		}
		result = checkDiagonals();
		if (result != ONGOING) {
			return result;
		}
		if (isFull()) {
			return DRAW;
		}
		return ONGOING;
	}

	public int checkRow(int row) {
		int playerCount = 0;
		int opponentCount = 0;
		for (int col = 0; col < SIZE; col++) {
			if (board[row][col] == PLAYER) {
				playerCount++;
			} else if (board[row][col] == OPPONENT) {
				opponentCount++;
			}
		}
		if (playerCount == SIZE) {
			return PLAYER;
		} else if (opponentCount == SIZE) {
			return OPPONENT;
		} else {
			return ONGOING;
		}
	}

	public int checkCol(int col) {
		int playerCount = 0;
		int opponentCount = 0;
		for (int row = 0; row < SIZE; row++) {
			if (board[row][col] == PLAYER) {
				playerCount++;
			} else if (board[row][col] == OPPONENT) {
				opponentCount++;
			}
		}
		if (playerCount == SIZE) {
			return PLAYER;
		} else if (opponentCount == SIZE) {
			return OPPONENT;
		} else {
			return ONGOING;
		}
	}

	public int checkDiagonals() {
		int playerCount = 0;
		int opponentCount = 0;

		// Check main diagonal
		for (int i = 0; i < SIZE; i++) {
			if (board[i][i] == PLAYER) {
				playerCount++;
			} else if (board[i][i] == OPPONENT) {
				opponentCount++;
			}
		}
		if (playerCount == SIZE) {
			return PLAYER;
		} else if (opponentCount == SIZE) {
			return OPPONENT;
		}

		playerCount = 0;
		opponentCount = 0;

		// Check secondary diagonal
		for (int i = 0; i < SIZE; i++) {
			if (board[i][SIZE - i - 1] == PLAYER) {
				playerCount++;
			} else if (board[i][SIZE - i - 1] == OPPONENT) {
				opponentCount++;
			}
		}
		if (playerCount == SIZE) {
			return PLAYER;
		} else if (opponentCount == SIZE) {
			return OPPONENT;
		} else {
			return ONGOING;
		}
	}

	public int checkDiagonal(boolean isFirst) {
		int playerCount = 0;
		int opponentCount = 0;
		if (isFirst) {
			for (int i = 0; i < SIZE; i++) {
				if (board[i][i] == PLAYER) {
					playerCount++;
				} else if (board[i][i] == OPPONENT) {
					opponentCount++;
				}
			}
			if (playerCount == SIZE) {
				return PLAYER;
			} else if (opponentCount == SIZE) {
				return OPPONENT;
			} else {
				return ONGOING;
			}
		} else {
			for (int i = 0; i < SIZE; i++) {
				if (board[i][SIZE - i - 1] == PLAYER) {
					playerCount++;
				} else if (board[i][SIZE - i - 1] == OPPONENT) {
					opponentCount++;
				}
			}
			if (playerCount == SIZE) {
				return PLAYER;
			} else if (opponentCount == SIZE) {
				return OPPONENT;
			} else {
				return ONGOING;
			}
		}
	}

	public boolean isFull() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col] == EMPTY) {
					return false;
				}
			}
		}
		return true;
	}
	public String boardPieceToString(int row, int col) {
		switch(board[row][col]) {
		case PLAYER:
			return X;
		case OPPONENT:
			return O;
		default:
			return SPACE;
		}
	}
	public int[] superEasyAI() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col] == EMPTY) {
					return new int[] { row, col };
				}
			}
		}
		return null;
	}

	public int[] nextToPlayerAI() {
		if (lastPlayerMove[0] == -1 || (board[MIDDLE][MIDDLE] == 0))
			return new int[] { MIDDLE, MIDDLE };
		int col = lastPlayerMove[1];
		for (int row = lastPlayerMove[0]; row < SIZE; row++) {
			while (col < SIZE) {
				if (board[row][col] == EMPTY)
					return new int[] { row, col };
				col++;
			}
			col = 0;
		}
		return superEasyAI();
	}

	public int moveAI(int type) {
		switch (type) {
		case 0:
			setLastOpponentMove(superEasyAI());
			break;
		case 1:
			setLastOpponentMove(nextToPlayerAI());
			break;
		case 2:
			setLastOpponentMove(mediumAI());
			break;
		default:
			setLastOpponentMove(superEasyAI());
		}
		return play(lastOpponentMove[0], lastOpponentMove[1]);
	}
	public int moveAI() {
		return moveAI(difficultyAI);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				int temp = board[i][j];
				if (temp == PLAYER)
					sb.append(" X ");
				else if (temp == OPPONENT)
					sb.append(" O ");
				else
					sb.append("   ");
				if (j < SIZE - 1)
					sb.append("|");
			}
			if (i < SIZE - 1) {
				sb.append("\n");
				for (int k = 0; k < SIZE - 1; k++) {
					sb.append("----");
				}
				sb.append("---");
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public static int getInvalid() {
		return INVALID;
	}

	public static int getDraw() {
		return DRAW;
	}

	public static int getOngoing() {
		return ONGOING;
	}

	public static int getOpponent() {
		return OPPONENT;
	}

	public static int getPlayer() {
		return PLAYER;
	}

	public static int getSize() {
		return SIZE;
	}

	public static int getEmpty() {
		return EMPTY;
	}
	public static int getMiddle() {
		return MIDDLE;
	}
	public int[] mediumAI() {
		// Check for a winning move
		int[] winningMove = findWinningMove(OPPONENT);
		if (winningMove != null) {
			return winningMove;
		}

		// Check for a blocking move
		int[] blockingMove = findWinningMove(PLAYER);
		if (blockingMove != null) {
			return blockingMove;
		}

		// Center position -> Default if available
		if (board[SIZE / 2][SIZE / 2] == EMPTY) {
			return new int[] { SIZE / 2, SIZE / 2 };
		}

		// Complete line or block the player
		int[] move = findLineOrBlockMove(OPPONENT);
		if (move != null) {
			return move;
		}

		// Random move
		return getRandomMove();
	}

	private int[] findWinningMove(int player) {
		// Check rows
		for (int row = 0; row < SIZE; row++) {
			int count = 0;
			int emptyCol = -1;
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col] == player) {
					count++;
				} else if (board[row][col] == EMPTY) {
					emptyCol = col;
				}
			}
			if (count == SIZE - 1 && emptyCol != -1) {
				return new int[] { row, emptyCol };
			}
		}

		// Check columns
		for (int col = 0; col < SIZE; col++) {
			int count = 0;
			int emptyRow = -1;
			for (int row = 0; row < SIZE; row++) {
				if (board[row][col] == player) {
					count++;
				} else if (board[row][col] == EMPTY) {
					emptyRow = row;
				}
			}
			if (count == SIZE - 1 && emptyRow != -1) {
				return new int[] { emptyRow, col };
			}
		}

		// Check diagonals
		int count = 0;
		int emptyIndex = -1;
		for (int i = 0; i < SIZE; i++) {
			if (board[i][i] == player) {
				count++;
			} else if (board[i][i] == EMPTY) {
				emptyIndex = i;
			}
		}
		if (count == SIZE - 1 && emptyIndex != -1) {
			return new int[] { emptyIndex, emptyIndex };
		}

		count = 0;
		emptyIndex = -1;
		for (int i = 0; i < SIZE; i++) {
			if (board[i][SIZE - i - 1] == player) {
				count++;
			} else if (board[i][SIZE - i - 1] == EMPTY) {
				emptyIndex = i;
			}
		}
		if (count == SIZE - 1 && emptyIndex != -1) {
			return new int[] { emptyIndex, SIZE - emptyIndex - 1 };
		}

		return null;
	}

	private int[] findLineOrBlockMove(int player) {
		// Check rows
		for (int row = 0; row < SIZE; row++) {
			int count = 0;
			int emptyCol = -1;
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col] == player) {
					count++;
				} else if (board[row][col] == EMPTY) {
					emptyCol = col;
				}
			}
			if (count == SIZE - 2 && emptyCol != -1) {
				return new int[] { row, emptyCol };
			}
		}

		// Check columns
		for (int col = 0; col < SIZE; col++) {
			int count = 0;
			int emptyRow = -1;
			for (int row = 0; row < SIZE; row++) {
				if (board[row][col] == player) {
					count++;
				} else if (board[row][col] == EMPTY) {
					emptyRow = row;
				}
			}
			if (count == SIZE - 2 && emptyRow != -1) {
				return new int[] { emptyRow, col };
			}
		}

		// Check diagonals
		int count = 0;
		int emptyIndex = -1;
		for (int i = 0; i < SIZE; i++) {
			if (board[i][i] == player) {
				count++;
			} else if (board[i][i] == EMPTY) {
				emptyIndex = i;
			}
		}
		if (count == SIZE - 2 && emptyIndex != -1) {
			return new int[] { emptyIndex, emptyIndex };
		}

		count = 0;
		emptyIndex = -1;
		for (int i = 0; i < SIZE; i++) {
			if (board[i][SIZE - i - 1] == player) {
				count++;
			} else if (board[i][SIZE - i - 1] == EMPTY) {
				emptyIndex = i;
			}
		}
		if (count == SIZE - 2 && emptyIndex != -1) {
			return new int[] { emptyIndex, SIZE - emptyIndex - 1 };
		}

		return null;
	}

	private int[] getRandomMove() {
		Random random = new Random();
		int row, col;
		do {
			row = random.nextInt(SIZE);
			col = random.nextInt(SIZE);
		} while (board[row][col] != EMPTY);

		return new int[] { row, col };
	}

	public int[] getLastOpponentMove() {
		return lastOpponentMove;
	}

	public void setLastOpponentMove(int[] lastOpponentMove) {
		this.lastOpponentMove = lastOpponentMove;
	}

	public String getGameResult() {
		return gameResult;
	}

	public void setGameResult(String gameResult) {
		this.gameResult = gameResult;
	}
}
