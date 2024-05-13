package com.erimali.cntrygame;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//Mixed between Logic and GUI
public class WaterBody implements DijkstraCalculable {


    public enum WaterBodyType {
        OCEAN("Ocean", Color.DARKBLUE),
        SEA("Sea", Color.CORNFLOWERBLUE),
        ;
        //if not final could be changed for translation (!)
        private final String desc;
        private Paint color;
        WaterBodyType(String desc, Paint color) {
            this.desc = desc;
            this.color = color;
        }

        @Override
        public String toString() {
            return desc;
        }

        public Paint getColor() {
            return color;
        }

        public void setColor(Paint color) {
            this.color = color;
        }
    }

    private int waterBodyId;
    private WaterBodyType type;
    private String name;
    private double area;
    private double centerX;//or represented by Point(x,y) in world map;
    private double centerY;
    private int[] neighbours;

    private int[] betweenProvs;
    //private List<WaterBody> neighbours;//or Set<String> neighbourWaterBodies, short[] neighbour provinces...


    public WaterBody(WaterBodyType type, String name, double area) {
        this.type = type;
        this.name = name;
        this.area = area;
    }

    public WaterBody(int waterBodyId, WaterBodyType type, String name, double area) {
        this.waterBodyId = waterBodyId;
        this.type = type;
        this.name = name;
        this.area = area;
    }

    public void setPoint(double centerX, double centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public void makePointBetweenProvinces(SVGProvince[] provinces, int... provIds) {
        if (provIds.length == 0)
            if (betweenProvs != null)
                provIds = betweenProvs;
            else
                provIds = neighbours;
        double totalX = 0.0;
        double totalY = 0.0;
        int n = 0;
        for (int i : provIds) {
            if (i < provinces.length) {
                SVGProvince temp = provinces[i];
                totalX += temp.getCenterX();
                totalY += temp.getCenterY();
                n++;
            }
        }
        if (n > 0) {
            this.centerX = totalX / n;
            this.centerY = totalY / n;
        }
    }

    public boolean equals(WaterBody o) {
        return type.equals(o.type) && name.equalsIgnoreCase(o.name);
    }

    @Override
    public String toString() {
        return name + " " + type.toString();
    }

    @Override
    public double getCenterX() {
        return centerX;
    }

    @Override
    public double getCenterY() {
        return centerY;
    }

    @Override
    public double getDistance(DijkstraCalculable dijkstraCalculable) {
        double distX = centerX - dijkstraCalculable.getCenterX();
        double distY = centerY - dijkstraCalculable.getCenterY();
        return Math.sqrt(distX * distX + distY * distY);
    }

    public WaterBodyType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getArea() {
        return area;
    }

    public int getWaterBodyId() {
        return waterBodyId;
    }

    public void setWaterBodyId(int waterBodyId) {
        this.waterBodyId = waterBodyId;
    }

    public int[] getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(int[] neighbours) {
        this.neighbours = neighbours;
    }

    public int[] getBetweenProvs() {
        return betweenProvs;
    }

    public void setBetweenProvs(int[] betweenProvs) {
        this.betweenProvs = betweenProvs;
    }

    public void fixWaterNeighbours(WaterBody[] waterBodies, Set<String> waterNeighbours) {
        int i = 0;
        int n;
        if (neighbours == null) {
            n = waterNeighbours.size();
            neighbours = new int[n];
            for (WaterBody w : waterBodies) {
                if (waterNeighbours.contains(w.name)) {
                    neighbours[i++] = w.waterBodyId;
                    if (i >= neighbours.length)
                        break;
                }
            }
        } else {
            int[] oldNeighbours = neighbours;
            n = neighbours.length + waterNeighbours.size();
            neighbours = new int[n];
            for (WaterBody w : waterBodies) {
                if (waterNeighbours.contains(w.name)) {
                    neighbours[i++] = w.waterBodyId;
                    if (i >= waterNeighbours.size())
                        break;
                }
            }
            int j = 0;
            while (i < neighbours.length && j < oldNeighbours.length) {
                neighbours[i++] = oldNeighbours[j++];
            }

        }
        if (i < n) {
            neighbours = Arrays.copyOf(neighbours, i);
        }
    }

    public static WaterBody[] loadWaterBodies() {
        List<List<WaterBody>> list = new LinkedList<>();
        List<Set<String>> waterNeighbours = new LinkedList<>();
        for (WaterBody.WaterBodyType type : WaterBody.WaterBodyType.values()) {
            list.add(WaterBody.loadListWaterBody(type, waterNeighbours));
        }
        int n = 0;
        for (List<WaterBody> l : list) {
            n += l.size();
        }
        WaterBody[] waterBodies = new WaterBody[n];
        int i = 0;
        for (List<WaterBody> l : list) {
            for (WaterBody w : l) {
                w.setWaterBodyId(Short.MAX_VALUE + i);
                waterBodies[i++] = w;
            }
        }
        Iterator<Set<String>> wnIterator = waterNeighbours.iterator();
        for (WaterBody w : waterBodies) {
            w.fixWaterNeighbours(waterBodies, wnIterator.next());
        }
        return waterBodies;
    }

    public static List<WaterBody> loadListWaterBody(WaterBody.WaterBodyType type, List<Set<String>> waterNeighboursList) {
        List<WaterBody> list = new LinkedList<>();
        String pathName = GLogic.RESOURCESPATH + "countries/water/" + type.name().toLowerCase() + "s.data";
        try (BufferedReader br = new BufferedReader(new FileReader(pathName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":")) {
                    String[] k = line.trim().split("\\s*:\\s*");
                    String[] constructor = k[0].split("\\s*,\\s*");
                    WaterBody wb = new WaterBody(type, constructor[0], GUtils.parseI(constructor, 1));

                    String[] waterNeighbours = k.length > 1 ? k[1].trim().split("\\s*,\\s*") : new String[]{};
                    Set<String> waterSet = new HashSet<>(Arrays.asList(waterNeighbours));
                    waterNeighboursList.add(waterSet);
                    int[] neighbours = k.length > 2 ? GUtils.parseIntArr(k[2].trim().split("\\s*,\\s*")) : null;
                    wb.setNeighbours(neighbours);
                    int[] betweenProvs = k.length > 3 ? GUtils.parseIntArr(k[3].trim().split("\\s*,\\s*")) : null;
                    wb.setBetweenProvs(betweenProvs);
                    list.add(wb);
                }
            }
        } catch (IOException ioe) {

        }
        return list;
    }

    public Paint getColor() {
        return type.getColor();
    }

}
