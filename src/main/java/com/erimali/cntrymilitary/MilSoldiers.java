package com.erimali.cntrymilitary;

public class MilSoldiers extends MilUnit {

    public MilSoldiers(MilUnitData data, int ownerId) {
        super(data,ownerId);
    }

    public int recruit(int amount) {
        size += amount / lvl;
        if (size > data.maxSize) {
            int extraManpower = size - data.maxSize;
            size = data.maxSize;
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
