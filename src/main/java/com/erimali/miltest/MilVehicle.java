package com.erimali.miltest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class MilVehicle extends MilUnit {
    //Logically not the basic personnel required for the vehicle, but transporting soldiers/marines... (?)
    protected List<MilPersonnel> carryingPersonnel;
    protected List<MilVehicle> carryingVehicles; //prevent carry on carry on carry on carry...

    //Using factories of provinces (?)
    public int build(int buildSize) {
        //
        return 0;
    }

    public void upgrade(int value) {
        //only personnel should be trainable (?)
        this.xp += value;
        //protected int lvlCap;
        int lvlCap = this.lvl * 100;
        if (this.xp > lvlCap) {
            this.lvl++;
            this.xp -= lvlCap;
        }
    }


}
