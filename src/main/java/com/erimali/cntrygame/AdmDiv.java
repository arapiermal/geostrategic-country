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
    private int occupierId = -1;
    private String name;


    private String nativeName;
    private double area;
    private int population;
    private boolean waterAccess;
    private short mainLanguage; // + culture?, PopDistFloat/Double..., can be [][] and static methods there.
    private short infrastructure;

    private float maxDefense = 100;
    private float defense = 100; //when conquering, infrastructure * 0.5 + defense
    //based on stability, rebellion can happen or if > 64
    //when annexing during war set rebellion to 16 32 or 64 (except provinces which consider us liberators)
    private final byte[] rebellion; //types: separatism, autonomy,...
    //treat like
    private final EnumSet<Building> buildings;
    private final EnumMap<Building, Byte> buildingBuildings;

    //private short[] claimedBy; (previous owners) ...

    //list in GLogic so only the ones necessary are updated.
    private MilUnit unitRecruitingBuild;
    //train more than recruit/build (maybe train division)
    //private MilUnit unitTrainingUpgrade;
//select based on multiple selection model?
    private List<MilUnit> friendlyUnits; // unit vs div or new interface
    private List<MilUnit> enemyUnits;
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

    public AdmDiv(String name, double area, int population, boolean waterAccess, short mainLanguage) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.waterAccess = waterAccess;
        this.mainLanguage = mainLanguage;
        this.infrastructure = 1;
        this.buildings = EnumSet.noneOf(Building.class);
        this.buildingBuildings = new EnumMap<>(Building.class);
        this.friendlyUnits = new LinkedList<>();
        this.enemyUnits = new LinkedList<>();
        this.rebellion = new byte[RebelType.values().length];
        resetRebellion();
    }

    public AdmDiv(String name, String area, String population, short mainLanguage) {
        this.name = name;
        this.mainLanguage = mainLanguage;
        this.infrastructure = 1;
        this.buildings = EnumSet.noneOf(Building.class);
        this.buildingBuildings = new EnumMap<>(Building.class);
        this.friendlyUnits = new LinkedList<>();
        this.enemyUnits = new LinkedList<>();
        this.rebellion = new byte[RebelType.values().length];
        resetRebellion();
        try {
            this.area = Double.parseDouble(area);
            this.population = Integer.parseInt(population);
        } catch (NumberFormatException e) {
            this.area = 10.0;
            this.population = 1000;
            ErrorLog.logError(name + " " + e);
        }
    }

    public void resetRebellion() {
        Arrays.fill(rebellion, (byte) 0);
    }

    public void sponsorRebellion(RebelType rt, int sponsorId, byte amount) {
        if (amount > 0) {
            int i = rt.ordinal();
            rebellion[i] += amount;
            checkRebellionArmy(i, sponsorId);
        }
    }

    public void checkRebellionArmy(int i, int sponsorId) {
        if (rebellion[i] >= 100) {
            rebellion[i] = 0;
            popupRebelArmy(i, sponsorId);
        }
    }

    //based on the provinces gdp the sponsor part?
    public void popupRebelArmy(int r, int sponsorId) {
        int n = population / 50000;
        int size = MilRebels.getRebelSoldiersData().getMaxSize();
        //at least one even on small provinces
        if (n <= 0) {
            MilRebels rebels = new MilRebels(sponsorId, RebelType.values()[r]);
            int amount = population / 10;
            int extra = rebels.incSize(amount);
            amount -= extra;
            population -= amount;
            enemyUnits.add(rebels);
        } else {
            for (int i = 0; i < n; i++) {
                enemyUnits.add(new MilRebels(sponsorId, RebelType.values()[r], true));
                population -= size;
            }
        }
    }

    public void fightRebellion(RebelType rt, byte amount) {
        if (amount > 0)
            rebellion[rt.ordinal()] -= amount;
    }

    public void addPopulation(int pop) {
        this.population += pop;
    }

    public void subtractPopulation(int pop) {
        if (pop > 0)
            return;
        this.population -= pop;
        if (this.population < 2)
            this.population = 2;
    }

    public int incPopulation(double percent) {
        int pop = (int) (population * percent);
        population += pop;
        return pop;
    }

    public void substractAllRebellion(byte a) {
        for (int i = 0; i < rebellion.length; i++) {
            if (rebellion[i] > 0) {
                rebellion[i] -= a;
                if (rebellion[i] < 0)
                    rebellion[i] = 0;
            }
        }
    }

    public void monthlyTick() {
        if (friendlyUnits != null && !friendlyUnits.isEmpty())
            substractAllRebellion((byte) 1);
    }

    public void yearlyTick() {
        substractAllRebellion((byte) 10);

    }

    // (int amount) for speed ups
    public void buildingTick() {
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
        //return buildingBuildings.isEmpty();
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
        setUnoccupied();
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
                friendlyUnits.add(unitRecruitingBuild);
                unitRecruitingBuild = null;
            }
            return extra;//fix based on max
        } else if (unitRecruitingBuild instanceof MilVehicles milVehicles) {
            int extra = milVehicles.build(10);
            if (extra > 0) {
                friendlyUnits.add(unitRecruitingBuild);
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
        friendlyUnits.add(u);
    }

    public boolean removeUnit(MilUnit u) {
        return friendlyUnits.remove(u);
    }

    public MilUnit removeUnit(int i) {
        return friendlyUnits.remove(i);
    }

    @Override
    public int compareTo(AdmDiv o) {
        return Integer.compare(this.provId, o.provId);
    }

    public boolean hasWaterAccess() {
        return waterAccess;
    }

    public void incInfrastructure() {
        infrastructure++;
    }

    public short getInfrastructure() {
        return infrastructure;
    }

    public void setInfrastructure(short infrastructure) {
        this.infrastructure = infrastructure;
    }

    public int getOccupierId() {
        return occupierId;
    }

    public void setOccupierId(int occupierId) {
        this.occupierId = occupierId;
        if (svgProvince != null)
            svgProvince.setOccupierId(occupierId);
    }

    public void setUnoccupied() {
        this.occupierId = -1;
        if (svgProvince != null)
            svgProvince.setOccupierId(occupierId);
    }

    //Rebels that aren't of a particular country, special occupierId.
    public boolean isOccupied() {
        return occupierId >= 0;
    }
    public boolean isOccupiedByRebels() {
        return occupierId >= CountryArray.getMaxIso2Countries();
    }
    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public float getMaxDefense() {
        return maxDefense;
    }

    public void setMaxDefense(float maxDefense) {
        this.maxDefense = maxDefense;
    }

    public float getDefense() {
        return defense;
    }

    public void setDefense(float defense) {
        this.defense = defense;
    }

    public boolean sameName(String id) {
        if (nativeName == null)
            return id.equalsIgnoreCase(name);
        return id.equalsIgnoreCase(name) || id.equalsIgnoreCase(nativeName);
    }

    public boolean decDefense(float amount, int occupierId) {
        defense -= amount;
        if (defense <= 0) {
            this.defense = 0;
            setOccupierId(occupierId);
            return true;
        }
        return false;
    }
}
