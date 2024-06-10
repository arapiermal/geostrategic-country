package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilDiv;
import com.erimali.cntrymilitary.MilUnit;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;


import java.io.Serializable;
import java.util.*;

interface ProvinceFight {

    int dayTick();
}

public class War implements Serializable {

    //if there's list in each province, no need for this stuff here.
    static class Battle implements Serializable {
        //if many MilDiv in the same province, after one is defeated, check province to fight the rest
        int provId;
        List<MilUnit> a;
        List<MilUnit> o;

        public Battle(int provId, List<MilUnit> a, List<MilUnit> o) {
            this.provId = provId;
            this.a = a;
            this.o = o;
        }

        public int dayTick() {
            return MilDiv.attack(a, o);
        }

    }

    static class BattleResult implements Serializable {

    }

    //also through wars ...
    public void dayTick() {
        for (Battle b : activeBattles) {
            int res = b.dayTick();
            //now there's res based on units destroyed/retreated, change formula
            switch (res) {
                case -2:
                    //Defeated
                    addWarState(-4);
                    break;
                case -1:
                    addWarState(-2);
                    //Retreating
                    break;
                case 0:
                    //The battle continues
                    break;
                case 1:
                    //Opponent retreat
                    addWarState(2);
                    break;
                case 2:
                    //Win
                    addWarState(4);
                    break;
            }
            if (res != 0) {
                activeBattles.remove(b);
            }
        }
    }

    // each country in war having warState?
    // if warState in disfavor, AI likely to accept terms
    private final GLogic game;
    private final CasusBelli casusBelli; //enum? array? //loadable casus bellis?
    private EnumSet<WarObjective> allowedObjectives;//?? dependent on casusBelli
    private DoubleProperty[] warStates; // from -100 to 100
    private Country declaringCountry;//what if Military, and in constructor get it
    private Country opposingCountry;
    private Set<Integer> declaringAllies; //Which accepted to enter war
    private Set<Integer> opposingAllies;
    private List<Battle> activeBattles;
    private ObservableList<AdmDiv> declaringOccupiedProv;
    private ObservableList<AdmDiv> opposingOccupiedProv;
    //if reference to Country inside military, Country <-> Military
    private String warResult; //...

    //get allies -> check relations and stability of those countries... join war
    public War(GLogic game, Country declaringCountry, Country opposingCountry, CasusBelli casusBelli) {
        this.warStates = new DoubleProperty[2];
        warStates[0] = new SimpleDoubleProperty(0);
        warStates[1] = new SimpleDoubleProperty(0);
        this.declaringCountry = declaringCountry;
        this.opposingCountry = opposingCountry;
        this.declaringAllies = new HashSet<>();
        this.opposingAllies = new HashSet<>();
        declaringCountry.getMilitary().addAtWarWith(opposingCountry.getCountryId());
        opposingCountry.getMilitary().addAtWarWith(declaringCountry.getCountryId());
        this.casusBelli = casusBelli;
        this.activeBattles = new LinkedList<>();
        this.declaringOccupiedProv = FXCollections.observableArrayList();
        this.opposingOccupiedProv = FXCollections.observableArrayList();
        this.game = game;
    }


    public boolean contains(int cId) {
        return declaringCountry.getCountryId() == cId || opposingCountry.getCountryId() == cId || declaringAllies.contains(cId) || opposingAllies.contains(cId);
    }

    //which side is AI...
    //private int player; //if >=0 there is at least the player that's not AI
    public void calcAlliesAI() {

    }

    public void bringAlly() {

    }

    //or float/double warState[] based on country ...
    public double getWarState(Country c) {
        return (c == declaringCountry || declaringAllies.contains(c.getCountryId())) ? warStates[0].get() : warStates[1].get();
    }

    public ObservableList<AdmDiv> getOccupiedProvinces(Country c) {
        return (c == declaringCountry || declaringAllies.contains(c.getCountryId())) ? declaringOccupiedProv : opposingOccupiedProv;
    }

    public ObservableList<AdmDiv> getOccupiedProvinces(boolean isDeclaring) {
        return isDeclaring ? declaringOccupiedProv : opposingOccupiedProv;
    }

    public boolean isDeclaring(int cId) {
        return (declaringCountry.getCountryId() == cId || declaringAllies.contains(cId));
    }

    public boolean isOpposing(int cId) {
        return (opposingCountry.getCountryId() == cId || opposingAllies.contains(cId));
    }

    public ObservableList<AdmDiv> getOccupiedProvincesByDeclaring() {
        return declaringOccupiedProv;
    }

    public ObservableList<AdmDiv> getOccupiedProvincesByOpposing() {
        return opposingOccupiedProv;
    }

    public void startBattle(int provId, List<MilUnit> a, List<MilUnit> o) {

    }

    public void negotiate() {

    }

    //or int... arg
    public void finishWar(int... arg) {
        if (arg.length == 0) {
            // withdrawal/draw
        } else {

        }
    }

    /*
    public static ListView<CasusBelli> makeListViewCasusBelli(Country c1, Country c2) {
        ListView<CasusBelli> lv = new ListView<>();
        for (CasusBelli cb : CasusBelli.values()) {
            if (cb.isValid(c1, c2)) {
                lv.getItems().add(cb);
            }
        }
        return lv;
    }*/

    public static <T extends Enum<T> & CValidatable> ListView<T> makeListViewValidatable(GLogic game, int cInd1, int cInd2, Class<T> enumClass) {
        ListView<T> lv = new ListView<>();
        for (T it : enumClass.getEnumConstants()) {
            if (it.isValid(game, cInd1, cInd2)) {
                lv.getItems().add(it);
            }
        }
        return lv;
    }

    public Set<Integer> getDeclaringAllies() {
        return declaringAllies;
    }

    public Set<Integer> getOpposingAllies() {
        return opposingAllies;
    }

    public boolean containsAsMains(int cId1, int cId2) {
        int a = declaringCountry.getCountryId();
        int o = opposingCountry.getCountryId();
        return a == cId1 && o == cId2 || a == cId2 && o == cId1;
    }

    public boolean containsAsMains(Country c1, Country c2) {
        return c1.equals(declaringCountry) && c2.equals(opposingCountry) || c2.equals(declaringCountry) && c1.equals(opposingCountry);
    }

    @Override
    public String toString() {
        return declaringCountry.getName() + " vs " + opposingCountry.getName() + " - " + casusBelli.toString();
    }

    public DoubleProperty warStateProperty(Country c) {
        return isDeclaring(c.getCountryId()) ? warStates[0] : warStates[1];
    }

    public DoubleProperty warStateProperty(boolean isDeclaring) {
        return isDeclaring ? warStates[0] : warStates[1];
    }

    public void addWarState(int cId, int val) {
        if (isDeclaring(cId)) {
            addWarState(val);
        } else {
            addWarState(-val);
        }
    }

    public void addWarState(int val) {
        warStates[0].set(warStates[0].get() + val);
        warStates[1].set(warStates[1].get() - val);
    }
}
