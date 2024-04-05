package com.erimali.cntrymilitary;

import com.erimali.cntrygame.ErrorLog;
import com.erimali.cntrygame.TESTING;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//move stuff related to country in military class
public class MilDiv implements MilAttack, Serializable {
    protected static List<MilUnitData>[] unitTypes;
    protected static String DIR_UNIT_TYPES = "src/main/resources/data/units";

    //call after loading save game
    public static void loadAllUnitData(List<MilUnitData>[] unitTypes) {
        if(unitTypes == null) {
            //noinspection unchecked
            unitTypes = (List<MilUnitData>[]) new ArrayList[MilUnitData.MAX_TYPES];
        }
        for (int i = 0; i < MilUnitData.MAX_TYPES; i++) {
            unitTypes[i] = new ArrayList<>();

        }
        Path dir = Paths.get(DIR_UNIT_TYPES);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                try {
                    MilUnitData d = new MilUnitData(entry.toString());
                    unitTypes[d.type].add(d);
                } catch (Exception e) {
                    ErrorLog.logError(e);
                }
            }
            for (int i = 0; i < MilUnitData.MAX_TYPES; i++) {
                Collections.sort(unitTypes[i]);
            }
        } catch (IOException e) {
            ErrorLog.logError(e);
        }
    }

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

    //Use in military for GUI
    public static List<MilUnitData> getUnitTypesList(int type) {
        return unitTypes[type];
    }

    //TreeTableView here(?)

    //////////////////////////////////////////////////////////////
    protected String name;
    protected MilLeader leader;
    protected List<MilUnit> units;
    protected int speed; //Math.min of all milunits?

    public MilDiv(String name) {
        this.name = name;
        this.units = new LinkedList<>();
    }

    public MilDiv(String name, MilLeader leader) {
        this.name = name;
        this.leader = leader;
        this.units = new LinkedList<>();
    }

    public void addMilUnit(MilUnit... milUnits) {
        Collections.addAll(units, milUnits);
    }

    public MilUnit removeMilUnit(int i) {
        if (i >= 0 && i < units.size())
            return units.remove(i);
        else
            return null;
    }

    public boolean removeMilUnit(MilUnit i) {
        return units.remove(i);
    }

    public int attack(MilDiv o) {
        //take care when units is empty (?)
        int n = Math.max(units.size(), o.units.size());
        int i = 0;
        int a1 = 0, a2 = 0;
        int res = 0;
        while (i < n && res == 0) {
            res = units.get(a1).attack(o.units.get(a2));
            if (res == -2) {
                units.remove(a1);
                //(?)
                if (units.isEmpty())
                    return res;
            } else if (res == 2) {
                o.units.remove(a2);
                if (o.units.isEmpty())
                    return res;
            }
            a1++;
            a2++;
            a1 %= units.size();
            a2 %= o.units.size();
            i++;
        }
        return res;
    }

    //Potential bullying (!)
    public int attack(MilUnit o) {
        int n = units.size();
        int i = 0;
        int a1 = 0;
        int res = 0;
        while (i < n && res == 0) {
            res = units.get(a1).attack(o);
            if (res == -2) {
                units.remove(a1);
            } else if (res == 2) {
                return res;
            }
            a1++;
            a1 %= units.size();
            i++;
        }
        return res;
    }

    @Override
    public int attack(MilAttack o) {
        return o instanceof MilDiv ? attack((MilDiv) o) : attack((MilUnit) o);
    }


    @Override
    public String toString() {
        return this.name;
    }

    public static MilUnit makeUnit(int type, int index) {
        MilUnitData data = unitTypes[type].get(index);
        MilUnit unit = (type % 2 == 0) ? new MilSoldiers(data) : new MilVehicles(data);
        return unit;
    }

    public static void main(String[] args) {
        loadAllUnitData(MilDiv.unitTypes);

        MilUnit u = makeUnit(0, 0);
        MilUnit o = makeUnit(0, 0);
        u.incSize(1000);
        o.incSize(500);
        //u.incLevel(1);
        o.incLevel(2);
        int res;
        while ((res = u.attack(o)) == 0) {
            TESTING.print(u.size + " " + u.morale, o.size + " " + o.morale);
        }
        TESTING.print(res > 0 ? "WIN" : "LOST");
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

}
