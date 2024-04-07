package com.erimali.cntrygame;

import com.erimali.cntrymilitary.*;
import javafx.scene.control.TableView;

import java.io.Serializable;
import java.util.*;

//DIJKSTRA TO TRAVERSE?????
//int terrain; int timeToTraverse;
//AdmDiv[] neighbours;
//Adjacency matrix?
public class AdmDiv implements Serializable, Comparable<AdmDiv> {
    // County,district,...
    private transient SVGProvince svgProvince; //if made transient it isn't saved / serialized
    //private Country mainCountry; //update buildings on finished
    private int provId;
    private int ownerId;
    private String name;
    private String nativeName; // Durrës vs Durres ?
    private double area;
    private int population;
    private boolean landlocked;
    private short mainLanguage; // + culture?, PopDistFloat/Double..., can be [][] and static methods there.
    private short infrastructure;
    //based on stability, rebellion can happen or if > 64
    //when annexing during war set rebellion to 16 32 or 64 (except provinces which consider us liberators)
    private byte[] rebellion; //types: separatism, autonomy,...
    //treat like
    private EnumSet<Building> buildings;
    private EnumMap<Building, Byte> buildingBuildings;

    //private short[] claimedBy; (previous owners) ...
    //Mil
    //private List<MilUnit> unitsRecruitingBuild;
    //private List<MilUnit> unitsTrainingUpgrade;
    //list in GLogic so only the ones necessary are updated.
    private MilUnit unitRecruitingBuild;
//train more than recruit/build (maybe train division)
    //private MilUnit unitTrainingUpgrade;

    private List<MilUnit> units; // unit vs div or new interface
//boolean or sth

    public String toString() {
        return name;
    }

    public String toStringLong() {
        return toStringBuilderLong().toString();
    }

    public StringBuilder toStringBuilderLong() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\nArea: ").append(area).append(" km^2\nPopulation: ").append(population);
        return sb;
    }

    public AdmDiv(String name, double area, int population,boolean landlocked, short mainLanguage) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.landlocked = landlocked;
        this.mainLanguage = mainLanguage;
        this.buildings = EnumSet.noneOf(Building.class);
        this.buildingBuildings = new EnumMap<>(Building.class);
        this.units = new LinkedList<>();
        this.rebellion = new byte[RebelType.values().length];
        resetRebellion();
    }

    public AdmDiv(String name, String area, String population, short mainLanguage) {
        this.name = name;
        this.mainLanguage = mainLanguage;
        this.buildings = EnumSet.noneOf(Building.class);
        this.buildingBuildings = new EnumMap<>(Building.class);
        this.units = new LinkedList<>();
        this.rebellion = new byte[RebelType.values().length];
        resetRebellion();
        try {
            this.area = Double.parseDouble(area);
            this.population = Integer.parseInt(population);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void resetRebellion() {
        Arrays.fill(rebellion, (byte) 0);
    }

    public void sponsorRebellion(RebelType rt, byte amount) {
        if (amount > 0)
            rebellion[rt.ordinal()] += amount;
    }

    public void fightRebellion(byte amount) {

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
        for (EnumMap.Entry<Building, Byte> entry : buildingBuildings.entrySet()) {
            Building b = entry.getKey();
            byte val = (byte) (entry.getValue() + 1);
            if (val >= b.stepsToBuild) {
                buildingBuildings.remove(b);
                buildings.add(b);
            } else {
                entry.setValue(val);
            }
        }
    }

    public void buildBuilding(Building b) {
        if (!buildings.contains(b))
            buildingBuildings.put(b, (byte) 1);
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

    public EnumMap<Building, Byte> getBuildingBuildings() {
        return buildingBuildings;
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
            } else if (buildingBuildings.containsKey(b)) {
                bb.setProgress(buildingBuildings.get(b));
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


    public void cancelUnitProcess() {

    }

    //stopped automatically when maxSize reached
    public int recruitBuildTenth() {
        if (unitRecruitingBuild == null) {
            return Integer.MIN_VALUE;
        } else {
            //how much manpower/resources goes in (?)
            return unitRecruitingBuild.recruitBuild();
        }
    }

    public int recruitBuild() {
        if (unitRecruitingBuild == null) {
            return Integer.MIN_VALUE; //maybe max value
        }
        if (unitRecruitingBuild instanceof MilSoldiers milSoldiers) {
            int extra = milSoldiers.recruit(100);
            if (extra > 0) {
                units.add(unitRecruitingBuild);
                unitRecruitingBuild = null;
            }
            return extra;//fix based on max
        } else if (unitRecruitingBuild instanceof MilVehicles milVehicles) {
            int extra = milVehicles.build(10);
            if (extra > 0) {
                units.add(unitRecruitingBuild);
                unitRecruitingBuild = null;
            }
            return extra;
        } else {
            return Integer.MAX_VALUE;
        }

    }


    public MilUnit getUnitRecruitingBuild() {
        return unitRecruitingBuild;
    }

    public void setUnitRecruitingBuild(MilUnit unitRecruitingBuild) {
        this.unitRecruitingBuild = unitRecruitingBuild;
    }

   /*
   //started/stopped when player clicks
    public void trainUpgrade() {
        if (unitTrainingUpgrade == null) {
            return;
        }
        if (unitRecruitingBuild instanceof MilSoldiers milSoldiers) {
            milSoldiers.train(100);//fix based on max
        } else if (unitRecruitingBuild instanceof MilVehicles milVehicles) {
            milVehicles.upgrade(10);
        }
    }
   public MilUnit getUnitTrainingUpgrade() {
        return unitTrainingUpgrade;
    }

    public void setUnitTrainingUpgrade(MilUnit unitTrainingUpgrade) {
        this.unitTrainingUpgrade = unitTrainingUpgrade;
    }*/

    public void makeMilUnit(MilUnitData d) {

    }

    public void addUnit(MilUnit u) {
        units.add(u);
    }

    public boolean removeUnit(MilUnit u) {
        return units.remove(u);
    }

    public MilUnit removeUnit(int i) {
        return units.remove(i);
    }

    @Override
    public int compareTo(AdmDiv o) {
        return Integer.compare(this.provId, o.provId);
    }
}
