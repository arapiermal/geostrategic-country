package com.erimali.cntrymilitary;

import com.erimali.cntrygame.ErrorLog;
import com.erimali.cntrygame.TESTING;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Military implements Serializable {
    private static final double[] DEF_POP_CONSCRIPTION_RATES = {0.01, 0.025, 0.05, 0.1, 0.25, 0.5};
    private static final int MAX_UNIT_TYPES = MilUnitData.getMaxTypes();
    private double popConscriptionRate;
    private LongProperty manpower;//influenced by government policies / other attributes (popwillingness)
    //manpower down when making or recovering MilUnit
    //manpower up when
    private long lastManpowerMonthlyIncrease;
    private long activePersonnel; // ! when adding type isPersonnel() , battles -> activepersonnel down, population down !
    private static final short MIL_TECH_LEVEL_CAP = 100;
    private final short[] milTechProgress;
    private final short[] milTechLevel;
    private final boolean[] researchingMilTech;
    //extract the cost from the Country
    private short baseResearch;
    private final ObservableList<MilDiv> divisions;
    //Have at least 1 division in all times (?)
    private final Set<Integer> atWarWith;


    public Military() {
        this.popConscriptionRate = 0.025;
        this.manpower = new SimpleLongProperty(1000);
        this.baseResearch = 10;
        this.divisions = FXCollections.observableArrayList();
        this.atWarWith = new HashSet<>();
        this.milTechProgress = new short[MAX_UNIT_TYPES];
        Arrays.fill(milTechProgress, (short) 0);
        this.milTechLevel = new short[MAX_UNIT_TYPES];
        Arrays.fill(milTechLevel, (short) 0);
        this.researchingMilTech = new boolean[MAX_UNIT_TYPES];
        Arrays.fill(researchingMilTech, false);
    }


    //if true pop up benefits/new available units
    private boolean progressMilTech(int type, int amount) {
        if (type < 0 || type >= milTechProgress.length || amount < 0)
            return false;
        milTechProgress[type] += (short) amount;
        if (milTechProgress[type] >= MIL_TECH_LEVEL_CAP) {
            milTechLevel[type]++;
            milTechProgress[type] -= MIL_TECH_LEVEL_CAP;
            return true;
        }
        return false;
    }

    public void monthlyResearch(short researchBonus) {
        for (int i = 0; i < MAX_UNIT_TYPES; i++) {
            if (researchingMilTech[i]) {
                addMilTechProgress(i, (short) (baseResearch + researchBonus));
            }
        }
    }

    public void makeUnit() {

    }

    public void addDivision(MilDiv d) {
        divisions.add(d);
    }

    public ObservableList<MilDiv> getDivisions() {
        return divisions;
    }

    public Set<Integer> getAtWarWith() {
        return atWarWith;
    }

    public void addAtWarWith(int... o) {
        for (int s : o) {
            atWarWith.add(s);
        }
    }

    public boolean isAtWarWith(int s) {
        return atWarWith.contains(s);
    }

    public void correlateUnitData(List<MilUnitData>[] unitTypes) {
        for (MilDiv d : divisions) {
            d.correlateUnitData(unitTypes);
        }
    }

    public short getMilTechProgress(int i) {
        return milTechProgress[i];
    }

    public short[] getMilTechProgress() {
        return milTechProgress;
    }

    public short getMilTechLevel(int i) {
        return milTechLevel[i];
    }

    public void addMilTechProgress(int type, short amount) {
        milTechProgress[type] += amount;
        //short lvlCap = MIL_TECH_LEVEL_CAP * milTechLevel[type];
        if (milTechProgress[type] >= MIL_TECH_LEVEL_CAP) {
            short lvlUp = (short) (milTechProgress[type] / MIL_TECH_LEVEL_CAP);
            milTechLevel[type] += lvlUp;
            milTechProgress[type] %= MIL_TECH_LEVEL_CAP;
        }
    }

    public short getMilTechLevelCap() {
        return MIL_TECH_LEVEL_CAP;
    }

    //change !!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public short getMilTechLevelCap(short lvl) {
        return (short) (MIL_TECH_LEVEL_CAP * lvl);
    }


    protected static List<MilUnitData>[] unitTypes;
    protected static String DIR_UNIT_TYPES = "src/main/resources/data/units";

    //call after loading save game
    public static void loadAllUnitData(List<MilUnitData>[] unitTypes) {
        if (unitTypes == null) {
            //noinspection unchecked
            Military.unitTypes = (List<MilUnitData>[]) new ArrayList[MilUnitData.MAX_TYPES];
            unitTypes = Military.unitTypes;
        }
        for (int i = 0; i < MilUnitData.MAX_TYPES; i++) {
            unitTypes[i] = new ArrayList<>();

        }
        Path dir = Paths.get(DIR_UNIT_TYPES);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                try {
                    MilUnitData d = new MilUnitData(entry.toString());
                    unitTypes[d.type].add(d);
                } catch (Exception e) {
                    ErrorLog.logError(e);
                }
            }
            for (int i = 0; i < MilUnitData.MAX_TYPES; i++) {
                Collections.sort(unitTypes[i]);
            }
        } catch (IOException e) {
            ErrorLog.logError(e);
        }
    }

    //Use in military for GUI
    public static List<MilUnitData> getUnitTypesList(int type) {
        return unitTypes[type];
    }

    public static MilUnit makeUnit(int ownerId, int type, int index) {
        if (type < 0 || type >= unitTypes.length || index < 0 || index > unitTypes[type].size())
            return null;
        if (unitTypes[type].isEmpty())
            return null;
        MilUnitData data = unitTypes[type].get(index);
        MilUnit unit = (type % 2 == 0) ? new MilSoldiers(data, ownerId) : new MilVehicles(data, ownerId);
        return unit;
    }

    public static void main(String[] args) {
        loadAllUnitData(null);

        MilUnit u = makeUnit(0, 0, 0);
        MilUnit o = makeUnit(1, 0, 0);
        u.incSize(1000);
        o.incSize(500);
        //u.incLevel(1);
        o.incLevel(2);
        int res;
        while ((res = u.attack(o)) == 0) {
            TESTING.print(u.size + " " + u.morale, o.size + " " + o.morale);
        }
        TESTING.print(res > 0 ? "WIN" : "LOST");
    }

    public String unitDataTypesToString(List<MilUnitData>[] a) {
        return unitDataTypesToString(milTechLevel, a);
    }

    public static String unitDataTypesToString(short[] milTechLevel, List<MilUnitData>[] a) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (List<MilUnitData> l : unitTypes) {
            sb.append("Type ").append(i).append(':').append(MilUnitData.getUnitTypeName(i++)).append('\n');
            for (MilUnitData d : l) {
                if (milTechLevel[d.type] >= d.minMilTech)
                    sb.append(d.subtype).append(')').append(d).append('\n');
            }
        }
        return sb.toString();
    }

    public String toStringLong() {
        StringBuilder sb = new StringBuilder();
        for (MilDiv d : divisions) {
            sb.append(d.toString()).append('\n');
            for (MilUnit u : d.getUnits()) {
                sb.append(u.toString()).append('\n');
            }
            sb.append("~").append('\n');
        }

        return sb.toString();
    }

    public String toStringDivs() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (MilDiv d : divisions) {
            sb.append(i++).append(')').append(d.toString()).append('\n');
        }
        return sb.toString();
    }

    public int attack() {
        return 0;
    }

    public MilDiv getDivision(int i) {
        return divisions.get(i);
    }

    public boolean removeDivision(MilDiv remDiv) {
        return divisions.remove(remDiv);
    }

    public void seizeVehicles(Military o) {
        List<MilUnit> vehicles = new ArrayList<>();
        for (MilDiv d : o.divisions) {
            for (MilUnit u : d.getUnits()) {
                if (u instanceof MilVehicles) {
                    vehicles.add(u);
                    d.removeUnit(u);
                }
            }
        }
        divisions.add(new MilDiv("Seized vehicles", vehicles));
    }

    public void takeDivisions(Military o) {
        while (!o.divisions.isEmpty()) {
            divisions.add(o.divisions.removeFirst());
        }
    }

    public void changeUnitDiv(MilDiv d1, MilUnit u, MilDiv d2) {
        d1.removeUnit(u);
        d2.addUnit(u);
    }

    public LongProperty manpowerProperty(){
        return manpower;
    }

    public long getManpower() {
        return manpower.get();
    }

    public void setManpower(long manpower) {
        this.manpower.set(manpower);
    }

    public void monthlyTick(long population, short researchBonus) {
        addManpowerFromPop(population);
        monthlyResearch(researchBonus);
    }

    public void addManpowerFromPop(long population) {
        lastManpowerMonthlyIncrease = (long) (population * popConscriptionRate / 60);
        if (lastManpowerMonthlyIncrease < population) {
            manpower.set(manpower.get() + lastManpowerMonthlyIncrease);
        } else {
            lastManpowerMonthlyIncrease = 0;
        }
    }

    public double getPopConscriptionRate() {
        return popConscriptionRate;
    }

    public void setPopConscriptionRate(double popConscriptionRate) {
        this.popConscriptionRate = popConscriptionRate;
    }

    public void setPopConscriptionRate(int index) {
        if (index >= 0 && index < DEF_POP_CONSCRIPTION_RATES.length)
            this.popConscriptionRate = DEF_POP_CONSCRIPTION_RATES[index];
    }

    public static double[] getDefPopConscriptionRates() {
        return DEF_POP_CONSCRIPTION_RATES;
    }

    public long getLastManpowerMonthlyIncrease() {
        return lastManpowerMonthlyIncrease;
    }

    public boolean getResearching(int i) {
        if (i >= 0 && i < researchingMilTech.length) {
            return researchingMilTech[i];
        } else {
            return false;
        }
    }

    public void incBaseResearch(short amount) {
        if (amount > 0)
            baseResearch += amount;
    }

    public void decBaseResearch(short amount) {
        if (amount > 0)
            baseResearch -= amount;
        if (baseResearch < 1)
            baseResearch = 1;
    }

    public void toggleResearching(int i) {
        if (i >= 0 && i < researchingMilTech.length) {
            researchingMilTech[i] = !researchingMilTech[i];
        }
    }

    public int getPopConscriptionRateIndex() {
        for (int i = 0; i < DEF_POP_CONSCRIPTION_RATES.length; i++) {
            if (popConscriptionRate == DEF_POP_CONSCRIPTION_RATES[i])
                return i;
        }
        return 0;
    }

    public short[] getMilTechLevel() {
        return milTechLevel;
    }

    public boolean[] getResearchingMilTech() {
        return researchingMilTech;
    }
}