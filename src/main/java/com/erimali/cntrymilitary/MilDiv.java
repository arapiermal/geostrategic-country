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
    protected List<MilUnit> units;

    public MilDiv(String name) {
        this.name = name;
        this.units = new LinkedList<>();
    }

    public void attack() {

    }

    public void makeUnit(int type, int index, int maxSize) {
        MilUnitData data = unitTypes[type].get(index);
        MilUnit unit = (type % 2 == 0) ? new MilSoldiers(data, maxSize) : new MilVehicle(data, maxSize);
        units.add(unit);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public static void main(String[] args) {
        loadAllUnitData();
        TESTING.print(unitTypes[0], unitTypes[2]);
        MilDiv div = new MilDiv("Strong");
        div.makeUnit(0, 0, 1000);

        MilUnit u = div.units.get(0);
        TESTING.print(u.size);

    }
}
