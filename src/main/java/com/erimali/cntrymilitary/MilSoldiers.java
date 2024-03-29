package com.erimali.cntrymilitary;

public class MilSoldiers extends MilUnit {

    public MilSoldiers(MilUnitData data, int maxSize) {
        super(data, maxSize);
    }

    public int recruitOld(int recruitSize) {
        if (this.lvl > 1) {
            //!!!!!!!!!!!!!!!!!
            return recruitSize;
            //substract xp until 0 ?!? as counterbalance?!?
            // if taking twice for lvl 2 while recruit
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

    public int recruit(int amount) {
        size += amount / lvl;
        if (size > maxSize) {
            int extraManpower = size - maxSize;
            size = maxSize;
            return extraManpower;
        }
        return amount % lvl;
    }

    public void train(int value) {
        this.xp += value;
        //protected int lvlCap;
        int lvlCap = this.lvl * 100;
        if (this.xp > lvlCap) {
            this.lvl++;
            this.xp -= lvlCap;
        }
    }

}
