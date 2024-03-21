package com.erimali.compute;

import java.util.Arrays;

public class EriWord extends EriString {
	private char[] string;
	private boolean encoded;

	public EriWord(String in) {
		super(in);
		string = in.toCharArray();
	}

	public EriWord(char... in) {
		super(new String(in));
		string = in;
	}

	public void changeChar(int i, char c) {
		if (i < 0 || i > string.length)
			throw new IllegalArgumentException("INVALID CHAR INDEX");
		string[i] = c;
	}

	public void appendChars(String s) {
		appendChars(s.toCharArray());
	}

	public void appendChars(char... c) {
		char[] temp = string;
		string = new char[temp.length + c.length];
		System.arraycopy(temp, 0, string, 0, temp.length);
		System.arraycopy(c, 0, string, temp.length, c.length);
	}

	public void appendChars(int i, String s) {
		appendChars(i, s.toCharArray());
	}

	public void appendChars(int i, char... c) {
		char[] temp = string;
		string = new char[temp.length + c.length];
		System.arraycopy(temp, 0, string, 0, i);
		System.arraycopy(c, 0, string, i, c.length);
		System.arraycopy(temp, i, string, i + c.length, temp.length - i);
	}

	public void removeChar(int... index) {
		if (index.length == 0)
			return;
		else if (index.length > 1)
			Arrays.sort(index);
		char[] temp = string;
		string = new char[temp.length - index.length];
		int k = 0;
		int j = 0;
		for (int i = 0; i < temp.length; i++) {
			if (k < index.length && i == index[k]) {
				k++;
			} else {
				string[j] = temp[i];
				j++;
			}
		}
	}

	public void removePostChars(int start) {
		removeChars(start, string.length);
	}

	public void removePreChars(int end) {
		removeChars(0, end);
	}

	public void removeChars(int start, int end) {
		if (start < 0 || end >= string.length || start > end) {
			throw new IllegalArgumentException("INVALID START OR END INDEX");
		}
		int removedLength = end - start;
		char[] temp = string;
		string = new char[temp.length - removedLength];
		System.arraycopy(temp, 0, string, 0, start);
		System.arraycopy(temp, end, string, start, temp.length - end);
	}

	public char charAt(int i) {
		return string[i];
	}

	public void uppercaseFirstChar() {
		if (string == null || string.length == 0)
			return;
		string[0] = Character.toUpperCase(string[0]);
	}

	public String toString() {
		if (string == null)
			return "NULL";
		StringBuilder sb = new StringBuilder();
		for (char c : string) {
			sb.append(c);
		}
		return sb.toString();
	}

	public char[] getChars() {
		return string;
	}

	public int length() {
		return string.length;
	}

	public EriWord getWord(int i) {
		return this;
	}

	public boolean isEmpty() {
		return string == null || string.length == 0;
	}

	public boolean isBlank() {
		if (isEmpty())
			return true;
		for (int i = 0; i < string.length; i++) {
			if (Character.isWhitespace(string[i]))
				return true;
		}
		return false;
	}

	// Encoding and decoding using XOR is symmetric, so we can just use the encode
	// method
	public void encDcdXOR(String key) {
		encoded = !encoded;
		char[] keyChars = key.toCharArray();
		for (int i = 0; i < string.length; i++) {
			string[i] = (char) (string[i] ^ keyChars[i % keyChars.length]);
		}
	}

	public boolean isEncoded() {
		return encoded;
	}

	public void toUpperCase() {
		for (int i = 0; i < string.length; i++) {
			if (Character.isLowerCase(string[i])) {
				string[i] = Character.toUpperCase(string[i]);
			}
		}
	}

	public boolean isPunctuation() {
		int k = 0;
		for (int i = 0; i < string.length; i++) {
			if (Character.getType(string[i]) == Character.OTHER_PUNCTUATION)
				k++;
		}
		if (k == string.length)
			return true;
		else
			return false;
	}

	public void toLowerCase() {
		for (int i = 0; i < string.length; i++) {
			if (Character.isUpperCase(string[i])) {
				string[i] = Character.toLowerCase(string[i]);
			}
		}
	}

	public boolean equals(Object o) {
		if (o instanceof EriWord) {
			char[] w = ((EriWord) o).getChars();
			if (string.length != w.length)
				return false;
			for (int i = 0; i < w.length; i++) {
				if (string[i] != w[i])
					return false;
			}
			return true;
		} else if (o instanceof EriSentence) {
			char[] w = ((EriSentence) o).getChars();
			if (string.length != w.length)
				return false;
			for (int i = 0; i < w.length; i++) {
				if (string[i] != w[i])
					return false;
			}
			return true;
		}
		return false;
	}
	@Override
	public void joinWith(EriString e) {
		appendChars(e.getChars());
	}
	@Override
	public boolean isWord() {
		return true;
	}
}
