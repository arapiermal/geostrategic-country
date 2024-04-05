package com.erimali.cntrymilitary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MilUnitData implements Comparable<MilUnitData> {
    private static final String[] TYPES = {"soldiers", "ground-vehicles", "marines", "ships",
            "airborne-forces", "planes", "space-ships", "space-soldiers"};
    protected static final int MAX_TYPES = 8;
    private static final int MAX_SUBTYPES = 4096;

    protected final int type;

    protected String name;
    protected String desc;
    protected int subtype;
    protected int speed;
    //protected int range; //in a matrix [][] artillery at the back can shoot at the front

    protected int[] atk;
    protected int[] def;
    //make hp go down and up (?) maxHP
    protected int hp;
    //
    protected int maxSize;

    protected int minMilTech;

    private boolean[] canCarry; //problematic if small vehicle carry big one

    public MilUnitData(String loc) throws Exception {
        this.type = (byte) (loc.charAt(loc.length() - 1) - '0');
        if (type >= MAX_TYPES)
            throw new IllegalArgumentException("TYPE CAN BE FROM 0 UP TO " + (MAX_TYPES - 1));
        try (BufferedReader br = new BufferedReader(new FileReader(loc))) {
            String line;
            while ((line = br.readLine()) != null) {
                setValue(line.trim().split("\\s*:\\s*"));
            }
            minValues();
        }
    }

    public static int parseIntOrDef(String in, int def) {
        try {
            int val = Integer.parseInt(in);
            return val;
        } catch (NumberFormatException nfe) {
            return def;
        }
    }

    public static int parseIntAndLowest(String in, int min) {
        try {
            int val = Integer.parseInt(in);
            return Math.max(val, min);
        } catch (NumberFormatException nfe) {
            return min;
        }
    }

    private void setValue(String[] in) {
        if (in.length < 2)
            return;
        switch (in[0].toLowerCase()) {
            case "name":
                this.name = in[1];
                break;
            case "desc":
                this.desc = in[1];
                break;
            case "subtype":
                this.subtype = Integer.parseInt(in[1]);
                break;
            case "speed":
                this.speed = parseIntOrDef(in[1], 1); //speed based on environment?!?
                break;
            case "atk":
                this.atk = new int[in.length - 1];
                for (int i = 1; i < in.length; i++) {
                    this.atk[i - 1] = Math.max(1, parseIntOrDef(in[i], 1));
                }
                break;
            case "def":
                this.def = new int[in.length - 1];
                for (int i = 1; i < in.length; i++) {
                    this.def[i - 1] = Math.max(1, parseIntOrDef(in[i], 1));
                }
                break;
            case "hp":
                this.hp = parseIntOrDef(in[1], 1);
                break;
            case "size":
                this.maxSize = parseIntOrDef(in[1], 100);
                break;
            case "carry":
                if (isVehicle()) {
                    this.canCarry = new boolean[MAX_TYPES];
                    Set<Integer> temp = new HashSet<>();
                    for (int i = 1; i < in.length; i++) {
                        int val = parseIntOrDef(in[i], -1);
                        if (val > -1 && val < canCarry.length) {
                            temp.add(val);
                        }
                    }
                    for (int i = 0; i < canCarry.length; i++) {
                        canCarry[i] = temp.contains(i);
                    }
                }
                break;
            case "tech":
                this.minMilTech = parseIntOrDef(in[1], 0);
                break;
        }
    }

    private void minValues() {

        this.maxSize = Math.max(100, this.maxSize);
        this.speed = Math.max(1, this.speed);
        this.hp = Math.max(1, this.hp);
        this.minMilTech = Math.max(0, this.minMilTech);
    }

    public boolean isVehicle() {
        return type % 2 == 1;
    }

    @Override
    public int compareTo(MilUnitData o) {
        return Integer.compare(getDataId(), o.getDataId());
    }

    public int getDataId() {
        return type * MAX_SUBTYPES + subtype;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public boolean canCarry() {
        return canCarry != null;
    }

    public boolean canCarry(int type) {
        return (canCarry != null) && (canCarry[type]);
    }

    public boolean canCarryPersonnel() {
        if (canCarry != null) {
            for (int i = 0; i < canCarry.length; i += 2) {
                if (canCarry[i])
                    return true;
            }
        }
        return false;
    }

    public boolean canCarryVehicles() {
        if (canCarry != null) {
            for (int i = 1; i < canCarry.length; i += 2) {
                if (canCarry[i])
                    return true;
            }
        }
        return false;
    }

    public static String getUnitTypeName(int type) {
        if (type >= 0 && type < MAX_TYPES) {
            return TYPES[type];
        }
        return "";
    }

    public static int getMaxTypes() {
        return MAX_TYPES;
    }
}
