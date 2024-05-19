package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilDiv;
import com.erimali.cntrymilitary.MilUnit;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;


import java.io.Serializable;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

interface ProvinceFight{

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
                    warState -= 4;
                    break;
                case -1:
                    warState -= 2;
                    //Retreating
                    break;
                case 0:
                    //The battle continues
                    break;
                case 1:
                    //Opponent retreat
                    warState += 2;
                    break;
                case 2:
                    //Win
                    warState += 4;
                    break;
            }
            if (res != 0) {
                activeBattles.remove(b);
            }
        }
    }

    // each country in war having warState?
    // if warState in disfavor, AI likely to accept terms
    private final CasusBelli casusBelli; //enum? array? //loadable casus bellis?
    private EnumSet<WarObjective> allowedObjectives;//?? dependent on casusBelli
    private double warState; // from -100 to 100
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
    public War(Country declaringCountry, Country opposingCountry, CasusBelli casusBelli) {
        this.declaringCountry = declaringCountry;
        this.opposingCountry = opposingCountry;
        declaringCountry.getMilitary().addAtWarWith(opposingCountry.getCountryId());
        opposingCountry.getMilitary().addAtWarWith(declaringCountry.getCountryId());
        this.casusBelli = casusBelli;
        this.activeBattles = new LinkedList<>();

    }

    public War(Country declaringCountry, Country opposingCountry, CasusBelli casusBelli, CountryArray cArr) {
        this.declaringCountry = declaringCountry;
        this.opposingCountry = opposingCountry;
        declaringCountry.getMilitary().addAtWarWith(opposingCountry.getCountryId());
        opposingCountry.getMilitary().addAtWarWith(declaringCountry.getCountryId());
        this.casusBelli = casusBelli;
        this.activeBattles = new LinkedList<>();

    }

    //which side is AI...
    //private int player; //if >=0 there is at least the player that's not AI
    public void calcAlliesAI() {

    }

    public void bringAlly() {

    }
//or float/double warState[] based on country ...
    public double getWarState(Country c) {
        return (c == declaringCountry || declaringAllies.contains(c)) ? warState : -warState;
    }

    public ObservableList<AdmDiv> getOccupiedProvinces(Country c) {
        return (c == declaringCountry || declaringAllies.contains(c)) ? declaringOccupiedProv : opposingOccupiedProv;
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
            if (it.isValid(game,cInd1,cInd2)) {
                lv.getItems().add(it);
            }
        }
        return lv;
    }

    public Set<Integer> getDeclaringAllies() {
        return declaringAllies;
    }

    public void setDeclaringAllies(Set<Integer> declaringAllies) {
        this.declaringAllies = declaringAllies;
    }

    public Set<Integer> getOpposingAllies() {
        return opposingAllies;
    }

    public void setOpposingAllies(Set<Integer> opposingAllies) {
        this.opposingAllies = opposingAllies;
    }

    @Override
    public String toString() {
        return declaringCountry.getName() + " vs " + opposingCountry.getName() + " - " + casusBelli.toString();
    }

}
