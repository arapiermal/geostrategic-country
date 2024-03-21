package com.erimali.compute;

//copy pasted from EriSentence, change and FIX!!!!!!
public class EriText extends EriString {
	private EriSentence[] text;
	private boolean encoded;

	public EriText(String in) {
		super(in);
		String s[] = in.split("\\r?\\n");
		text = new EriSentence[s.length];
		for (int i = 0; i < s.length; i++) {
			text[i] = new EriSentence(s[i]);
		}
	}

	public void uppercaseFirstChar() {
		if (text.length > 0)
			text[0].uppercaseFirstChar();
	}

	public void uppercaseFirstChars() {
		for (EriSentence w : text) {
			w.uppercaseFirstChars();
		}
	}

	public EriSentence getSentence(int i) {
		if (i < 0 || i > text.length)
			throw new IllegalArgumentException("INVALID WORD INDEX");
		return text[i];
	}

	public char charAt(int i) {
		for (EriSentence s : text) {
			if (i > s.length()) {
				i -= s.length();
			} else {
				return s.charAt(i);
			}
		}
		return (char) 0;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < text.length - 1; i++) {
			sb.append(text[i].getChars());// for specialized stuff .append(" ")
		}
		sb.append(text[i].getChars());
		return sb.toString();
	}

	public void encDcdXOR(String key) {
		encoded = !encoded;
		for (EriSentence w : text) {
			w.encDcdXOR(key);
		}
	}

	public boolean isEncoded() {
		return encoded;
	}

	public EriSentence[] getWords() {
		return text;
	}

	public char[] getChars() {
		char[] all = new char[calcLength()];
		int curr = 0;
		for (EriSentence w : text) {
			System.arraycopy(w.getChars(), 0, all, curr, w.length());
			curr += w.length();
		}
		return all;
	}

	public int length() {
		return calcLength();
	}

	public int calcLength() {
		int l = 0;
		for (EriSentence w : text) {
			l += w.length();
		}
		return l;
	}

	public boolean equals(Object o) {
		if (o instanceof EriText) {
			char[] w = ((EriText) o).getChars();
			char[] th = this.getChars();
			if (th.length != w.length)
				return false;
			for (int i = 0; i < w.length; i++) {
				if (th[i] != w[i])
					return false;
			}
			return true;
		} else if (o instanceof EriSentence) {
			char[] w = ((EriSentence) o).getChars();
			char[] th = this.getChars();
			if (th.length != w.length)
				return false;
			for (int i = 0; i < w.length; i++) {
				if (th[i] != w[i])
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isWord() {
		return false;
	}

	public int size() {
		return text.length;
	}

	// change!!!!!!!!
	@Override
	public EriWord getWord(int i) {
		for (EriSentence s : text) {
			if (i > s.length()) {
				i -= s.length();
			} else {
				return s.getWord(i);
			}
		}
		return null;
	}
	@Override
	public void joinWith(EriString e) {
		//INEFFICIENT
		EriSentence s = new EriSentence(new String(e.getChars()));
		
	}
}
