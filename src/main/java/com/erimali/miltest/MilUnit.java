package com.erimali.miltest;

public abstract class MilUnit {
    protected MilUnitData data;

    //////////////////////////////////////////////////////////////////////

    protected float morale;
    //protected int maxHealth=hp*maxSize;
    //protected double totalHealth;
    //by this logic there's a direct interdependency between hp and size
    //
    protected int maxSize;
    protected int size;
    protected int xp;
    protected int lvl;

    public void attackUnit(MilUnit o, boolean attacking) {
        double mATK = data.atk[o.data.type] * size * Math.sqrt(this.lvl) + this.data.speed * this.morale + this.xp;
        double oDEF = o.data.def[data.type] * size * Math.sqrt(o.lvl) + o.data.speed * o.morale + o.xp;
        double diff1 = mATK - oDEF;


        if (o.stillStanding())
            o.attackUnit(this, false);
    }

    public void attackUnits(MilUnit... opponent) {
        for (MilUnit o : opponent)
            this.attackUnit(o, true);
    }

    public boolean stillStanding() {
        //if size > 0 BUT morale <= 0, army can surrender or retreat (if it can do the later)
        return this.size > 0 && this.morale > 0;
    }

}
