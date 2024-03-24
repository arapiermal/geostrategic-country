package com.erimali.cntrygame;

import javafx.scene.control.ListView;


import java.util.Set;

enum WarObjectives {
    //There can be partly annexation! cost cannot be a stuck value, what can be better
    ANNEX(95) {
        //flip flop warState, if negative but the opponent is player, make positive?
        public void action(Country c1, Country c2, float warState, int... args) {
            //factors that effect?
            if (warState > this.getCost()) {
                //c1.annexCountry(c2);
            }
        }
    },
    //types???
    SUBJUGATE(85) {
        @Override
        public void action(Country c1, Country c2, float warState, int... args) {
            //c1.subjugateCountry(c2);
        }
    },
    REGIMECHANGE(70) {
        @Override
        public void action(Country c1, Country c2, float warState, int... args) {
            //make same regime as self
            if (args[0] == 0) {
                c2.getGovernment().setType(c1.getGovernment().getType());
            }
        }
    },
    //handle better
    DISMANTLEMILITARY(85) {
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


public class War {
    // each country in war having warState?
    // if warState in disfavor, AI likely to accept terms
    private CasusBelli casusBelli; //enum? array? //loadable casus bellis?
    //private String[] warGoals;?? dependent on casusBelli
    private float warState; // from -100 to 100
    private Country declaringCountry;//what if Military, and in constructor get it
    private Country opposingCountry;
    private Set<Country> declaringAllies; //Which accepted to enter war
    private Set<Country> opposingAllies;
    //private Military declaringCountry;
    //private Military opposingCountry;
    //private Set<Military> declaringAllies;
    //private Set<Military> opposingAllies;
    private String warResult; //...

    public War(Country declaringCountry, Country opposingCountry, CasusBelli casusBelli) {
        this.declaringCountry = declaringCountry;//.getMilitary();
        this.opposingCountry = opposingCountry;//.getMilitary();
        this.casusBelli = casusBelli;
    }

    //Unify militaries
    //!!!!!!!!!!! CLASH WITH MILITARY
    public float executeBattle(Military declaringMil, Military opposingMil) {
        float battleResult = 0;

        return battleResult;
    }

    //or int... arg
    public void finishWar(String... arg) {
        if (arg.length == 0) {
            // withdrawal/draw
        } else {

        }
    }


    public static ListView<CasusBelli> makeListViewCasusBelli(Country c1, Country c2) {
        ListView<CasusBelli> lv = new ListView<>();
        for (CasusBelli cb : CasusBelli.values()) {
            if (cb.isValid(c1, c2)) {
                lv.getItems().add(cb);

            }
        }
        return lv;
    }

}
