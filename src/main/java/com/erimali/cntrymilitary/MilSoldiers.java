package com.erimali.cntrymilitary;

public class MilSoldiers extends MilUnit {

    public MilSoldiers(MilUnitData data, int maxSize) {
        super(data, maxSize);
    }

    public int recruit(int recruitSize) {
        if (this.lvl > 1) {
            //!!!!!!!!!!!!!!!!!
            return recruitSize;
            //substract xp until 0 ?!? as counterbalance?!?
            //or take longer to train
            //lvl 2 divide by 2?!?
        }
        this.size += recruitSize;

        //take care of max Size
        if (this.size > this.maxSize) {
            int extraManpower = this.size - this.maxSize;
            this.size = this.maxSize;
            return extraManpower;
        }
        return 0;
    }

    public void train(int value) {
        //only personnel should be trainable (?)
        this.xp += value;
        //protected int lvlCap;
        int lvlCap = this.lvl * 100;
        if (this.xp > lvlCap) {
            this.lvl++;
            this.xp -= lvlCap;
        }
    }

    public void attackUnit(MilSoldiers o, boolean attacking) {
        double mATK = data.atk[o.data.type] * size * Math.sqrt(this.lvl) + data.speed * this.morale + this.xp;
        double oDEF = o.data.def[data.type] * size * Math.sqrt(o.lvl) + o.data.speed * o.morale + o.xp;
        double diff1 = mATK - oDEF;


        if (o.stillStanding())
            o.attackUnit(this, false);
    }

    public void attackUnits(MilSoldiers... opponent) {
        for (MilSoldiers o : opponent)
            this.attackUnit(o, true);
    }

}
