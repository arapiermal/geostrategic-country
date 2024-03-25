package com.erimali.cntrymilitary;

import java.util.List;

public class MilVehicle extends MilUnit {
    //Logically not the basic personnel required for the vehicle, but transporting soldiers/marines... (?)
    protected List<MilPersonnel> carryingPersonnel;
    protected List<MilVehicle> carryingVehicles; //prevent carry on carry on carry on carry...
    public MilVehicle(MilUnitData data, int maxSize){
        super(data, maxSize);
    }
    //Using factories of provinces (?)
    public int build(int buildSize) {
        //
        return 0;
    }

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
