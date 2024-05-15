package com.erimali.cntrymilitary;

import com.erimali.cntrygame.AdmDiv;
import com.erimali.cntrygame.CountryArray;
import com.erimali.cntrygame.RebelType;
import com.erimali.cntrygame.TESTING;

import java.io.Serializable;

public abstract class MilUnit implements Serializable {
    protected transient MilUnitData data;
    protected int dataId;
    protected int ownerId; //rebellious units -> change ownerId
    //provId (?) for movement
    private static int CURR_ID = 0;
    protected final int id;
    //////////////////////////////////////////////////////////////////////
    protected float morale;
    protected int size;
    protected int xp;
    protected int lvl;

    protected double bonusAtk;
    protected double bonusDef;

    private boolean retreating;

    public MilUnit(MilUnitData data, int ownerId) {
        this.data = data;
        this.ownerId = ownerId;
        this.dataId = data.getDataId();
        this.id = CURR_ID++;
        this.morale = 100;
        this.lvl = 1;
    }
    public static void main(String... args){
        MilRebels rebels = new MilRebels(CountryArray.getIndex("EL"), RebelType.INDEPENDENCE, true);
        AdmDiv el = new AdmDiv("Elbasan", 100.0, 300000,(short)0);
        while (rebels.attack(el) == 0) {
            TESTING.print("DEF = " + el.getDefense());
        }
        TESTING.print("SUCCESS: DEF = " + el.getDefense(), el.getOccupierId(), rebels.size);


    }
    public int attack(AdmDiv a) {
        double admDEF = 0.5 * a.getInfrastructure() + 0.01 * (a.getMaxDefense() + a.getDefense()); // 0.5 * 1 + 0.01 * (100 + 100) = 2.5
        double popScale = size / Math.sqrt(a.getPopulation());
        double ATK = popScale * ((data.atk[0] + bonusAtk) / admDEF) * Math.sqrt((double) (lvl * (data.minMilTech + 1)) + data.speed);
        if(ATK <= 0)
            ATK++;

        if(a.decDefense((float) ATK, ownerId))
            return 2;
        size -= (int) (size / admDEF);
        if(size<=0)
            return -2;
        return 0;
    }
    //return double ?
    public int attack(MilUnit o) {
        double dmg1 = dmgCalc(this, o);
        double dmg2 = dmgCalc(o, this);
        if (dmg1 > 0) {
            int prevSize = o.size;
            o.size -= (int) (dmg1 / (o.data.hp) + 1);
            o.morale -= (float) (prevSize - o.size) / prevSize * 100;

            if (o.size <= 0) {
                o.size = 0;
                return 2;
            } else if (o.morale <= 0) {
                o.morale = 0;
                return 1; //Opponent retreats
            }
        }
        if (dmg2 > 0) {
            int prevSize = this.size;
            this.size -= (int) (dmg2 / (this.data.hp)) + 1;
            this.morale -= (float) (prevSize - this.size) / prevSize * 100;
            if (this.size <= 0) {
                this.size = 0;
                return -2;
            } else if (this.morale <= 0) {
                this.morale = 0;
                return -1; //This unit retreats
            }
        }
        return 0;

    }

    public static double dmgCalc(MilUnit a, MilUnit o) {
        double ATK = a.size * ((a.data.atk[o.data.type] + a.bonusAtk) / (o.data.def[a.data.type] + a.bonusDef))
                * Math.sqrt((double) (a.lvl * (a.data.minMilTech + 1)) / (o.lvl * (o.data.minMilTech + 1)) + (a.data.speed - o.data.speed));
        ATK += ATK * Math.random() / 2;

        return ATK;
    }

    public boolean stillStanding() {
        //if size > 0 BUT morale <= 0, army can surrender or retreat (if it can do the later)
        return size > 0 && morale > 0;
    }

    public int incSize(int value) {
        this.size += value;
        if (size > data.maxSize) {
            int extra = size - data.maxSize;
            size = data.maxSize;
            return extra;
        }
        return 0;
    }

    public void incLevel(int i) {
        if (i > 0)
            this.lvl += i;
    }

    public int recruitBuild(int amount) {
        size += amount / lvl;
        if (size > data.maxSize) {
            int extra = size - data.maxSize;
            size = data.maxSize;
            return extra;
        }
        return amount % lvl;
    }

    public int recruitBuild() {
        return recruitBuild(data.maxSize / 10);
    }

    @Override
    public String toString() {
        return size + "x " + data.name + (lvl > 1 ? (" Lvl " + lvl) : "");
    }

    public int getType() {
        return data.type;
    }

    public void setBonuses(double bonusAtk, double bonusDef) {
        this.bonusAtk = bonusAtk;
        this.bonusDef = bonusDef;
    }

    public void resetBonuses() {
        this.bonusAtk = 0;
        this.bonusDef = 0;
    }

    public void maximizeSize() {
        this.size = data.maxSize;
    }

    public void setSize(int val) {
        if (val < 0)
            size = 0;
        else if (val > data.maxSize)
            size = data.maxSize;
        else
            size = val;
    }

    public MilUnitData getData() {
        return data;
    }

    public boolean isRetreating() {
        return retreating;
    }

    public void setRetreating(boolean retreating) {
        if (!retreating) {
            this.morale = 100;
        }
        this.retreating = retreating;
    }

}
