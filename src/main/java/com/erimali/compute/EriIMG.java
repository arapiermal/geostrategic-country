package com.erimali.compute;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class EriIMG {
	private Map<Character, Color> colorMap;
	private int pixelSize;
	private int width;
	private int height;
	private char[][] pixelArr;

	public EriIMG(int pixelSize, String colorMapInfo, String in) {
		this.colorMap = new HashMap<Character, Color>();
		this.pixelSize = pixelSize;
		String[] cRows = colorMapInfo.split("\\s*\r?\n\\s*");
		for (String cRow : cRows) {
			String[] cInfo = cRow.trim().split("\\s*=\\s*");
			colorMap.put(cInfo[0].charAt(0), Color.valueOf(cInfo[1]));
		}
		String[] inRows = in.split("\r?\n");
		this.height = inRows.length;
		pixelArr = new char[this.height][];
		// spaces transparency?

		this.width = 0;
		int i = 0;
		for (String inRow : inRows) {
			this.width = Math.max(this.width, inRow.length());
			pixelArr[i++] = inRow.toCharArray();
		}
	}

	private static boolean isNewSegment(String in) {
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i) == '~')
				return true;
			else if (!Character.isWhitespace(in.charAt(i)))
				return false;
		}
		return false;
	}

	public EriIMG(Path p) {
		try {
			List<String> list = Files.readAllLines(p);
			this.colorMap = new HashMap<Character,Color>();
			this.colorMap.put(' ', Color.TRANSPARENT);
			this.pixelSize = Integer.parseInt(list.get(0));
			int i = 2;
			while (i < list.size()&&!isNewSegment(list.get(i)) ) {
				if(list.get(i).isBlank()) {
					i++;
					continue;
				}
				String[] cInfo =  list.get(i).trim().split("\\s*=\\s*");
				colorMap.put(cInfo[0].charAt(0), Color.valueOf(cInfo[1]));
				i++;
			}
			i++; // skip ~
			this.height = list.size() - i;
			this.width = 0;
			pixelArr = new char[this.height][];
			int j = 0;
			// (String currLine = list.get(i))
			while (i < list.size()&&!isNewSegment(list.get(i)) ) {
				this.width = Math.max(this.width, list.get(i).length());
				pixelArr[j] = list.get(i).toCharArray();
				i++;
				j++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Stage generateStage() {
		Stage primaryStage = new Stage();
		GridPane gridPane = new GridPane();
		this.modifyGridPane(gridPane);
		Scene scene = new Scene(gridPane);
		primaryStage.setScene(scene);
		primaryStage.setTitle("EriIMG Display");
		return primaryStage;
	}

	public void modifyGridPane(GridPane gridPane) {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				char pixelChar;
				Color pixelColor;
				if(j >= pixelArr[i].length) {
					pixelColor = Color.TRANSPARENT;
				} else {
					pixelChar = pixelArr[i][j];
					pixelColor = colorMap.get(pixelChar);
				}
				
				Rectangle rectangle = new Rectangle(pixelSize, pixelSize, pixelColor);
				gridPane.add(rectangle, j, i);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("pixelSize=").append(this.pixelSize);
		sb.append("\n~\n");
		for (Map.Entry<Character, Color> entry : colorMap.entrySet()) {
			sb.append(entry.getKey()).append("=").append(entry.getValue().toString());
			sb.append("\n");
		}
		sb.append("~");
		for (char[] arrRow : this.pixelArr) {
			sb.append("\n");
			sb.append(arrRow);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		EriIMG e;
		// = new EriIMG(10, "R=red\r\n" + "G=green\r\n" + "C=#00FFFF",
				//"RRRRRRRRRRRRRRRRR\r\n" + "GGGGGGGGGGGGGGGGG\r\n" + "CCCCCCCCCCCCCCCCC");
		e = new EriIMG(Paths.get("im.txt"));
		System.out.println(e.toString());
		System.out.println(e.height + " " + e.width);
	}
}
