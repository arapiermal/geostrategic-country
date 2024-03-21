package com.erimali.compute;

public class EriSentence extends EriString{
	private EriWord[] sentence;
	private boolean encoded;
	
	public EriSentence(String in) {
		super(in);
		//punctuation marks and spaces as words in themselves how to
		//String s[] = in.split("\\s+|(?=[.,!?])|(?<=[.,!?])");
		//String s[] = in.split("(?<=\\s)(?=\\S)|(?<=\\S)(?=\\s)");
		String s[] = in.split("(?<=\\s)(?=\\S)|(?<=\\S)(?=\\s)|(?<=\\S)(?=[.,!?])|(?<=[.,!?])(?=\\S)");

		sentence = new EriWord[s.length];
		for (int i = 0; i < s.length; i++) {
			sentence[i] = new EriWord(s[i]);
		}
	}

	public void uppercaseFirstChar() {
		if (sentence.length > 0)
			sentence[0].uppercaseFirstChar();
	}

	public void uppercaseFirstChars() {
		for(EriWord w : sentence) {
			w.uppercaseFirstChar();
		}
	}
	public EriWord getWord(int i) {
		if (i < 0 || i > sentence.length)
			throw new IllegalArgumentException("INVALID WORD INDEX");
		return sentence[i];
	}
	public char charAt(int i) {		
		for(EriWord w : sentence) {
			if(i > w.length()) {
				i -= w.length();
			} else {
				return w.charAt(i);
			}
		}
		return (char) 0;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < sentence.length - 1; i++) {
			sb.append(sentence[i].getChars());//for specialized stuff .append(" ")
		}
		sb.append(sentence[i].getChars());
		return sb.toString();
	}
	public void encDcdXOR(String key) {
		encoded = !encoded;
		for(EriWord w : sentence) {
			w.encDcdXOR(key);
		}
	}
	public boolean isEncoded() {
		return encoded;
	}

	public EriWord[] getWords() {
		return sentence;
	}
	public char[] getChars() {
		char[] all = new char[calcLength()];
		int curr = 0;
		for(EriWord w : sentence) {
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
		for(EriWord w : sentence) {
			l+=w.length();
		}
		return l;
	}
	public boolean equals(Object o) {
		if(o instanceof EriSentence) {
			char[] w = ((EriSentence) o).getChars();
			char[] th = this.getChars();
			if(th.length != w.length)
				return false;
			for(int i = 0; i < w.length; i++) {
				if(th[i] != w[i])
					return false;
			}
			return true;
		} else if(o instanceof EriWord) {
			char[] w = ((EriWord) o).getChars();
			char[] th = this.getChars();
			if(th.length != w.length)
				return false;
			for(int i = 0; i < w.length; i++) {
				if(th[i] != w[i])
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
		return sentence.length;
	}
	public void appendWords(EriWord... w) {
		
	}
	@Override
	public void joinWith(EriString e) {
		//INEFFICIENT
		EriSentence s = new EriSentence(new String(e.getChars()));
		
	}
}
