package com.erimali.cntrymilitary;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.List;

public class MilUnitData implements Comparable<MilUnitData> , Serializable {
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
                this.speed = Integer.parseInt(in[1]); //speed based on environment?!?
                break;
            case "atk":
                this.atk = new int[in.length - 1];
                for (int i = 1; i < in.length; i++) {
                    this.atk[i - 1] = Integer.parseInt(in[i]);
                }
                break;
            case "def":
                this.def = new int[in.length - 1];
                for (int i = 1; i < in.length; i++) {
                    this.def[i - 1] = Integer.parseInt(in[i]);
                }
                break;
            case "hp":
                this.hp = Integer.parseInt(in[1]);
                break;

            case "carry":
                if (isVehicle()) {
                    this.canCarry = true;
                }
                //more carry options, what can it carry
                break;
            case "tech":
                this.minMilTech = Math.min(0, Integer.parseInt(in[1]));
                break;
        }
    }

    public boolean isVehicle() {
        return type % 2 == 1;
    }

    @Override
    public int compareTo(MilUnitData o) {
        int f1 = type * MAX_SUBTYPES + subtype;
        int f2 = o.type * MAX_SUBTYPES + o.subtype;
        return Integer.compare(f1, f2);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
