package com.erimali.cntrymilitary;

import com.erimali.cntrygame.CountryArray;
import com.erimali.cntrygame.RebelType;

public class MilRebels extends MilSoldiers {
    private static final MilUnitData REBEL_SOLDIERS = new MilUnitData(0, -1, "Rebels", "People rebelling militarily",
            1, new int[]{1, 1, 1, 1, 1, 1, 1, 1}, new int[]{1, 1, 1, 1, 1, 1, 1, 1}, 5, 900, 0, null);
    private RebelType rebelType;

    //not transient here, removes need for correlation
    protected MilUnitData data;

    //ownerId = the sponsor ? or -1 for not affiliated with any state
    public MilRebels(int ownerId, RebelType rebelType, boolean full) {
        super(REBEL_SOLDIERS, CountryArray.getMaxIso2Countries() + ownerId);
        this.rebelType = rebelType;
        if (full)
            maximizeSize();
    }

    public MilRebels(int ownerId, RebelType rebelType) {
        super(REBEL_SOLDIERS, CountryArray.getMaxIso2Countries() + ownerId);
        this.rebelType = rebelType;
    }

    public int getSponsorId() {
        return ownerId - CountryArray.getMaxIso2Countries();
    }

    public void setRebelType(RebelType rebelType) {
        this.rebelType = rebelType;
    }

    public RebelType getRebelType() {
        return rebelType;
    }

    public static MilUnitData getRebelSoldiersData() {
        return REBEL_SOLDIERS;
    }

}
