package com.erimali.compute;

public abstract class EriString {
	public abstract void uppercaseFirstChar();

	public abstract EriWord getWord(int i);

	public abstract boolean isWord();

	public abstract char charAt(int i);

	public abstract char[] getChars();

	public abstract int length();

	public abstract void joinWith(EriString e);

	// if even after trim() contains whitespace -> sentence
	public EriString(String string) {
	};

	public static String numberToLetters(char c) {
		switch (c) {
		case '0':
			return "zero";
		case '1':
			return "one";
		case '2':
			return "two";
		case '3':
			return "three";
		case '4':
			return "four";
		case '5':
			return "five";
		case '6':
			return "six";
		case '7':
			return "seven";
		case '8':
			return "eight";
		case '9':
			return "nine";
		default:
			return String.valueOf(c);
		}
	}
}
