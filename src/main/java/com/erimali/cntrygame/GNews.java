package com.erimali.cntrygame;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

//For big news -> WW3
public class GNews implements Serializable {
	private static final String SEPARATOR = "~~~";
	//copy paste on new game?
	private static final String DEFLOCATION = "resources/web/articles.txt";
	//same place SAVEGAME/news
	private GDate date; //BDate instead ?
	private String title;
	private String desc;
	private String[] paragraphs;
	private boolean appended;

	public GNews(GDate date, String title, String desc) {
		this.date = date;
		this.title = title;
		this.desc = desc;
	}

	public GNews(GDate date, String title, String desc, String... paragraphs) {
		this.date = date;
		this.title = title;
		this.desc = desc;
		this.paragraphs = paragraphs;
	}

	public static void appendManyNewsToFile(String filePath, List<GNews> news) {
		try {
			StringBuilder sb = new StringBuilder();
			for (GNews temp : news) {
				sb.append(temp.toStringForFile()).append(SEPARATOR);
			}

			Files.write(Path.of(filePath), sb.toString().getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);

		} catch (IOException e) {
			//CHANGE
			System.out.println("An error occurred while appending the content to the file: " + e.getMessage());
		}
	}

	public static void appendNewsToFile(String filePath, GNews news) {
		try {
			Files.write(Path.of(filePath), (news.toStringForFile() + SEPARATOR).getBytes(), StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);

		} catch (IOException e) {
			System.out.println("An error occurred while appending the content to the file: " + e.getMessage());
		}
	}
	public void appendSelfToFile(String filePath) {
		appendNewsToFile(filePath,this);
		appended = true;
	}
	public void appendSelfToFile() {
		appendNewsToFile(DEFLOCATION,this);
		appended = true;
	}
	public String toString() {
		if (paragraphs == null)
			return date + "\n" + title + "\n" + desc;
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(this.date).append("\n").append(this.title).append("\n").append(this.desc);
			for (String p : paragraphs) {
				sb.append(p).append("\n");
			}
			return sb.toString();
		}
	}
	public String toStringForFile() {
		String separator = System.lineSeparator();
		if (paragraphs == null)
			return date + separator + title + separator + desc;
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(date).append(separator).append(title).append(separator).append(desc);
			for (String p : paragraphs) {
				sb.append(p).append(separator);
			}
			return sb.toString();
		}
	}

	public static void main(String[] args) {
		GNews n1 = new GNews(new GDate("7/7/2023"), "Hello", "paragraph here");
		GNews n2 = new GNews(new GDate(8, 7, 2023), "Bye", "paragraph here");
		List<GNews> l = new LinkedList<GNews>();
		l.add(n1);
		l.add(n2);
		GNews.appendManyNewsToFile("resources/web/articles.txt", l);
		// fix html
		// if last split empty
	}

	public GDate getDate() {
		return date;
	}

	public void setDate(GDate date) {
		this.date = date;
	}

	public boolean isAppended() {
		return appended;
	}

	public void setAppended(boolean appended) {
		this.appended = appended;
	}
}
