package com.erimali.cntrygame;

import javafx.scene.control.TableView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;

//DIJKSTRA TO TRAVERSE?????
//int terrain; int timeToTraverse;
//AdmDiv[] neighbours;
//Adjacency matrix?
public class AdmDiv implements Serializable {
    // County,district,...
    private transient SVGProvince svgProvince; //if made transient it isn't saved / serialized
    private int provId, ownerId; //Correlate with provinces
    private String name;
    private String nativeName; // Durrës vs Durres ?

    private double area;
    private int population;
    private short mainLanguage; // + culture?, PopDistFloat/Double..., can be [][] and static methods there.
    //based on stability, rebellion can happen or if > 64
    //when annexing during war set rebellion to 16 32 or 64 (except provinces which consider us liberators)
    private byte[] rebellion; //types: separatism, autonomy,...
    //treat like
    EnumSet<Building> buildings;
    public EnumMap<Building, Byte> currProvBuildings;

    //private short[] claimedBy; (previous owners) ...
    //

    public String toString() {
        return this.name;
    }

    public String toStringLong() {
        return toStringBuilderLong().toString();
    }

    public StringBuilder toStringBuilderLong() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\nArea: ").append(area).append(" km^2\nPopulation: ").append(population);
        return sb;
    }

    public AdmDiv(String name, double area, int population, short mainLanguage) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.mainLanguage = mainLanguage;
        this.buildings = EnumSet.noneOf(Building.class);
        this.currProvBuildings = new EnumMap<>(Building.class);
        resetRebellion();
    }

    public AdmDiv(String name, String area, String population, short mainLanguage) {
        this.name = name;
        this.mainLanguage = mainLanguage;
        this.buildings = EnumSet.noneOf(Building.class);
        this.currProvBuildings = new EnumMap<>(Building.class);
        resetRebellion();
        try {
            this.area = Double.parseDouble(area);
            this.population = Integer.parseInt(population);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void resetRebellion() {
        //or EnumMap ...
        rebellion = new byte[RebelType.values().length];
        Arrays.fill(rebellion, (byte) 0);
    }

    public void addPopulation(int pop) {
        this.population += pop;
    }

    public void subtractPopulation(int pop) {
        if (pop > 0)
            return;
        this.population -= pop;
        if (this.population < 0)
            this.population = 0;
    }

    public int incPopulation(double incPop) {
        int pop = (int) (population * incPop);
        population += pop;
        return pop;
    }

    public void monthlyTick() {
        for (EnumMap.Entry<Building, Byte> entry : currProvBuildings.entrySet()) {
            Building b = entry.getKey();
            byte val = (byte) (entry.getValue() + 1);
            if (val >= b.stepsToBuild) {
                currProvBuildings.remove(b);
                buildings.add(b);
            } else {
                entry.setValue(val);
            }
        }
    }

    public void buildBuilding(Building b) {
        if (!buildings.contains(b))
            currProvBuildings.put(b, (byte) 1);
    }

    public boolean hasBuilding(Building b) {
        return buildings.contains(b);
    }

    public void instaBuildBuilding(Building b) {
        buildings.add(b);
    }

    public void demolishBuilding(Building b) {
        buildings.remove(b);
    }

    public EnumSet<Building> getBuildings() {
        return buildings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getProvId() {
        return provId;
    }

    public void setProvId(int provId) {
        this.provId = provId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
        if (svgProvince != null)
            svgProvince.setOwnerId(ownerId);
    }

    public short getMainLanguage() {
        return mainLanguage;
    }

    public void setMainLanguage(short mainLanguage) {
        this.mainLanguage = mainLanguage;
    }

    public void setFromSVGProvince(SVGProvince svg) {
        this.provId = svg.getProvId();
        this.ownerId = svg.getOwnerId();
        this.svgProvince = svg;
    }

    //if not player or subject id -> second column ...
//every month and when changing
    public void setValuesFromEnumMapSet(TableView<BuildBuildings.BuildBuilding> tableView) {
        Building[] builds = Building.values();
        for (int i = 0; i < builds.length; i++) {
            BuildBuildings.BuildBuilding bb = tableView.getItems().get(i);
            Building b = builds[i];
            if (buildings.contains(b)) {
                bb.setProgress(1.0);
            } else if (currProvBuildings.containsKey(b)) {
                bb.setProgress(currProvBuildings.get(b));
            } else {
                bb.setProgress(0.0);
            }
        }
    }

    public SVGProvince getSvgProvince() {
        return svgProvince;
    }

    public void setSvgProvince(SVGProvince svgProvince) {
        this.svgProvince = svgProvince;
    }
}
