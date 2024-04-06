package com.erimali.cntrymilitary;

import com.erimali.cntrygame.TESTING;

import java.io.Serializable;

public abstract class MilUnit implements Serializable {
    protected transient MilUnitData data;
    protected int dataId;
    protected int ownerId; //rebellious units -> change ownerId
    //even provId (?) for movement
    private static int CURR_ID = 0;
    protected final int id;
    //////////////////////////////////////////////////////////////////////
    protected float morale;
    //protected int maxHealth=hp*maxSize;
    //protected double totalHealth;
    //by this logic there's a direct interdependency between hp and size
    //
    protected int size;
    protected int xp;
    protected int lvl;

    public MilUnit(MilUnitData data, int ownerId) {
        this.data = data;
        this.ownerId = ownerId;
        this.dataId = data.getDataId();
        this.id = CURR_ID++;
        this.morale = 100;
        this.lvl = 1;
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

    public static double dmgCalcOld(MilUnit a, MilUnit o) {
        double mATK = a.data.atk[o.data.type] * a.size * Math.sqrt(a.lvl + a.data.speed) * (0.5 + a.morale / 100) + a.xp;
        mATK += mATK * Math.random();
        double oDEF = o.data.def[a.data.type] * o.size * Math.sqrt(o.lvl + o.data.speed) * (0.5 + o.morale / 100) + o.xp;
        return mATK - oDEF;
    }

    public static double dmgCalc(MilUnit a, MilUnit o) {
        double ATK = a.size * ((double) (a.data.atk[o.data.type] / o.data.def[a.data.type]))
                * Math.sqrt((double) (a.lvl * (a.data.minMilTech + 1)) / (o.lvl * (o.data.minMilTech + 1)) + (a.data.speed - o.data.speed));
        //* Math.sqrt(1 + a.morale / o.morale);
        ATK += ATK * Math.random() / 2;
        TESTING.print(a.id + ") ATK = " + ATK);

        return ATK;
    }

    public void attackAll(MilUnit... opponents) {
        for (MilUnit o : opponents)
            this.attack(o);
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
    public int recruitBuild(){
        return recruitBuild(data.maxSize/10);
    }
    @Override
    public String toString(){
        return data.name;
    }

    public int getType(){
        return data.type;
    }
}
