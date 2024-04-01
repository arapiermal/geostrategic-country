package com.erimali.cntrymilitary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.List;

public class MilUnitData implements Comparable<MilUnitData> {
    private static final String[] TYPES = {"Soldiers", "Ground Vehicles", "Marines", "Water Vehicles",
            "Airborne forces", "Air Vehicles", "Space Vehicles", "Space soldiers"};
    protected static final int MAX_TYPES = 8;
    private static final int MAX_SUBTYPES = 4096;

    protected final byte type;

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

    protected int minMilTech;

    protected boolean canCarry;

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

    private void setValue(String[] in) {
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

            case "carry":
                if (isVehicle()) {
                    this.canCarry = true;
                }
                //more carry options, what can it carry
                break;
            case "tech":
                this.minMilTech = parseIntOrDef(in[1], 0);
                break;
        }
    }

    private void minValues() {


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
}
