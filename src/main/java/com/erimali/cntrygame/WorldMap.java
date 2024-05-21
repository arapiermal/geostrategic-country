package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilUnitData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.text.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class WorldMap {
    private static double mapWidth = 12200;
    private static double mapHeight = 6149.8;
    private ZoomableScrollPane scrollPane;
    private Paint[] colors;
    private Color backgroundColor = Color.valueOf("#ADD8E6");
    private Color colDefTransparent = new Color(0, 0, 0, 0);
    private String defColorString = "#ececec";
    private Color defColor = Color.valueOf(defColorString);
    private Color defColorCountry = new Color(0, 0, 0, 0);
    private String defBorderColorString = "#000000";
    private Paint defBorderColor = Paint.valueOf(defBorderColorString);
    private Paint defAtWarWithColor = Color.RED;
    private Paint defAllyColor = Paint.valueOf("blue");
    private Paint defNeutralColor = Paint.valueOf("lightgreen");
    private Paint defSelectedColor = Paint.valueOf("gray");
    private Paint defSubjectColor = Paint.valueOf("green");
    private Paint defOwnerColor = Paint.valueOf("yellow");
    private Group mapGroup;

    // CURSOR
    private SVGProvince[] mapSVG;// all adm divisions

    private WaterBody[] waterBodies; // put elsewhere so accessible from elsewhere
//set/remove fill to countries when that mode

    private List<Region> milUnits;

    private final GameStage gs;
    private ShortestPathFinder roadFinder;
    private MilUnitRegion debugMilUnitRegion;
    private int mapMode;

    private SVGPath[] milSVG;
    private static final String[] MAP_MODE_NAMES = new String[]{"Default", "Allies", "Unions", "Neighbours", "Continents", "Water"};

    private Set<Integer> waterProvinces;

    public WorldMap(GameStage gs) {
        waterProvinces = new HashSet<>();
        this.waterBodies = WaterBody.loadWaterBodies(waterProvinces);
        loadColors();
        loadMilSVGData();
        this.gs = gs;
        this.scrollPane = start();
    }

    public static double getMapWidth() {
        return mapWidth;
    }

    public static double getMapHeight() {
        return mapHeight;
    }

    public Set<Integer> getWaterProvinces() {
        return waterProvinces;
    }

    public static int getMaxMapModes() {
        return MAP_MODE_NAMES.length;
    }

    public static String getMapModeName(int i) {
        if (i < 0 || i >= MAP_MODE_NAMES.length) {
            return "";
        } else {
            return MAP_MODE_NAMES[i];
        }
    }

    public void loadMilSVGData() {
        URL p = getClass().getResource("img/milsvgdata.txt");
        if (p == null) {
            throw new IllegalArgumentException("img/milsvgdata.txt not found");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(p.getFile()))) {
            milSVG = new SVGPath[8];
            int i = 0;
            String s;
            while ((s = br.readLine()) != null) {
                milSVG[i] = new SVGPath();
                milSVG[i].setContent(s);
                i++;
            }
        } catch (IOException ioException) {
            ErrorLog.logError(ioException);
        }
    }


    public ZoomableScrollPane start() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/map/illustMap.svg"))) {
            // Load SVG file
            List<SVGProvince> svgPaths = new LinkedList<>(); //LINKED LIST MORE EFFICIENT?!? SINCE WILL BE CONVERTED TO ARRAY?
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
                    j += 5;
                    int beginPath = j;
                    while (j < line.length() && line.charAt(j) != '"') {
                        j++;
                    }
                    String pathData = line.substring(beginPath, j);
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
            this.mapSVG = svgPaths.toArray(new SVGProvince[0]);
            this.mapGroup = new Group(mapSVG);
            //SVGPath bg = makeBackground();
            //mapGroup.getChildren().add(bg);
            //mapGroup.getChildren().addAll();
            //If number of Paths already known, no need
            mapGroup.setOnMouseClicked(this::onPathClicked);

            //mapGroup.setOnMouseEntered(this::onMouseHover);


            mapGroup.setCursor(Cursor.HAND);
            ZoomableScrollPane scrollPane = new ZoomableScrollPane(mapGroup);
            loadWaterBodiesCircle();
            roadFinder = new ShortestPathFinder(mapSVG, waterBodies);
            debugMilUnitRegion = makeMilUnitImg(0, 3198, 0);

            ContextMenu cm = new ContextMenu();
            MenuItem[] menuItems = new MenuItem[GOptions.isDebugMode() ? 6 : 2];
            menuItems[0] = new MenuItem("Manage units/divisions");
            menuItems[1] = new MenuItem("");
            if (menuItems.length > 2) {
                menuItems[2] = new MenuItem("Debug-dijk: mainProv");
                menuItems[2].setOnAction(e -> {
                    System.out.println();
                    int provInd = gs.getSelectedProv();
                    System.out.print(provInd + ":");
                    mapSVG[provInd].setFill(Color.BLACK);

                });
                menuItems[3] = new MenuItem("Debug-dijk: neighProvs");
                menuItems[3].setOnAction(e -> {
                    int provInd = gs.getSelectedProv();
                    System.out.print(provInd + ",");
                    mapSVG[provInd].setFill(Color.CRIMSON);

                });
                menuItems[4] = new MenuItem("Debug-move: dest DebugUnit");
                menuItems[4].setOnAction(e -> {
                    int dstInd = gs.getSelectedProv();
                    debugMilUnitRegion.move(dstInd);
                    //debugMilUnitRegion.makeLines(roadFinder.findShortestPath(srcInd, dstInd));

                });
                menuItems[5] = new MenuItem("Debug-move: move DebugUnit");
                menuItems[5].setOnAction(e -> {
                    TESTING.print(debugMilUnitRegion.moveTick());
                });
            }
            cm.getItems().addAll(menuItems);
            scrollPane.setContextMenu(cm);
            scrollPane.setHvalue(scrollPane.getHmax() / 2);
            scrollPane.setVvalue(scrollPane.getVmax() / 2);
            URL css = getClass().getResource("css/worldMap.css");
            if (css != null)
                scrollPane.getStylesheets().add(css.toExternalForm());
            return scrollPane;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public SVGPath makeBackground() {
        // Set background to water color
        SVGPath bg = new SVGPath();
        bg.setContent("M0," + mapHeight + "V0h" + mapWidth + "v" + mapHeight + "H0z");
        bg.setFill(backgroundColor);
        bg.setStroke(Paint.valueOf("black"));
        bg.setStrokeLineCap(StrokeLineCap.ROUND);
        bg.setStrokeLineJoin(StrokeLineJoin.ROUND);
        bg.setStrokeWidth(0.2);
        return bg;
    }

    public void loadColors() {
        colors = new Paint[CountryArray.getMaxIso2Countries()];
        try (BufferedReader br = new BufferedReader(new FileReader(GLogic.RESOURCESPATH + "map/colors.csv"))) {
            String row = br.readLine(); // Skip first row
            while ((row = br.readLine()) != null) {
                if (row.isBlank())
                    continue;
                String[] c = row.trim().split("\\s*,\\s*", 2);
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
        Node clickedNode = (Node) event.getTarget();
        if (clickedNode instanceof SVGProvince clickedPath) {
            //String pathId = clickedPath.getId();
            int oldSel = gs.getSelectedCountry();
            int pathOwn = clickedPath.getOwnerId();
            int provId = clickedPath.getProvId();
            gs.setSelectedProvince(provId);
            gs.setSelectedCountry(pathOwn);
            //System.out.println(clickedPath.getId() + " clicked");
            if (pathOwn != oldSel) {
                if (mapMode == 1)
                    paintMapAllies(oldSel);
                else if (mapMode == 3)
                    paintMapNeighbours();
            }
        }
    }

    public DijkstraCalculable getCalculableByIndex(int i) {
        if (i < Short.MAX_VALUE) {
            return mapSVG[i];
        } else {
            return waterBodies[i - Short.MAX_VALUE];
        }
    }

    public ZoomableScrollPane getScrollPane() {
        return scrollPane;
    }

    //cancel when new...
    public static class MilUnitRegion extends Region {
        //TYPE CHECK
        int provId;
        List<Integer> movingIds;
        ShortestPathFinder roadFinder;

        public MilUnitRegion(ShortestPathFinder roadFinder, SVGPath milSVG, SVGProvince prov) {
            super();
            this.roadFinder = roadFinder;
            setShape(milSVG);
            setSizeAndPos(prov);
            getStyleClass().add("milImg");
            setMouseTransparent(true);
        }

        public void setSizeAndPos(DijkstraCalculable prov) {
            if (prov instanceof SVGProvince temp) {
                provId = temp.getProvId();
                double w = temp.getBoundsInLocal().getWidth() / 3;
                double h = temp.getBoundsInLocal().getHeight() / 3;
                setMinSize(w, h);
                setPrefSize(w, h);
                setMaxSize(w, h);
                setLayoutX(prov.getCenterX() - w / 2);
                setLayoutY(prov.getCenterY() - h / 2);
            } else if (prov instanceof WaterBody temp) {
                provId = temp.getWaterBodyId(); //waterId , set to -1 when not in water...

                setLayoutX(prov.getCenterX());
                setLayoutY(prov.getCenterY());//circle radius
            }
        }

        public void move(int dst) {
            //change based on vehicle type, only if ships (or maybe even marines? but marines swim slowly)
            makeLines(roadFinder.findShortestPath(provId, dst, true));
        }

        public void makeLines(List<Integer> p) {
            getChildren().clear();
            if (!p.isEmpty()) {
                movingIds = p;
            } else {
                return;
            }
            for (int i = 0; i < p.size() - 1; i++) {
                DijkstraCalculable s0 = roadFinder.getCalculableByIndex(p.get(i));
                DijkstraCalculable s1 = roadFinder.getCalculableByIndex(p.get(i + 1));
                double x0 = s0.getCenterX() - getLayoutX();
                double y0 = s0.getCenterY() - getLayoutY();
                double x1 = s1.getCenterX() - getLayoutX();
                double y1 = s1.getCenterY() - getLayoutY();
                Line line = new Line(x0, y0, x1, y1);
                //line.setMouseTransparent(true); // does not work inside region
                if (s0 instanceof WaterBody || s1 instanceof WaterBody) {
                    line.setStroke(Color.DARKBLUE);
                } else {
                    line.setStroke(Color.BLACK);
                }
                getChildren().add(line);

            }
        }

        public void updateLines() {
            for (int i = 0; i < movingIds.size() - 1; i++) {
                DijkstraCalculable s0 = roadFinder.getCalculableByIndex(movingIds.get(i));
                DijkstraCalculable s1 = roadFinder.getCalculableByIndex(movingIds.get(i + 1));
                double x0 = s0.getCenterX() - getLayoutX();
                double y0 = s0.getCenterY() - getLayoutY();
                double x1 = s1.getCenterX() - getLayoutX();
                double y1 = s1.getCenterY() - getLayoutY();
                Line line = (Line) getChildren().get(i);
                line.setStartX(x0);
                line.setStartY(y0);
                line.setEndX(x1);
                line.setEndY(y1);
                if (s0 instanceof WaterBody || s1 instanceof WaterBody) {
                    line.setStroke(Color.DARKBLUE);
                } else {
                    line.setStroke(Color.BLACK);
                }
            }
        }

        public boolean isMoving() {
            return movingIds != null && !movingIds.isEmpty();
        }

        public int getProvId() {
            return provId;
        }

        public boolean moveTick() {
            if (isMoving()) {
                getChildren().removeFirst();
                movingIds.removeFirst();
                DijkstraCalculable sNext = roadFinder.getCalculableByIndex(movingIds.getFirst());
                setSizeAndPos(sNext);
                updateLines();
                if (movingIds.size() <= 1) {
                    movingIds = null;

                    return true;
                }
                return false;
            } else {
                return true;
            }
        }
    }

    public MilUnitRegion makeMilUnitImg(int type, int provId, int friendly) {
        SVGProvince prov = mapSVG[provId];

        MilUnitRegion milImg = new MilUnitRegion(roadFinder, milSVG[type], prov);
        //milImg.setStyle("-fx-background-color: green;");
        milImg.getStyleClass().add(MilUnitData.getUnitTypeName(type));
        milImg.getStyleClass().add(friendly == 0 ? "player" : friendly > 0 ? "allies" : "enemy");

        mapGroup.getChildren().add(milImg);
        return milImg;
    }

    private static Color lightenColor(Color color, double factor) {
        double r = color.getRed();
        double g = color.getGreen();
        double b = color.getBlue();
        if (color.getBrightness() >= 1.0) {
            // Color is at maximum brightness -> change saturation
            r = Math.min(r + ((1.0 - r) * factor), 1.0);
            g = Math.min(g + ((1.0 - g) * factor), 1.0);
            b = Math.min(b + ((1.0 - b) * factor), 1.0);
            return Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255), color.getOpacity());
        } else {
            Color lightenedColor = color.brighter();
            return lightenedColor;
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
            Paint ownerColor = getColor(t.getOwnerId());
            if (t.isOccupied()) {
                t.setFillExtra(ownerColor, getColor(t.getOccupierId()));
            } else {
                t.setFill(ownerColor);
            }
        }
    }

    public void paintMapNeighbours() {
        int cId = gs.getSelectedCountry();
        Country c = gs.getGame().getCountry(cId);
        if (c != null) {
            Set<Integer> neighbours = c.getNeighbours();
            for (SVGProvince t : mapSVG) {
                int ownerId = t.getOwnerId();
                if (neighbours.contains(ownerId)) {
                    t.setFill(defNeutralColor);
                } else if (cId == ownerId) {
                    t.setFill(defSelectedColor);
                } else {
                    t.setFill(defColor);
                }
            }
        }
    }

    /*
    //problems because not all neighbours loaded
        public void paintMapNeighbours(int oldSel) {
            CountryArray cArr = gs.getGame().getWorld().getCountries();
            int cId = gs.getSelectedCountry();
            Country o = cArr.get(oldSel);
            setColorOnAdmDivs(o, defColor);
            for (int i : o.getNeighbours()) {
                setColorOnAdmDivs(cArr.get(i), defColor);
            }
            Country c = cArr.get(cId);
            if (c != null) {
                setColorOnAdmDivs(c, defSelectedColor);
                for (int i : c.getNeighbours()) {
                    setColorOnAdmDivs(cArr.get(i), defNeutralColor);
                }
            }
        }
    */
    public void paintMapUnions(Union union) {
        if (union != null) {
            Paint unionColor = union.getColor() == null ? defAllyColor : union.getColor();
            Set<Integer> set = union.getUnionCountries();
            for (SVGProvince t : mapSVG) {
                if (set.contains(t.getOwnerId())) {
                    t.setFill(unionColor);
                } else {
                    t.setFill(defColor);
                }
            }
        }
    }

    //7 loops not efficient...
    //CFormable.FirstAdmDivs if it stored everything from worldmap...
    public void paintMapContinents() {
        for (Continent cont : Continent.values()) {
            Set<Short> c = cont.getCountries();
            Paint color = cont.getColor();
            for (SVGProvince t : mapSVG) {
                if (c.contains((short) t.getOwnerId())) {
                    t.setFill(color);
                }
            }
        }
    }

    public void paintMapUnions() {
        Union union = gs.getSelectedUnionFromWorld();
        paintMapUnions(union);
    }

    //prevSelected -> maybe in int[mapmodeslength], set -1 when changing mapmode
    public void paintMapAllies() {
        int cId = gs.getSelectedCountry();
        Country c = gs.getGame().getCountry(cId);
        if (c != null)
            if (c.isNotSubject()) {
                for (SVGProvince t : mapSVG) {
                    int ownerId = t.getOwnerId();
                    if (c.isAllyWith(ownerId)) {
                        t.setFill(defAllyColor);
                    } else if (c.hasSubject(ownerId)) {
                        t.setFill(defSubjectColor);
                    } else if (c.isAtWarWith(ownerId)) {
                        t.setFill(defAtWarWithColor);
                    } else if (cId == ownerId) {
                        t.setFill(defSelectedColor);
                    } else {
                        t.setFill(defColor);
                    }
                }
            } else {
                int mainId = c.getSubjectOf().getMainId();
                for (SVGProvince t : mapSVG) {
                    int ownerId = t.getOwnerId();
                    if (c.isAllyWith(ownerId)) {
                        t.setFill(defAllyColor);
                    } else if (mainId == ownerId) {
                        t.setFill(defOwnerColor);
                    } else if (c.isAtWarWith(ownerId)) {
                        t.setFill(defAtWarWithColor);
                    } else if (cId == ownerId) {
                        t.setFill(defSelectedColor);
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

    public void setColorOnAdmDivs(Country c, Paint color) {
        if (c == null)
            return;
        setColorOnAdmDivs(c.getAdmDivs(), color);
    }

    public void setColorOnAdmDivs(List<AdmDiv> admDivs, Paint color) {
        for (AdmDiv a : admDivs) {
            SVGProvince svg = a.getSvgProvince();
            if (svg != null)
                svg.setFill(color);
            //mapSVG[a.getProvId()].setFill(color);
        }
    }

    public void paintMapAllies(int oldSel) {
        CountryArray cArr = gs.getGame().getWorld().getCountries();
        int cId = gs.getSelectedCountry();
        Country o = cArr.get(oldSel);
        Country c = cArr.get(cId);
        //if was subject (?)
        if (o != null) {
            setColorOnAdmDivs(o, defColor);
            for (short i : o.getDiplomacy().getAllies()) {
                Country oldAlly = cArr.get(i);
                setColorOnAdmDivs(oldAlly, defColor);
            }
            for (int i : o.getSubjects().keySet()) {
                Country oldSubject = cArr.get(i);
                setColorOnAdmDivs(oldSubject, defColor);
            }
            for (int i : o.getMilitary().getAtWarWith()) {
                Country oldAtWar = cArr.get(i);
                setColorOnAdmDivs(oldAtWar, defColor);
            }
            if (!o.isNotSubject()) {
                setColorOnAdmDivs(o.getSubjectOf().getMain(), defColor);
            }
        }
        if (c != null) {
            setColorOnAdmDivs(c, defSelectedColor);
            for (short i : c.getDiplomacy().getAllies()) {
                Country ally = cArr.get(i);
                setColorOnAdmDivs(ally, defAllyColor);
            }
            for (int i : c.getMilitary().getAtWarWith()) {
                Country oldAtWar = cArr.get(i);
                setColorOnAdmDivs(oldAtWar, defAtWarWithColor);
            }
            if (c.isNotSubject()) {
                for (int i : c.getSubjects().keySet()) {
                    Country subject = cArr.get(i);
                    setColorOnAdmDivs(subject, defSubjectColor);
                }
            } else {
                setColorOnAdmDivs(c.getSubjectOf().getMain(), defOwnerColor);
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
        switch (mapMode) {
            case 0:
                paintMapDefault();
                break;
            case 1:
                paintMapAllies();
                break;
            case 2:
                paintMapUnions();
                break;
            case 3:
                paintMapNeighbours();
                break;
            case 4:
                paintMapContinents();
                break;
            case 5:
                paintMapWaters();
                break;
            default:
                break;
        }
    }

    private void paintMapWaters() {
        for (SVGProvince s : mapSVG) {
            if (waterProvinces.contains(s.getProvId())) {
                s.setFill(Color.DODGERBLUE);
            } else {
                s.setFill(defColor);
            }
        }
        for (WaterBody w : waterBodies) {
            //show text on top of circle (?)

        }
    }

    public Line[] makeLines(int... p) {
        Line[] res = new Line[p.length - 1];
        for (int i = 0; i < res.length; i++) {
            SVGProvince s0 = mapSVG[p[i]];
            SVGProvince s1 = mapSVG[p[i + 1]];
            res[i] = new Line(s0.getCenterX(), s0.getCenterY(), s1.getCenterX(), s1.getCenterY());
        }
        return res;
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

    public void removeLine(Line l) {
        mapGroup.getChildren().remove(l);
    }


    public void printFileAllProvData() {

    }

    public double getWidth() {
        return mapWidth;
    }

    public double getHeight() {
        return mapHeight;
    }


    public static void main(String... args) {
        SVGPath a = loadSVGPath("img/zoom_in.svg");
        TESTING.print(a.getContent());
    }

    public static SVGPath loadSVGPath(String path) {
        InputStream inputStream = WorldMap.class.getResourceAsStream(path);
        if (inputStream != null)
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    int check = line.indexOf('<');
                    if (check >= 0 && line.length() > 10 && line.substring(check + 1, check + 5).equalsIgnoreCase("path")) {
                        int d0 = line.indexOf("d=\"");
                        if (d0 > 0) {
                            d0 += 3;
                            stringBuilderTillStringEnd(d0, line, content, br);
                        }
                        content.append(' ');
                    }

                }
                SVGPath svgPath = new SVGPath();
                svgPath.setContent(content.toString());
                return svgPath;
            } catch (IOException ioe) {
                return null;
            }
        return null;
    }

    public static void stringBuilderTillStringEnd(int i, String line, StringBuilder sb) {
        while (i < line.length()) {
            char c = line.charAt(i);
            if (c == '\"')
                return;
            sb.append(c);
            i++;
        }
    }

    public static void stringBuilderTillStringEnd(int i, String line, StringBuilder sb, BufferedReader br) throws IOException {
        do {
            while (i < line.length()) {
                char c = line.charAt(i);
                if (c == '\"')
                    return;
                sb.append(c);
                i++;
            }
            i = 0;
        } while ((line = br.readLine()) != null);
    }

    public int getMapMode() {
        return mapMode;
    }

    public void refreshMapIf(int i) {
        if (mapMode == i)
            refreshMap();
    }

    public void loadWaterBodiesCircle() {
        if (waterBodies != null) {
            for (WaterBody w : waterBodies) {
                w.makePointBetweenProvinces(mapSVG);
                if (w.getCenterX() > 0 && w.getCenterY() > 0) {
                    Tooltip tooltip = new Tooltip(w.toString());
                    Circle circle = new Circle(w.getCenterX(), w.getCenterY(), mapHeight / 800, w.getColor());
                    Tooltip.install(circle, tooltip);
                    mapGroup.getChildren().add(circle);
                }
            }
        }
    }

    //slows down (?)
    private final Label[] labelsCountryNames = new Label[CountryArray.getMaxIso2Countries()];
    private boolean countryNameLabelVisibility = true;

    public void toggleLabelsCountryNamesVisibility() {
        countryNameLabelVisibility = !countryNameLabelVisibility;
        for (Label l : labelsCountryNames) {
            if (l != null) {
                l.setVisible(countryNameLabelVisibility);
            }
        }
    }

    public void removeLabelCountryName(int cId) {
        mapGroup.getChildren().remove(labelsCountryNames[cId]);
        labelsCountryNames[cId] = null;
    }

    public void makeTextCountriesNames(CountryArray cArr) {

        for (Country c : cArr) {
            makeUpdateTextCountryName(c);
        }
    }

    public Point2D calculateCountryCenter(Country c) {
        int n = 0;
        double sumX = 0;
        double sumY = 0;
        for (AdmDiv a : c.getAdmDivs()) {
            SVGProvince svg = a.getSvgProvince();
            if (svg != null) {
                sumX += svg.getCenterX();
                sumY += svg.getCenterY();
                n++;
            }
        }
        double cX = sumX / n;
        double cY = sumY / n;
        return new Point2D(cX, cY);
    }

    public void makeUpdateTextCountryName(Country c) {
        if (c == null)
            return;
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (AdmDiv a : c.getAdmDivs()) {
            SVGPath province = a.getSvgProvince();
            if (province != null) {
                Bounds bounds = province.getBoundsInLocal();
                minX = Math.min(minX, bounds.getMinX());
                minY = Math.min(minY, bounds.getMinY());
                maxX = Math.max(maxX, bounds.getMaxX());
                maxY = Math.max(maxY, bounds.getMaxY());
            }
        }
        if (minX == Double.MAX_VALUE || minY == Double.MAX_VALUE || maxX == Double.MIN_VALUE || maxY == Double.MIN_VALUE) {
            return;
        }
        int cId = c.getCountryId();
        if (labelsCountryNames[cId] == null) {
            labelsCountryNames[cId] = new Label(c.getName());
            mapGroup.getChildren().add(labelsCountryNames[cId]);
            labelsCountryNames[cId].setMouseTransparent(true);
            labelsCountryNames[cId].setTextAlignment(TextAlignment.CENTER);
            labelsCountryNames[cId].setAlignment(Pos.CENTER);
            labelsCountryNames[cId].setWrapText(true);

        } else {
            labelsCountryNames[cId].setText(c.getName());
        }
        double centerX = (minX + maxX) / 2;
        double centerY = (minY + maxY) / 2;

        double minWidth = (maxX - minX);
        double minHeight = (maxY - minY);
        double fontSize = Math.max(10, Math.min(minWidth / c.getName().length(), minHeight) / 1.5);
        labelsCountryNames[cId].setFont(Font.font("System", FontWeight.BOLD, fontSize));
        labelsCountryNames[cId].setTextFill(Color.WHITE);

        labelsCountryNames[cId].setMinWidth(minWidth);
        labelsCountryNames[cId].setMinHeight(minHeight);
        labelsCountryNames[cId].setLayoutX(centerX - minWidth / 2);
        labelsCountryNames[cId].setLayoutY(centerY - minHeight / 2);

    }

    public static double calculateAngle(double x0, double y0, double x1, double y1) {
        double dX = x1 - x0;
        double dY = y1 - y0;
        double angleRad = Math.atan2(dY, dX);
        return Math.toDegrees(angleRad);
    }
}
