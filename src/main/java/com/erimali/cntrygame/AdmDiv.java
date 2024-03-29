package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.EnumSet;

//DIJKSTRA TO TRAVERSE?????
//int terrain; int timeToTraverse;
//AdmDiv[] neighbours;
//Adjacency matrix?
public class AdmDiv implements Serializable {
    // County,district,...
    //private transient SVGProvince svgProvince; //if made transient it isn't saved / serialized
    private int provId, ownerId; //Correlate with provinces
    private String name;
    private String nativeName; // Durrës vs Durres ?

    private double area;
    private int population;
    private short mainLanguage; // + culture?

    private byte[] rebellion; //types: separatism, autonomy,...
    EnumSet<Building> buildings;

    //private short[] claimedBy; (previous owners) ...
    //

    public String toString() {
        return this.name;
    }

    public String toStringLong() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\nArea: ").append(area).append(" km^2\nPopulation: ").append(population);
        return sb.toString();
    }

    public AdmDiv(String name, double area, int population, short mainLanguage) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.mainLanguage = mainLanguage;
        this.buildings = EnumSet.noneOf(Building.class);
    }

    public AdmDiv(String name, String area, String population, short mainLanguage) {
        this.name = name;
        try {
            this.area = Double.parseDouble(area);
            this.population = Integer.parseInt(population);
            this.mainLanguage = mainLanguage;
            this.buildings = EnumSet.noneOf(Building.class);

        } catch (NumberFormatException e) {

        }
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

    public boolean hasBuilding(Building b) {
        return buildings.contains(b);
    }

    public void buildBuilding(Building b) {
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
    }

    public short getMainLanguage() {
        return mainLanguage;
    }

    public void setMainLanguage(short mainLanguage) {
        this.mainLanguage = mainLanguage;
    }

    public void setFromSVGProvince(SVGProvince svg){
        this.provId = svg.getProvId();
        this.ownerId = svg.getOwnerId();
    }

}
