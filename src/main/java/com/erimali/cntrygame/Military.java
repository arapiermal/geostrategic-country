package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.*;

import com.erimali.cntrymilitary.MilDiv;
import javafx.scene.shape.SVGPath;

public class Military implements Serializable {
    //named divisions (?)
    private long manpower;
    private short[] milTechLevel;
    private short[] milTechProgress;
    private List<MilDiv> divisions;
    private Set<Short> atWarWith;


    public Military() {
        divisions = new ArrayList<>();
        atWarWith = new HashSet<>();
    }


    private boolean progressMilTech(int type, int amount) {
        if (type < 0 || type >= milTechProgress.length || amount < 0)
            return false;
        milTechProgress[type] += (short) amount;
        if (milTechProgress[type] >= 100) {
            milTechLevel[type]++;
            milTechProgress[type] -= 100;
            return true;
        }
        return false;
    }


    public void addDivision(MilDiv d) {
        divisions.add(d);
    }


}
