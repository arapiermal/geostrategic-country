package com.erimali.cntrymilitary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MilVehicles extends MilUnit {
    protected MilSoldiers personnel;//can be redundant
    protected List<MilSoldiers> carryingPersonnel;
    protected List<MilVehicles> carryingVehicles; //prevent carry on carry on carry on carry...

    public MilVehicles(MilUnitData data, int ownerId) {
        super(data, ownerId);
        if (data.canCarryPersonnel()) {
            carryingPersonnel = new LinkedList<>();
        } else if (data.canCarryVehicles()) {
            carryingVehicles = new ArrayList<>();
        }
    }

    public MilVehicles(MilUnitData data, int ownerId, MilSoldiers personnel) {
        super(data, ownerId);
        this.personnel = personnel;
        if (data.canCarryPersonnel()) {
            carryingPersonnel = new LinkedList<>();
        } else if (data.canCarryVehicles()) {
            carryingVehicles = new ArrayList<>();
        }
    }

    //Using factories of provinces (?) RESOURCES
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

    public boolean carry(MilUnit u) {
        if (data.canCarry(u.getType())) {
            if (u instanceof MilSoldiers)
                carryingPersonnel.add((MilSoldiers) u);
            else if (u instanceof MilVehicles)
                carryingVehicles.add((MilVehicles) u);
            return true;
        }
        return false;
    }

}
