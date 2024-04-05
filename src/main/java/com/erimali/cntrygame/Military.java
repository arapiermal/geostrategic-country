package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.*;

import com.erimali.cntrymilitary.MilDiv;
import com.erimali.cntrymilitary.MilUnitData;
import javafx.scene.shape.SVGPath;

public class Military implements Serializable {
    //named divisions (?)
    private long manpower;//influenced by government policies / other attributes (popwillingness)
    private static final short MIL_TECH_LEVEL_CAP = 100;
    private short[] milTechLevel;
    private short[] milTechProgress;
    private List<MilDiv> divisions;
    //Have at least 1 division in all times?
    private Set<Short> atWarWith;


    public Military() {
        divisions = new ArrayList<>();
        atWarWith = new HashSet<>();
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


    public void addDivision(MilDiv d) {
        divisions.add(d);
    }
    public void addDivisions(MilDiv... d) {
        Collections.addAll(divisions, d);
    }

    //public void seizeVehicles(MilDiv d){}

    public List<MilDiv> getDivisions() {
        return divisions;
    }

    public void setDivisions(List<MilDiv> divisions) {
        this.divisions = divisions;
    }

    public Set<Short> getAtWarWith() {
        return atWarWith;
    }
    public void addAtWarWith(short... o){
        for(short s : o){
            atWarWith.add(s);
        }
    }
    public boolean isAtWarWith(short s){
        return atWarWith.contains(s);
    }

    public void correlateUnitData(List<MilUnitData>[] unitTypes) {
        for(MilDiv d : divisions){
            d.correlateUnitData(unitTypes);
        }
    }
}
