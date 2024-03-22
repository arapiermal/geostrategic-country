package com.erimali.cntrygame;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMap {
	protected static double mapWidth = 12200;
	protected static double mapHeight = 6149.8;
	// can make Color and simply take care when adding/changing/removing
	private Map<String, String> colors;// save as OCEAN,#ADD8E6?
	private Color backgroundColor = Color.valueOf("#ADD8E6");
	private Color colDef = new Color(0, 0, 0, 0);
	private String playerCountry;
	private String defColorString = "#ececec";
	private Color defColor = Color.valueOf(defColorString);
	private Color defColorCountry = new Color(0, 0, 0, 0);
	private String defBorderColorString = "#000000";
	private Paint defBorderColor = Paint.valueOf(defBorderColorString);
	private String defSubBorderColorString = "#FFFFFF";
	private Paint defSubBorderColor = Paint.valueOf(defSubBorderColorString);
	// default fill of country svg -> alpha 0

	private String defaultAllyColor = "blue";
	private Group mapGroup;
	private Group countryGroup;

	// CURSOR

	private SVGProvince[] mapSVG;// all divisions
//set/remove fill to countries when that mode

	private GameStage gs;

	private int mapMode;

	private int lastClickedProvince;

	public WorldMap(GameStage gs) {
		loadColors();
		this.gs = gs;
	}
	// SET PLAYER COUNTRY

	public WorldMap(String playerCountry, GameStage gs) {
		this.setPlayerCountry(playerCountry);
		loadColors();
		this.gs = gs;
	}

	public ScrollPane start() {
		try  (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/map/mcBig.svg"))){
			// Load SVG file

			List<SVGProvince> svgPaths = new ArrayList<>(); //LINKED LIST MORE EFFICIENT?!? SINCE WILL BE CONVERTED TO ARRAY?
			//List<SVGPath> countryPaths = new ArrayList<>();
			String line;
			int currProvId = 0;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("<path")) {
					int j = 10;
					while (j < line.length() && line.charAt(j) != '"') {
						j++;
					}
					String pathId = line.substring(10, j-3);
					String pathOwn = line.substring(j-2,j);
					//TESTING.print(pathId,pathOwn);
					j += 5;
					int beginPath = j;
					while (j < line.length() && line.charAt(j) != '"') {
						j++;
					}
					String pathData = line.substring(beginPath, j);
					//TESTING.print(pathData);
					try {

						SVGProvince svgPath = new SVGProvince(CountryArray.getIndex(pathOwn),currProvId++);
						svgPath.setId(pathId);
						svgPath.setAccessibleText(pathOwn);
						svgPath.setContent(pathData);

						//OR LOAD PROVINCE FROM HERE
						// AL/Elbasan.txt

						if (colors.containsKey(pathOwn)) {
							// Color.valueOf(colors.get(ids));
							svgPath.setFill(Paint.valueOf(colors.get(pathOwn)));
						} else {
							svgPath.setFill(defColor);
							colors.put(pathOwn, defColor.toString()); // !!!!!!!!!!!!!!!!!!!

						}
						//svgPath.setOnMouseClicked(this::onPathClicked);
						svgPath.setStroke(defBorderColor);
						svgPaths.add(svgPath);
					} catch (Exception e){
						//continue;
					}
				}
			}

			//If number of Paths already known, no need
			mapSVG = svgPaths.toArray(new SVGProvince[0]);
			mapGroup = new Group(mapSVG);
			mapGroup.setOnMouseClicked(this::onPathClicked);

			mapGroup.setOnMouseEntered(this::onMouseHover);

			mapGroup.setCursor(Cursor.HAND);
			// better solution?
			//Pane stackPane = new Pane(mapGroup);
			ScrollPane scrollPane = new ZoomableScrollPane(mapGroup);
			//scrollPane.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
			ContextMenu cm = new ContextMenu();
			MenuItem menuItem1 = new MenuItem("Recruit troops");
			MenuItem menuItem2 = new MenuItem("Construct building");
			MenuItem menuItem3 = new MenuItem("");
			cm.getItems().addAll(menuItem1, menuItem2, menuItem3);
			scrollPane.setContextMenu(cm);
			return scrollPane;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void loadColors() {
		colors = new HashMap<>();
		try (BufferedReader br = new BufferedReader(new FileReader(GLogic.RESOURCESPATH+"map/colors.csv"))) {
			String row = br.readLine(); // Skip first row
			while ((row = br.readLine()) != null) {
				if (row.isBlank())
					continue;
				String[] c = row.split("\\s*,\\s*");
				if (c.length >= 2) {
					colors.put(c[0], c[1]);
				}
			}

		} catch (Exception e) {

		}
	}

	private void onPathClicked(MouseEvent event) {
		// Your handling logic here
		Node clickedNode = (Node) event.getTarget();
		if (clickedNode instanceof SVGProvince clickedPath) {
            String pathId = clickedPath.getId();
			String pathOwn = clickedPath.getAccessibleText();
			gs.setSelectedCountry(clickedPath.getOwnerId());
			//TESTING.print(clickedPath.getOwnerId(),clickedPath.getProvId());
			//gs.changeSelectedCountryInfo();
			gs.setSelectedProvince(clickedPath.getProvId());
			//gs.changeSelectedProvinceInfo();

			System.out.println("Path clicked - ID: " + pathId + ", Owner: " + pathOwn);
			if (mapMode == 1) {
				paintMapAllies(); // CAN BECOME MORE EFFICIENT
				// Only change the colors of the things which are
				// in the previous and next Set<String/Integer> allies??
				// insert SVGPath into country/admdiv???
			}
		}
	}

	private void onMouseHoverOld(MouseEvent event) {
		SVGProvince hovering = (SVGProvince) event.getSource();
		if (this.mapMode == 0) {

			Color currentColor = Color.web(hovering.getFill().toString());
			Color lightenedColor = lightenColor(currentColor, 0.25);
			// Color lightenedColor = currentColor.brighter();
			String lightenedColorHex = lightenedColor.toString().replace("0x", "#");
			hovering.setFill(Paint.valueOf(lightenedColorHex));
		}
		gs.changeHoveringOverCountry(hovering.getAccessibleText() + " - " + hovering.getId());
	}

	private void onMouseNotHover(MouseEvent event) {
		SVGProvince hovering = (SVGProvince) event.getSource();
		if (this.mapMode == 0) {
			String ids = hovering.getId();
			hovering.setFill(Paint.valueOf(colors.get(ids)));
		}
	}

	private static Color lightenColor(Color color, double factor) {
		double r = color.getRed();
		double g = color.getGreen();
		double b = color.getBlue();
		if (color.getBrightness() >= 1.0) {
			// Color is already at maximum brightness, adjust saturation instead
			r = Math.min(r + ((1.0 - r) * factor), 1.0);
			g = Math.min(g + ((1.0 - g) * factor), 1.0);
			b = Math.min(b + ((1.0 - b) * factor), 1.0);
			return Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255), color.getOpacity());
		} else {
			// Use the brighter() method to lighten the color
			Color lightenedColor = color.brighter();
			return lightenedColor;
		}

	}

	// change for show allies???

	private void onMouseHover(MouseEvent event) {
		Node hoveredNode = (Node) event.getTarget();

		if (hoveredNode instanceof SVGProvince hoveredPath) {
			//CHANGE
			gs.changeHoveringOverCountry(CountryArray.getIndexISO2(hoveredPath.getOwnerId()) + " - " + hoveredPath.getId());
		}
	}
	public String getPlayerCountry() {
		return playerCountry;
	}

	public void setPlayerCountry(String playerCountry) {
		this.playerCountry = playerCountry;
	}
	public void setPlayerCountry(int playerId) {
		this.playerCountry = CountryArray.getIndexISO2(playerId);
	}

	public void setMapGroupCursorCrosshair() {
		mapGroup.setCursor(Cursor.CROSSHAIR);
	}

	public void setMapGroupCursorHand() {
		mapGroup.setCursor(Cursor.HAND);
	}

	public void setMapGroupCursorNone() {
		mapGroup.setCursor(Cursor.NONE);
	}

	public void switchMapMode(int mode) {
		if (this.mapMode != mode) {
			this.mapMode = mode;
			switch (this.mapMode) {
			case 0:
				paintMapDefault();
				break;
			case 1:
				paintMapCountries();
				break;
			case 2:
				paintMapAllies();
				break;
			default:

				break;
			}
		}
	}

	// how to load?!?
	public void paintMapDefault() {

		for (SVGProvince t : mapSVG) {
			t.setFill(Paint.valueOf(colors.get(t.getAccessibleText())));
		}
	}

	public void paintMapCountries() {
		for (SVGProvince t : mapSVG) {
			t.setFill(Paint.valueOf(colors.get(t.getAccessibleText())));
		}
	}

	public void paintMapAllies() {
		int cName = gs.getSelectedCountry();
		Country c = gs.getGame().getWorldCountries().get(cName);
		// On changed???
		// also show subjects? different color
		if (c != null)
			for (SVGProvince t : mapSVG) {
				if (c.isAllyWith(t.getOwnerId())) {
					t.setFill(Paint.valueOf(defaultAllyColor));
				} else {
					t.setFill(defColor);
				}
			}
		else {
			for (SVGProvince t : mapSVG) {
				t.setFill(defColor);
			}
		}
	}

	public void changeDefColor(String cn, String value) {
		colors.put(cn, value);
		// LOAD ?!?
	}

	public Map<String, String> getColors() {
		return colors;
	}

	public void setColors(Map<String, String> colors) {
		this.colors = colors;
	}

	public SVGProvince[] getMapSVG() {
		return mapSVG;
	}

}
