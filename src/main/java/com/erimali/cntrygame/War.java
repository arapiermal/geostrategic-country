package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilDiv;
import com.erimali.cntrymilitary.MilUnit;
import javafx.scene.control.ListView;


import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

enum WarObjectives {
    //if all provinces selected -> full annexation
    //There can be partly annexation! cost cannot be a stuck value, what can be better
    ANNEX(100) {
        //flip flop warState, if negative but the opponent is player, make positive?
        //2 cArr if different worlds (?!?!?)
        //or should there be reference to world in each country ????
        public void action(CountryArray cArr1, int ind1, CountryArray cArr2, int ind2, float warState, int... args) {
            //factors that effect?
            Country c1 = cArr1.get(ind1);
            Country c2 = cArr2.get(ind2);
            if (warState > this.getCost()) {
                c1.annexCountry(cArr2, ind2, false);
            }
        }
        public void action(Country c1, Country c2, float warState, int... args) {
            //c1.annexCountry(c2);
        }
    },
    //types???
    SUBJUGATE(85) {
        @Override
        public void action(Country c1, Country c2, float warState, int... args) {
            //c1.subjugateCountry(c2);

        }
    },
    REGIME_CHANGE(70) {
        @Override
        public void action(Country c1, Country c2, float warState, int... args) {
            //make same regime as self
            if (args[0] == 0) {
                c2.getGovernment().setType(c1.getGovernment().getType());
            }
        }
    },
    //handle better
    DISMANTLE_MILITARY(80) {
        public void action(Country c1, Country c2, float warState, int... args) {
            c2.setMilitary(null);
            int years = args[0];
            c2.getGovernment().addPolicy(GovPolicy.BANNED_MILITARY, years);
        }
    };
    private float cost;

    WarObjectives(float cost) {
        this.cost = cost;
    }

    public abstract void action(Country c1, Country c2, float warState, int... args);

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }
}


public class War implements Serializable {
    static class Battle implements Serializable{
        //if many MilDiv in the same province, after one is defeated, check province to fight the rest
        int provId;
        List<MilUnit> a;
        List<MilUnit> o;
        public Battle(int provId, List<MilUnit> a, List<MilUnit> o){
            this.provId = provId;
            this.a = a;
            this.o = o;
        }
        public int dayTick(){
            return MilDiv.attack(a, o);
        }

    }
    static class BattleResult implements Serializable {

    }
    //also through wars ... 
    public void dayTick(){
        for(Battle b : activeBattles){
            int res = b.dayTick();
            switch (res){
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
            if(res != 0){
                activeBattles.remove(b);
            }
        }
    }
    // each country in war having warState?
    // if warState in disfavor, AI likely to accept terms
    private CasusBelli casusBelli; //enum? array? //loadable casus bellis?
    private WarObjectives[] allowedObjectives;//?? dependent on casusBelli
    private float warState; // from -100 to 100
    private Country declaringCountry;//what if Military, and in constructor get it
    private Country opposingCountry;
    private Set<Country> declaringAllies; //Which accepted to enter war
    private Set<Country> opposingAllies;
    private List<Battle> activeBattles;

    //if reference to Country inside military;
    //private Military declaringCountry;
    //private Military opposingCountry;
    //private Set<Military> declaringAllies;
    //private Set<Military> opposingAllies;
    private String warResult; //...

    public War(Country declaringCountry, Country opposingCountry, CasusBelli casusBelli) {
        this.declaringCountry = declaringCountry;
        this.opposingCountry = opposingCountry;
        this.casusBelli = casusBelli;
        this.activeBattles = new LinkedList<>();

    }
    public void bringAlly(){

    }
    public float getWarState(Country c){
        return (c == declaringCountry || declaringAllies.contains(c)) ? warState : -warState;
    }
    public void startBattle(int provId, MilDiv a, MilDiv o){

    }

    public void negotiate(){

    }
    //or int... arg
    public void finishWar(int... arg) {
        if (arg.length == 0) {
            // withdrawal/draw
        } else {

        }
    }

//Not used
    public static ListView<CasusBelli> makeListViewCasusBelli(Country c1, Country c2) {
        ListView<CasusBelli> lv = new ListView<>();
        for (CasusBelli cb : CasusBelli.values()) {
            if (cb.isValid(c1, c2)) {
                lv.getItems().add(cb);
            }
        }
        return lv;
    }

    public static <T extends Enum<T> & CValidatable> ListView<T> makeListViewValidatable(Country c1, Country c2, Class<T> enumClass) {
        ListView<T> lv = new ListView<>();
        for (T it : enumClass.getEnumConstants()) {
            if (it.isValid(c1, c2)) {
                lv.getItems().add(it);
            }
        }
        return lv;
    }

    @Override
    public String toString(){
        return declaringCountry.getName() + " vs " + opposingCountry.getName() + " - " + casusBelli.toString();
    }

}
