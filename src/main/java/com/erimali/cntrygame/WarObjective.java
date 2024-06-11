package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.List;

public enum WarObjective {
    //if all provinces selected -> full annexation
    //There can be partly annexation! cost cannot be a stuck value, what can be better
    ANNEX(100) {

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
            game.subjugateCountry(cInd1,cInd2, args);
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
            short improveRel;
            for (int i : released) {
                improveRel = (short) 100;

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
            c2.setMilitary(null); //erases all progress !!!!!!...
            int years = 5;
            if(args.length > 0)
             years = args[0];
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


    //negotiation one on one (?)...
    //or one on many... (occupied provinces shared in war)
    public abstract void action(GLogic game, War war, int cInd1, int cInd2, int... args);

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
