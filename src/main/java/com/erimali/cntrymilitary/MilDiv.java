package com.erimali.cntrymilitary;

import com.erimali.cntrygame.AdmDiv;

import java.io.Serializable;
import java.util.*;

//move stuff related to country in military class
public class MilDiv implements Serializable {
    private Military military; //if null -> rebels
    private String name;
    private MilLeader leader;
    private List<MilUnit> units;
    private int speed; //Math.min of all milunits?

    //private int provId; if tied to province
    //

    public MilDiv(String name) {
        this.name = name;
        this.units = new ArrayList<>();
    }

    public MilDiv(String name, List<MilUnit> units) {
        this.name = name;
        this.units = units;
    }

    public MilDiv(String name, MilLeader leader) {
        this.name = name;
        this.leader = leader;
        this.units = new ArrayList<>();
    }


    public MilDiv(Military military, String name, MilLeader leader) {
        this.military = military;
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
            u.setBonuses(leader.getAtkBonus(), leader.getDefBonus());

        units.add(u);
    }

    public MilUnit removeUnit(int i) {
        if (i >= 0 && i < units.size()) {
            MilUnit u = units.remove(i);
            u.resetBonuses();
            return u;
        } else
            return null;
    }

    public boolean removeUnit(MilUnit u) {
        u.resetBonuses();
        return units.remove(u);
    }
    public double getLeaderAtkBonus(){
        return leader.getAtkBonus();
    }
    public double getLeaderDefBonus(){
        return leader.getDefBonus();
    }
    //double m1 = calcMorale(u);
    //double m2 = calcMorale(o);
    public static int attack(List<MilUnit> units, List<MilUnit> o) {
        if(units.isEmpty() || o.isEmpty())
            throw new IllegalArgumentException("Empty!");

        //How to take care of retreating!
        int n = Math.max(units.size(), o.size());
        int i = 0;
        int a1 = 0;
        int a2 = 0;
        int score = 0;
        int res = 0;
        int retreat1 = 0;
        int retreat2 = 0;
        while (i < n) {
            MilUnit u1 = units.get(a1);
            MilUnit u2 = o.get(a2);
            if (u1.isRetreating() && u2.isRetreating()) {
                a1++;
                a2++;
                a1 %= units.size();
                a2 %= o.size();
            } else if (u1.isRetreating()) {
                a1++;
                a1 %= units.size();
            } else if (u2.isRetreating()) {
                a2++;
                a2 %= o.size();
            } else {
                res = u1.attack(u2);
                if (res == -2) {
                    score -= 2;
                    units.remove(a1).removeSelf(); //remove self assumes MilDiv.list != this list
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
                    o.remove(a2).removeSelf();
                    if (o.isEmpty())
                        return Integer.MAX_VALUE;
                }
                a1++;
                a2++;
                a1 %= units.size();
                a2 %= o.size();
            }
            i++;
        }
        return score;
    }
    //
    public static boolean attack(List<MilUnit> units, AdmDiv a){
        int n = units.size();
        int i = 0;
        while(i < n && !a.isOccupied()){
            switch(units.get(i).attack(a)){
                case 2:
                    return true;
                case 0:
                    break;
                case -2:
                    units.remove(i).removeSelf();
                    break;
            }
        }
        return false;
    }
    public int attack(MilDiv o) {
        return attack(units, o.units);
    }

    public static double calcMorale(List<MilUnit> l) {
        int n = l.size();
        double sum = 0;
        for (MilUnit u : l) {
            sum += u.morale;
        }
        return sum / n;
    }


    @Override
    public String toString() {
        return name + ((leader != null) ? (" " + leader) : "");
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
    //////////////////////////////////////////////////////////
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
    //////////////////////////////////////////////////////////////

    public Military getMilitary() {
        return military;
    }

    public void setMilitary(Military military) {
        this.military = military;
    }

}
