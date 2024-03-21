package com.erimali.minigames;

import java.util.List;

public class MGChess {
	private char[][] board;
	private static final int DIFF = 'A' - 'a';
	private static final char WKING = 'K';
	private static final char WQUEEN = 'Q';
	private static final char WROOK = 'R';
	private static final char WBISHOP = 'B';
	private static final char WKNIGHT = 'N';
	private static final char WPAWN = 'P';
	private static final char BKING = 'k';
	private static final char BQUEEN = 'q';
	private static final char BROOK = 'r';
	private static final char BBISHOP = 'b';
	private static final char BKNIGHT = 'n';
	private static final char BPAWN = 'p';
	private static final char EMPTY = ' ';

	private boolean whiteTurn;
	private boolean playerTurn;
	private boolean isWhite; // = whiteTurn&&playerTurn at beginning

	public MGChess() {
		initializeBoard();
		isWhite = true;
		playerTurn = true;
	}

	public MGChess(boolean isWhite) {
		initializeBoard();
		this.isWhite = isWhite;
		playerTurn = isWhite;
	}

	public void initializeBoard() {
		board = new char[][] { //
				{ 'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r' }, //
				{ 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p' }, //
				{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, //
				{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, //
				{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, //
				{ ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, //
				{ 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P' }, //
				{ 'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R' } };//
	}

	private List<Character> BEaten;
	private List<Character> WEaten;

	public boolean move(int srcRow, int srcCol, int toRow, int toCol) {
		// can be blocked at JavaFX
		if (isLowerCase(board[srcRow][srcCol])) {
			if (isWhite)
				return false;
		} else {
			if (!isWhite)
				return false;
		}
		if (isValidMove(srcRow, srcCol, toRow, toCol)) {
			char toPiece = board[toRow][toCol];
			if (toPiece != EMPTY) {
				if (isLowerCase(toPiece))
					BEaten.add(toPiece);
				else
					WEaten.add(toPiece);
			}
			board[toRow][toCol] = board[srcRow][srcCol];
			board[srcRow][srcCol] = EMPTY;
		}
		return false;
	}

	public boolean isValidMove(int srcRow, int srcCol, int toRow, int toCol) {
		if (srcRow == toRow && srcCol == toCol)
			return false;
		char piece = board[srcRow][srcCol];
		char toPlace = board[toRow][toCol];
		switch (piece) {
		case WPAWN:
			if (srcCol == toCol && toPlace == EMPTY) {
				if (srcRow - toRow == 1)
					return true;
				else if (srcRow == 6 && toRow == 4)
					return true;
				else
					return false;
			} else if ((absVal(srcCol - toCol) == 1) && isEatablePiece(true, toPlace)) {
				return true;
			} else
				return false;
		case BPAWN:
			if (srcCol == toCol && toPlace == EMPTY) {
				if (toRow - srcRow == 1)
					return true;
				else if (srcRow == 1 && toRow == 3)
					return true;
				else
					return false;
			} else if ((absVal(srcCol - toCol) == 1) && isEatablePiece(false, toPlace)) {
				return true;
			} else
				return false;
		case WBISHOP:
		case BBISHOP:
			// Add logic for bishop moves
			if (absVal(srcRow - toRow) == absVal(srcCol - toCol)) {
				int rowDirection = (toRow - srcRow) / absVal(toRow - srcRow);
				int colDirection = (toCol - srcCol) / absVal(toCol - srcCol);
				int row = srcRow + rowDirection;
				int col = srcCol + colDirection;
				// || or &&
				while (row != toRow && col != toCol) {
					if (board[row][col] != EMPTY) {
						return false; // Blocked path
					}
					row += rowDirection;
					col += colDirection;
				}
				return true; // Valid diagonal move
			}
			return false;
		case WKNIGHT:
			int rowDiff = absVal(srcRow - toRow);
		    int colDiff = absVal(srcCol - toCol);
		    
		    if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
		        if (toPlace == EMPTY || isEatablePiece(true, toPlace)) {
		            return true; // Valid knight move (capture or no capture)
		        }
		    }
			return false;
		case BKNIGHT:
			//?????
			rowDiff = absVal(srcRow - toRow);
		    colDiff = absVal(srcCol - toCol);
		    if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
		        if (toPlace == EMPTY || isEatablePiece(false, toPlace)) {
		            return true; // Valid knight move (capture or no capture)
		        }
		    }
			return false;
		case WROOK:
		case BROOK:
			// Add logic for rook moves
			if (srcRow == toRow || srcCol == toCol) {
				if (srcRow == toRow) {
					int start = Math.min(srcCol, toCol);
					int end = Math.max(srcCol, toCol);
					for (int col = start + 1; col < end; col++) {
						if (board[srcRow][col] != EMPTY) {
							return false; // Blocked horizontal path
						}
					}
				} else {
					int start = Math.min(srcRow, toRow);
					int end = Math.max(srcRow, toRow);
					for (int row = start + 1; row < end; row++) {
						if (board[row][srcCol] != EMPTY) {
							return false; // Blocked vertical path
						}
					}
				}
				return true; // Valid rook move
			} 
			return false;
		case WQUEEN:
		case BQUEEN:
		    rowDiff = Math.abs(srcRow - toRow);
		    colDiff = Math.abs(srcCol - toCol);
		    
		    if ((srcRow == toRow || srcCol == toCol) || (rowDiff == colDiff)) {
		        int rowDirection = Integer.compare(toRow, srcRow);
		        int colDirection = Integer.compare(toCol, srcCol);
		        
		        int row = srcRow + rowDirection;
		        int col = srcCol + colDirection;
		        //while error
		        while (row != toRow || col != toCol) {
		            if (board[row][col] != EMPTY) {
		                return false; // Path is blocked
		            }
		            row += rowDirection;
		            col += colDirection;
		        }
		        
		        if (toPlace == EMPTY || isEatablePiece2(isWhite, toPlace)) {
		            return true; // Valid queen move (capture or no capture)
		        }
		    }
		    return false; // Invalid queen move
		}
		return false;//or this
	}

	public static int absVal(int a) {
		if (a < 0)
			return -a;
		return a;
	}

	public boolean isEatablePiece(boolean forWhite, char piece) {
		if (forWhite) {
			return piece == BQUEEN || piece == BROOK || piece == BBISHOP || piece == BKNIGHT || piece == BPAWN;
		} else {
			return piece == WQUEEN || piece == WROOK || piece == WBISHOP || piece == WKNIGHT || piece == WPAWN;
		}
	}

	public boolean isEatablePiece2(boolean forWhite, char piece) {
		if (forWhite) {
			return isLowerCase(piece) && piece != BKING;
		} else {
			return isUpperCase(piece) && piece != WKING;
		}
	}

	public static boolean isLowerCase(char c) {
		return c >= 'a' && c <= 'z';
	}

	public static boolean isUpperCase(char c) {
		return c >= 'A' && c <= 'Z';
	}
}
