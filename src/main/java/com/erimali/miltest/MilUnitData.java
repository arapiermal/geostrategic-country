package com.erimali.miltest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class MilUnitData {
    private static final String[] TYPES = {"Soldiers", "Marines", "Airborne forces", "Space soldiers", "",
            "Ground Vehicles", "Water Vehicles", "Air Vehicles", "Space vehicles", ""};
    private static final int MAX_TYPES = 10;
    //0-4 -> personnel, 5-9 -> vehicles
    private static int CURR_ID = 0;
    protected final int id;
    protected final byte type;

    protected String name;
    protected String desc;
    protected int subtype;
    protected int speed;
    protected int[] atk;
    protected int[] def;
    protected int hp;
    //
    protected boolean canCarry;

    public MilUnitData(String loc) throws Exception {
        this.type = (byte) (loc.charAt(loc.length() - 1) - '0');
        try (BufferedReader br = new BufferedReader(new FileReader(loc))) {
            String line;
            while ((line = br.readLine()) != null) {
                setValue(line.split("\\s*:\\s*"));
            }
        }
        this.id = CURR_ID++;

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
                this.hp = Integer.parseInt(in[1]); //speed based on environment?!?
                break;

            case "carry":
                if(isVehicle())
                    this.canCarry = true;
                //more carry options, what can it carry
                //problem, all vehicles carry sth
                break;
        }
    }

    public static List<MilUnitData> loadAllUnitData() {


        return null;
    }

    public boolean isVehicle() {
        return type > 4 && type < 10;
    }
}
