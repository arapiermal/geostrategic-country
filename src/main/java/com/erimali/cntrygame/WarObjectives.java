package com.erimali.cntrygame;

public enum WarObjectives {
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
    },
    //SHARE_ECONOMIC_PROSPERITY(50){},
    ;
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
