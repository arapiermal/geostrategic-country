package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.List;

public enum WarObjective {
    //if all provinces selected -> full annexation
    //There can be partly annexation! cost cannot be a stuck value, what can be better
    ANNEX(100) {
        //flip flop warState, if negative but the opponent is player, make positive?
        //2 cArr if different worlds (?!?!?)
        //or should there be reference to world in each country ????

        //link glogic here? and getCountry of glogic calculate...
        public void action(GLogic game, War war, int cInd1, int cInd2, int... args) {
            //factors that effect?
            Country c1 = game.getCountry(cInd1);
            double warState = war.getWarState(c1);
            //Country c2 = game.getCountry(cInd2);
            CountryArray cArr2 = game.getWorldCountries();
            if (warState > this.getCost()) {
                c1.annexCountry(cArr2, cInd2, false);
            }
        }

    },
    //types???
    SUBJUGATE(80) {
        @Override
        public void action(GLogic game, War war, int cInd1, int cInd2, int... args) {
            //c1.subjugateCountry(c2);

        }
    },
    LIBERATE_ANNEXED(100){
        //taking care of partial annexation...
        public void action(GLogic game, War war, int cInd1, int cInd2, int... args) {
            Country c1 = game.getCountry(cInd1);
            World world = game.getWorld();
            if(!world.getInitialProvinces().ownsOthers(c1)){
                return;
            }
            List<Integer> released = world.releaseAllCountries(cInd2, true);

            for (int i : released) {
                short improveRel = (short) 100;

                c1.improveRelations(i, improveRel);
            }

        }
    },
    LIBERATE_SUBJECTS(75) {
        public void action(GLogic game, War war, int cInd1, int cInd2, int... args) {
            Country c1 = game.getCountry(cInd1);
            Country c2 = game.getCountry(cInd2);
            for (int i : c2.getSubjects().keySet()) {
                CSubject subject = c2.getSubject(i);
                short improveRel;
                if(subject.isAtGoodTerms()){
                    improveRel =  (short) (c2.getRelations(i) / 8);//-
                } else{
                    improveRel = (short) (subject.getIndependenceDesire() * 2);
                }
                c1.improveRelations(i, improveRel);
            }
            c2.liberateAllSubjects(true);

        }
    },
    REGIME_CHANGE(70) {
        @Override
        public void action(GLogic game, War war, int cInd1, int cInd2, int... args) {
            //make same regime as self
            Country c1 = game.getCountry(cInd1);
            Country c2 = game.getCountry(cInd2);
            if (args[0] == 0) {
                c2.getGovernment().setType(c1.getGovernment().getType());
            }
        }
    },
    //handle better
    DISMANTLE_MILITARY(75) {
        public void action(GLogic game, War war, int cInd1, int cInd2, int... args) {
            //Country c1 = game.getCountry(cInd1);
            Country c2 = game.getCountry(cInd2);
            c2.setMilitary(null);
            int years = args[0];
            c2.getGovernment().addPolicy(GovPolicy.BANNED_MILITARY, years);
        }
    },
    //SHARE_ECONOMIC_PROSPERITY(50){},
    ;
    private double cost;
    private EnumSet<WarObjective> exclude;
    WarObjective(double cost) {
        this.cost = cost;
    }


    //public abstract void action(Country c1, Country c2, double warState, int... args);
    //double warState
    //negotiation one on one (?)...
    //or one on many... (occupied provinces shared in war)
    public abstract void action(GLogic game, War war, int cInd1, int cInd2, int... args);

    public double getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }
}
