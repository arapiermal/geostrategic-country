package com.erimali.cntrymilitary;

import java.util.List;

public class MilVehicles extends MilUnit {
    protected MilPersonnel personnel;
    protected List<MilPersonnel> carryingPersonnel;
    protected List<MilVehicles> carryingVehicles; //prevent carry on carry on carry on carry...

    public MilVehicles(MilUnitData data, int maxSize) {
        super(data, maxSize);
    }

    //Using factories of provinces (?)
    public int build(int amount) {
        this.size += amount / lvl;
        return amount % lvl;
    } // WOULD MAKE IT IMPOSSIBLE IF AMOUNT < LVL


    //use xp as progress bar for the upgrade
    public void upgrade(int value) {
        //
        this.xp += value;
        //
        int lvlCap = this.lvl * 100;
        if (this.xp > lvlCap) {
            this.lvl++;
            this.xp -= lvlCap;
        }
    }


}
