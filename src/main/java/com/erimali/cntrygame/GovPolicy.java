package com.erimali.cntrygame;

public enum GovPolicy {
    NEUTRALITY("Neutrality", "Take a neutral stance in global conflicts", 0, 1),
    MULTI_LINGUALISM("Embrace Linguistic Diversity", "Our country is united regardless diversity of languages, others cannot sponsor linguistic rebels on our territory or declare wars based on such minorities.", 16, 3.5),
    FORCED_NEUTRALITY("Forced Neutrality", "", -1),
    DEMILITARIZED("Demilitarized", "", -8){
        //negative boost in manpower and recruiting speed (?)
    },
    BANNED_MILITARY("Banned Military", "", -16),
    //based on pop of country?
    INCENTIVIZE_BIRTHS("Incentivize Births", "Every year, the population growth (or lack thereof) gets higher.", 4, 10, 4) {
        @Override
        public void tick(Country c) {
            c.incPopulationIncrease(0.01);
        }
    },
    INCENTIVIZE_HEALTHINESS("Incentivize Healthiness", "An apple a day, keeps the doctor away.", 4, 3),

    INCENTIVIZE_RESEARCH("Incentivize Research", "Gain a boost in monthly research", 4, 1.5),

    NUCLEAR_PROGRAM("Nuclear Program", "", 256, 5, 3) {
        @Override
        public void tick(Country c) {
            //nuclearTech
            //nuclearTech lvl 3 + enriched uranium =>
        }
    },

    ;


    public void tick(Country c) {}

    //Positive
    //Negative/imposed policies
    //value for AI to know whether it is a positive or negative policy
    private final String desc;
    private String info;
    private final int val;
    private double price;
    private int periodTick;

    GovPolicy(String desc, String info, int val) {
        this.desc = desc;
        this.info = info;
        this.val = val;
    }

    GovPolicy(String desc, String info, int val, double price) {
        this.desc = desc;
        this.info = info;
        this.val = val;
        this.price = price;
    }

    GovPolicy(String desc, String info, int val, double price, int periodTick) {
        this.desc = desc;
        this.info = info;
        this.val = val;
        this.price = price;
        this.periodTick = periodTick;
    }

    public double getPrice() {
        return price;
    }

    public boolean isRemovable() {
        return val >= 0;
    }

    @Override
    public String toString() {
        return desc;
    }

    public boolean hasMonthlyTick() {
        return periodTick == 3;
    }

    public boolean hasYearlyTick() {
        return periodTick == 4;
    }

    public String getInfo() {
        return info;
    }
}
