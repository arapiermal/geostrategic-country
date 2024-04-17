package com.erimali.cntrymilitary;

import com.erimali.cntrygame.ErrorLog;
import com.erimali.cntrygame.TESTING;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

//move stuff related to country in military class
public class MilDiv implements Serializable {


    public void correlateUnitData(List<MilUnitData>[] unitTypes) {
        int n = MilUnitData.MAX_TYPES;
        for (MilUnit u : units) {
            try {
                u.data = unitTypes[u.dataId / n].get(u.dataId % n);
            } catch (Exception e) {
                units.remove(u);
            }
        }
    }

    //TreeTableView here(?)

    //////////////////////////////////////////////////////////////
    protected String name;
    protected MilLeader leader;
    protected List<MilUnit> units;
    protected int speed; //Math.min of all milunits?

    public MilDiv(String name) {
        this.name = name;
        this.units = new ArrayList<>();
    }

    public MilDiv(String name, MilLeader leader) {
        this.name = name;
        this.leader = leader;
        this.units = new ArrayList<>();
    }

    public MilUnit getUnit(int i) {
        if (i >= 0 && i < units.size()) {
            return units.get(i);
        } else {
            return null;
        }
    }

    public List<MilUnit> getUnits() {
        return units;
    }

    public void addUnit(MilUnit u) {
        if (leader != null)
            u.setBonuses(leader.atkBonus(), leader.defBonus());
        units.add(u);
    }

    public MilUnit removeMilUnit(int i) {
        if (i >= 0 && i < units.size()) {
            MilUnit u = units.remove(i);
            u.resetBonuses();
            return u;
        } else
            return null;
    }

    public boolean removeMilUnit(MilUnit i) {
        return units.remove(i);
    }

    //double m1 = calcMorale(u);
    //double m2 = calcMorale(o);
    public static int attack(List<MilUnit> u, List<MilUnit> o) {
        int n = Math.max(u.size(), o.size());
        int i = 0;
        int a1 = 0, a2 = 0;
        int res = 0;
        while (i < n) {
            res = u.get(a1).attack(o.get(a2));
            if (res == -2) {
                u.remove(a1);
                //(?)
                if (u.isEmpty())
                    return res;
            } else if (res == 2) {
                o.remove(a2);
                if (o.isEmpty())
                    return res;
            }
            a1++;
            a2++;
            a1 %= u.size();
            a2 %= o.size();
            i++;
        }
        return res;
    }

    public static double calcMorale(List<MilUnit> l) {
        int n = l.size();
        double sum = 0;
        for (MilUnit u : l) {
            sum += u.morale;
        }
        return sum / n;
    }

    public static int attackOld(List<MilUnit> u, List<MilUnit> o) {
        int n = Math.max(u.size(), o.size());
        int i = 0;
        int a1 = 0, a2 = 0;
        int res = 0;
        while (i < n && res == 0) {
            res = u.get(a1).attack(o.get(a2));
            if (res == -2) {
                u.remove(a1);
                //(?)
                if (u.isEmpty())
                    return res;
            } else if (res == 2) {
                o.remove(a2);
                if (o.isEmpty())
                    return res;
            }
            a1++;
            a2++;
            a1 %= u.size();
            a2 %= o.size();
            i++;
        }
        return res;
    }

    public int attack(MilDiv o) {
        if(this.units.isEmpty() || o.units.isEmpty())
            throw new IllegalArgumentException("Empty division!");
        //take care when units is empty (?)
        int n = Math.max(units.size(), o.units.size());
        int i = 0;
        int a1 = 0;
        int a2 = 0;
        int score = 0;
        int res = 0;
        int retreat1 = 0;
        int retreat2 = 0;
        while (i < n) {
            MilUnit u1 = units.get(a1);
            MilUnit u2 = o.units.get(a2);
            if (u1.isRetreating() && u2.isRetreating()) {
                a1++;
                a2++;
                a1 %= units.size();
                a2 %= o.units.size();
            } else if (u1.isRetreating()) {
                a1++;
                a1 %= units.size();
            } else if (u2.isRetreating()) {
                a2++;
                a2 %= o.units.size();
            } else {
                res = u1.attack(u2);
                if (res == -2) {
                    score -= 2;
                    units.remove(a1);
                    if (units.isEmpty())
                        return Integer.MIN_VALUE;
                } else if (res == -1) {
                    score--;
                    retreat1++;
                    u1.setRetreating(true);
                } else if (res == 1) {
                    score++;
                    retreat2++;
                    u2.setRetreating(true);
                } else if (res == 2) {
                    score += 2;
                    o.units.remove(a2);
                    if (o.units.isEmpty())
                        return Integer.MAX_VALUE;
                }
                a1++;
                a2++;
                a1 %= units.size();
                a2 %= o.units.size();
            }
            i++;
        }
        return score;
    }


    @Override
    public String toString() {
        return name + ((leader != null) ? leader.toString() : "");
    }

    public String toStringUnits() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (MilUnit u : units) {
            sb.append(i++).append(')').append(u.toString()).append('\n');
        }
        return sb.toString();
    }

    public boolean hasLeader() {
        return leader != null;
    }

    public MilLeader getLeader() {
        return leader;
    }

    public void setLeader(MilLeader leader) {
        this.leader = leader;
    }
    /*
     * 0 0 0 0 0 0
     * 0 0 0 0 0 0
     * vs
     * 0 0 0 0 0 0
     * 0 0 0
     * make combinations 0 <-> 0
     * */

    public void train(int amount) {
        for (MilUnit u : units) {
            if (u instanceof MilSoldiers) {
                ((MilSoldiers) u).train(amount);
            }
        }
    }

    public int getTotalSize() {
        int s = 0;
        for (MilUnit u : units)
            s += u.size;
        return s;
    }

    public void stopAllRetreating(){
        for(MilUnit u : units)
            u.setRetreating(false);
    }

    public void maximizeSizeAllUnits(){
        for(MilUnit u : units)
            u.maximizeSize();
    }
}
