package com.erimali.cntrygame;

import java.util.*;

import com.erimali.cntrymilitary.MilDiv;
import javafx.scene.shape.SVGPath;

class MilImg {
    SVGPath svg;

    public MilImg(String path) {
        svg = new SVGPath();
        svg.setContent(path);
    }
    // soldier cap
    // navy symbol
    // airplane symbol
}

public class Military {
    //named divisions (?)
    private long manpower;
    private int milTechLevel;
    private int milTechProgress;
    private List<MilDiv> divisions;
    private Set<Short> atWarWith;

    private GDate lastDeclaredWar;


    public Military() {
        divisions = new ArrayList<>();
        atWarWith = new HashSet<>();
    }

    public void addDivision(MilDiv d) {
        divisions.add(d);
    }

    public void attackMilitary(Military o) {

    }

    //........
    public void attackMilitary(List<MilDiv> m, List<MilDiv> others) {

    }

}
