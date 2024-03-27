package com.erimali.cntrymilitary;

import com.erimali.cntrygame.ErrorLog;
import com.erimali.cntrygame.GLogic;
import com.erimali.cntrygame.TESTING;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
//move stuff related to country in military class
public class MilDiv {
    protected static List<MilUnitData>[] unitTypes;
    protected static String DIR_UNIT_TYPES = "src/main/resources/data/units";

    public static void loadAllUnitData() {
        unitTypes = (List<MilUnitData>[]) new ArrayList[MilUnitData.MAX_TYPES];
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

    public static List<MilUnitData> getUnitTypesList(int type) {
        return unitTypes[type];
    }

    protected String name;
    protected MilLeader leader;
    protected List<MilUnit> units;

    public MilDiv(String name) {
        this.name = name;
        this.units = new LinkedList<>();
    }

    public void attack() {

    }

    public static MilUnit makeUnit(int type, int index, int maxSize) {
        MilUnitData data = unitTypes[type].get(index);
        MilUnit unit = (type % 2 == 0) ? new MilSoldiers(data, maxSize) : new MilVehicle(data, maxSize);
        return unit;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static void main(String[] args) {
        loadAllUnitData();

        MilUnit u = makeUnit(0, 0, 2000);
        MilUnit o = makeUnit(0, 0, 1000);
        u.incSize(1400); // DEPENDENT UPON RATION atk:def , 2:1 ratio -> near certain loss
        //UNPENETRATABLE DEFENSE !, maybe make defense to divide the attack and health somewhat different ?
        o.incSize(700);
        TESTING.print(u.attack(o));

        TESTING.print(u.size + " " + u.morale,o.size + " " + o.morale);
        TESTING.print(u.attack(o));

        TESTING.print(u.size + " " + u.morale,o.size + " " + o.morale);
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
