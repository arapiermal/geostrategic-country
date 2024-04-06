package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilUnitData;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.SVGPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WorldMap {
    protected static double mapWidth = 12200;
    protected static double mapHeight = 6149.8;
    // can make Color and simply take care when adding/changing/removing
    //private Map<String, Paint> colors;// save as OCEAN,#ADD8E6?
    private Paint[] colors;
    private Color backgroundColor = Color.valueOf("#ADD8E6");
    private Color colDefTransparent = new Color(0, 0, 0, 0);
    private int playerCountry;
    private String defColorString = "#ececec";
    private Color defColor = Color.valueOf(defColorString);
    private Color defColorCountry = new Color(0, 0, 0, 0);
    private String defBorderColorString = "#000000";
    private Paint defBorderColor = Paint.valueOf(defBorderColorString);
    //Irrelevant based on new SVG
    private String defSubBorderColorString = "#FFFFFF";
    private Paint defSubBorderColor = Paint.valueOf(defSubBorderColorString);
    // default fill of country svg -> alpha 0

    private Paint defaultAllyColor = Paint.valueOf("blue");
    private Paint defaultSubjectColor = Paint.valueOf("gray");
    private Paint defaultOwnerColor = Paint.valueOf("yellow");
    private Group mapGroup;
    //private Group countryGroup;

    // CURSOR

    private SVGProvince[] mapSVG;// all adm divisions
//set/remove fill to countries when that mode

    private List<Line> lines;
    private List<Region> milUnits;

    private final GameStage gs;

    private int mapMode;

    private int lastClickedProvince;

    private SVGPath[] milSVG;

    public void loadMilSVGData() {
        URL p =  getClass().getResource("img/milsvgdata.txt");
        if(p == null){
            throw new IllegalArgumentException("img/milsvgdata.txt not found");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(p.getFile()))) {
            milSVG = new SVGPath[8];
            int i = 0;
            String s;
            while((s = br.readLine()) != null){
                milSVG[i] = new SVGPath();
                milSVG[i].setContent(s);
                i++;
            }
        } catch (IOException ioException) {
            ErrorLog.logError(ioException);
        }
    }

    public WorldMap(GameStage gs) {
        loadColors();
        loadMilSVGData();
        this.gs = gs;
    }

    public ScrollPane start() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/map/mcBig.svg"))) {
            // Load SVG file
            List<SVGProvince> svgPaths = new LinkedList<>(); //LINKED LIST MORE EFFICIENT?!? SINCE WILL BE CONVERTED TO ARRAY?
            //List<SVGPath> countryPaths = new ArrayList<>();
            String line;
            int currProvId = 0;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("<path")) {
                    int j = 10;
                    while (j < line.length() && line.charAt(j) != '"') {
                        j++;
                    }
                    String pathId = line.substring(10, j - 3);
                    String pathOwn = line.substring(j - 2, j);
                    int pathOwnId = CountryArray.getIndex(pathOwn);
                    //TESTING.print(pathId,pathOwn);
                    j += 5;
                    int beginPath = j;
                    while (j < line.length() && line.charAt(j) != '"') {
                        j++;
                    }
                    String pathData = line.substring(beginPath, j);
                    //TESTING.print(pathData);
                    try {

                        SVGProvince svgPath = new SVGProvince(CountryArray.getIndex(pathOwn), currProvId++);
                        svgPath.setId(pathId);
                        svgPath.setContent(pathData);
                        svgPath.updateXY();
                        //OR LOAD PROVINCE FROM HERE
                        // AL/Elbasan.txt
                        if (containsColor(pathOwnId)) {
                            svgPath.setFill(getColor(pathOwnId));
                        } else {
                            svgPath.setFill(defColor);
                            setColor(pathOwnId, defColor);

                        }
                        //svgPath.setOnMouseClicked(this::onPathClicked);
                        svgPath.setStroke(defBorderColor);
                        svgPaths.add(svgPath);
                    } catch (Exception e) {
                        //continue;
                    }
                }
            }

            //If number of Paths already known, no need
            this.mapSVG = svgPaths.toArray(new SVGProvince[0]);
            this.mapGroup = new Group(mapSVG);
            mapGroup.setOnMouseClicked(this::onPathClicked);

            //mapGroup.setOnMouseEntered(this::onMouseHover);

            mapGroup.setCursor(Cursor.HAND);

            // better solution?
            //Pane stackPane = new Pane(mapGroup);
            ScrollPane scrollPane = new ZoomableScrollPane(mapGroup);
            this.lines = new ArrayList<>();
            //int l = drawLine(3198, 3031);
            //int[] l = drawLines(3031, 3030, 2993, 2994, 2991, 2992, 3198);
            //makeMilSVG(0, 3198, 0);
            //scrollPane.removeLine(l);
            //scrollPane.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
            ContextMenu cm = new ContextMenu();
            MenuItem menuItem1 = new MenuItem("Recruit troops");
            MenuItem menuItem2 = new MenuItem("Construct building");
            MenuItem menuItem3 = new MenuItem("");
            cm.getItems().addAll(menuItem1, menuItem2, menuItem3);
            scrollPane.setContextMenu(cm);
            scrollPane.setHvalue(scrollPane.getHmax() / 2);
            scrollPane.setVvalue(scrollPane.getVmax() / 2);
            URL css = getClass().getResource("css/worldMap.css");
            if(css != null)
                scrollPane.getStylesheets().add(css.toExternalForm());
            return scrollPane;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadColors() {
        colors = new Paint[CountryArray.maxISO2Countries];
        try (BufferedReader br = new BufferedReader(new FileReader(GLogic.RESOURCESPATH + "map/colors.csv"))) {
            String row = br.readLine(); // Skip first row
            while ((row = br.readLine()) != null) {
                if (row.isBlank())
                    continue;
                String[] c = row.split("\\s*,\\s*", 2);
                if (c.length >= 2) {
                    if (c[0].length() == 2) {
                        int id = CountryArray.getIndex(c[0]);
                        colors[id] = Paint.valueOf(c[1]);
                    }
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
            int pathOwn = clickedPath.getOwnerId();
            gs.setSelectedCountry(pathOwn);
            //TESTING.print(clickedPath.getOwnerId(),clickedPath.getProvId());
            //gs.changeSelectedCountryInfo();
            gs.setSelectedProvince(clickedPath.getProvId());
            //gs.changeSelectedProvinceInfclickedNode.getProo()  +; c
            System.out.println(clickedPath.getProvId() + " clicked - ID: " + pathId + ", Owner: " + pathOwn);
            if (mapMode == 1) {
                //Put CountryArray countries here...
                //To access allies fast
                //Would work when clicking after the first time going through Set (because all others have to be set default color)
                paintMapAllies(); // CAN BECOME MORE EFFICIENT
                // Only change the colors of the things which are
                // in the previous and next Set<String/Integer> allies??
                // insert SVGPath into country/admdiv???
            }
        }
    }

    public Region makeMilSVG(int type, int provId, int friendly) {
        SVGProvince prov = mapSVG[provId];
        double w = prov.getBoundsInLocal().getWidth() / 3;
        double h = prov.getBoundsInLocal().getHeight() / 3;
        Region milImg = new Region();
        milImg.setShape(milSVG[type]);
        milImg.setMinSize(w, h);
        milImg.setPrefSize(w, h);
        milImg.setMaxSize(w, h);
        //milImg.setStyle("-fx-background-color: green;");
        milImg.getStyleClass().add("milImg");
        milImg.getStyleClass().add(MilUnitData.getUnitTypeName(type));
        milImg.getStyleClass().add(friendly == 0 ? "player" : friendly > 0 ? "allies" : "enemy");
        milImg.setLayoutX(prov.getProvX() - w / 2);
        milImg.setLayoutY(prov.getProvY() - h / 2);

        mapGroup.getChildren().add(milImg);
        return milImg;
    }

    private void onMouseHoverOld(MouseEvent event) {
        SVGProvince hovering = (SVGProvince) event.getSource();
        if (this.mapMode == 0) {

            Color currentColor = Color.web(hovering.getFill().toString());
            Color lightenedColor = lightenColor(currentColor, 0.25);
            // Color lightenedColor = currentColor.brighter();
            //String lightenedColorHex = lightenedColor.toString().replace("0x", "#");
            hovering.setFill(lightenedColor);
        }
        gs.changeHoveringOverCountry(hovering.getAccessibleText() + " - " + hovering.getId());
    }

    private void onMouseNotHover(MouseEvent event) {
        SVGProvince hovering = (SVGProvince) event.getSource();
        if (this.mapMode == 0) {
            int oid = hovering.getOwnerId();
            hovering.setFill(getColor(oid));
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

    public void setMapGroupCursor(Cursor c) {
        mapGroup.setCursor(c);
    }

    public void switchMapMode(int mode) {
        if (mapMode != mode) {
            mapMode = mode;
            refreshMap();
        }
    }

    public void paintMapDefault() {

        for (SVGProvince t : mapSVG) {
            t.setFill(getColor(t.getOwnerId()));
        }
    }

    public void paintMapAllies() {
        int cId = gs.getSelectedCountry();
        Country c = gs.getGame().getCountry(cId);
        // On changed???
        // also show subjects? different color

        if (c != null)
            if (c.isNotSubject()) {
                for (SVGProvince t : mapSVG) {
                    if (c.isAllyWith(t.getOwnerId())) {
                        t.setFill(defaultAllyColor);
                    } else if (c.hasSubject(t.getOwnerId())) {
                        t.setFill(defaultSubjectColor);

                    } else {
                        t.setFill(defColor);
                    }
                }
            } else {
                int mainId = c.getSubjectOf().getMainId();
                for (SVGProvince t : mapSVG) {
                    if (c.isAllyWith(t.getOwnerId())) {
                        t.setFill(defaultAllyColor);
                    } else if (mainId == t.getOwnerId()) {
                        t.setFill(defaultOwnerColor);

                    } else {
                        t.setFill(defColor);
                    }
                }
            }
        else {
            for (SVGProvince t : mapSVG) {
                t.setFill(defColor);
            }
        }
    }

    public void changeColor(String c, String value) {
        int id = CountryArray.getIndex(c);
        Paint paint = Paint.valueOf(value);
        setColor(id, paint);
    }

    public Paint getColor(int id) {
        if (id >= 0 && id < colors.length)
            return colors[id] != null ? colors[id] : defColor;
        return defColor;
    }

    public void setColor(int id, Paint color) {
        if (id <= 0 || id > colors.length)
            return;
        colors[id] = color;
    }

    public Paint[] getColors() {
        return colors;
    }

    public void setColors(Paint[] colors) {
        this.colors = colors;
    }

    public SVGProvince[] getMapSVG() {
        return mapSVG;
    }

    public boolean containsColor(String c) {
        return colors[CountryArray.getIndex(c)] != null;
    }

    public boolean containsColor(int id) {
        return id >= 0 && id < colors.length && colors[id] != null;
    }

    public void refreshMap() {
        switch (this.mapMode) {
            case 0:
                paintMapDefault();
                break;
            case 1:

                break;
            case 2:
                paintMapAllies();
                break;
            default:

                break;
        }
    }
    //0.0 0.1 0.2 0.3, 1.0 1.1 1.2 1.3  ...
    //0                4                ...
    //how to keep track of removable lines
    public int[] drawLines(int... p) {
        int[] res = new int[p.length - 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = drawLine(p[i], p[i + 1]);
        }
        return res;
    }

    public int[] drawLinesArrow(int... p) {
        int[] res = new int[p.length + 1];
        for (int i = 0; i < p.length - 1; i++) {
            res[i] = drawLine(p[i], p[i + 1]);
        }
        //res[p.length] = ;
        //res[p.length + 1] = ;
        return res;
    }

    public int drawLine(int s0, int s1) {
        if (s0 < 0 || s0 > mapSVG.length || s1 < 0 || s1 > mapSVG.length)
            return -1;
        lines.add(drawLine(mapSVG[s0], mapSVG[s1]));
        return lines.size() - 1;
    }

    public Line drawLine(SVGProvince s0, SVGProvince s1) {
        Line line = new Line(s0.getProvX(), s0.getProvY(), s1.getProvX(), s1.getProvY());
        mapGroup.getChildren().add(line);
        return line;
    }

    public Line drawLine(SVGPath s0, SVGPath s1) {
        double minX0 = s0.getBoundsInLocal().getMinX();
        double minY0 = s0.getBoundsInLocal().getMinY();
        double maxX0 = s0.getBoundsInLocal().getMaxX();
        double maxY0 = s0.getBoundsInLocal().getMaxY();
        double x0 = (minX0 + maxX0) / 2;
        double y0 = (minY0 + maxY0) / 2;
        double minX1 = s1.getBoundsInLocal().getMinX();
        double minY1 = s1.getBoundsInLocal().getMinY();
        double maxX1 = s1.getBoundsInLocal().getMaxX();
        double maxY1 = s1.getBoundsInLocal().getMaxY();
        double x1 = (minX1 + maxX1) / 2;
        double y1 = (minY1 + maxY1) / 2;
        TESTING.print(x0 + " " + y0, x1 + " " + y1);

        Line line = new Line(x0, y0, x1, y1);
        mapGroup.getChildren().add(line);
        return line;
    }
    //double x0 = s0.getLayoutX() + s0.getBoundsInLocal().getWidth() / 2;
    //double y0 = s0.getLayoutY() + s0.getBoundsInLocal().getHeight() / 2;
    //double x1 = s1.getLayoutX() + s1.getBoundsInLocal().getWidth() / 2;
    //double y1 = s1.getLayoutY() + s1.getBoundsInLocal().getHeight() / 2;

    public Line drawLine(double x0, double y0, double x1, double y1) {
        Line line = new Line(x0, y0, x1, y1);
        mapGroup.getChildren().add(line);
        return line;
    }

    //problematic when updating indexes...
    public void removeLine(int i) {
        removeLine(lines.remove(i));
    }

    public void removeLine(Line l) {
        mapGroup.getChildren().remove(l);
    }


}
