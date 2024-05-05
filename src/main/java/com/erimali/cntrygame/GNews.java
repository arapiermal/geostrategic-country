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
    private static final String DEFLOCATION = "resources/web/articles.txt";
    private static final String NEWS_LOCATION = "src/main/resources/web/news/";//"saveGames/news/";
    //same place SAVEGAME/news
    private BaseDate date; //BDate instead ?
    private String title;
    private String desc;
    private String[] paragraphs;
    private boolean appended;

    public GNews(BaseDate date, String title, String desc) {
        this.date = date;
        this.title = title;
        this.desc = desc;
    }

    public GNews(BaseDate date, String title, String desc, String... paragraphs) {
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

    //add the unique id of GLogic
    public void appendSelfToFile(String filePath) {
        appendNewsToFile(filePath, this);
        appended = true;
    }

    public void appendSelfToUniqueFile(String uniqueId) {
        appendNewsToFile(NEWS_LOCATION + uniqueId + ".txt", this);
        appended = true;
    }

    public String toString() {
        if (paragraphs == null)
            return date + "\n" + title + "\n" + desc;
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(this.date).append("\n").append(this.title).append("\n").append(this.desc);
            for (String p : paragraphs) {
                sb.append("\n").append(p);
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
                sb.append(separator).append(p);
            }
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        GNews n1 = new GNews(new BaseDate("7/7/2023"), "Hello", "paragraph here");
        GNews n2 = new GNews(new BaseDate(8, 7, 2023), "Bye", "paragraph here", "TEST", "TEST2");
        List<GNews> l = new LinkedList<>();
        l.add(n1);
        l.add(n2);
        GNews.appendManyNewsToFile("saveGames/news/articles.txt", l);
        // fix html
        // if last split empty
    }

    public BaseDate getDate() {
        return date;
    }

    public void setDate(BaseDate date) {
        this.date = date;
    }

    public boolean isAppended() {
        return appended;
    }

    public void setAppended(boolean appended) {
        this.appended = appended;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String[] getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(String[] paragraphs) {
        this.paragraphs = paragraphs;
    }
}
