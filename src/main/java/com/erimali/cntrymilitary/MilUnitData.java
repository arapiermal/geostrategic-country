package com.erimali.cntrymilitary;

import com.erimali.cntrygame.GUtils;
import com.erimali.cntrygame.Language;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MilUnitData implements Comparable<MilUnitData> {
    private static final String[] TYPES = {"soldiers", "ground-vehicles", "marines", "ships",
            "airborne-forces", "planes", "space-ships", "space-soldiers"};
    protected static final int MAX_TYPES = 8;
    private static final int MAX_SUBTYPES = 4096;

    protected final int type;
    protected int subtype;
    protected String name;
    protected String desc;
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

    private double baseScore;

    public MilUnitData() {
        this.type = -1;
    }

    public MilUnitData(String loc) throws Exception {
        this.type = (byte) (loc.charAt(loc.length() - 1) - '0');
        initEmptyAtkDef();
        if (type < 0 || type >= MAX_TYPES)
            throw new IllegalArgumentException("TYPE CAN BE FROM 0 UP TO " + (MAX_TYPES - 1));
        try (BufferedReader br = new BufferedReader(new FileReader(loc))) {
            String line;
            while ((line = br.readLine()) != null) {
                setValue(line.trim().split("\\s*:\\s*"));
            }
            minValues();
        }
        calcBaseScore();
    }

    //!! TO IMPROVE
    public void calcBaseScore() {
        baseScore = 0;
        double avgAtk = (double) Arrays.stream(atk).sum() / MAX_TYPES;
        double avgDef = (double) Arrays.stream(def).sum() / MAX_TYPES;
        baseScore += avgAtk + avgDef + hp + speed + minMilTech;
    }


    public void initEmptyAtkDef(){
        if(atk == null)
            atk = new int[MAX_TYPES];
        Arrays.fill(atk, 1);
        if(def == null)
            def = new int[MAX_TYPES];
        Arrays.fill(def, 1);
    }
    public MilUnitData(int type, int subtype, String name, String desc, int speed, int[] atk, int[] def, int hp, int maxSize, int minMilTech, boolean[] canCarry) {
        this.type = type;
        this.subtype = subtype;
        this.name = name;
        this.desc = desc;
        this.speed = speed;
        this.atk = atk;
        this.def = def;
        this.hp = hp;
        this.maxSize = maxSize;
        this.minMilTech = minMilTech;
        this.canCarry = canCarry;
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
                int personnelAtk = Math.max(1, parseIntOrDef(in[1], 1));
                for (int i = 0; i < atk.length; i += 2) {
                    atk[i] = personnelAtk;
                }
                int j = 1;
                for (int i = 2; i < in.length; i++) {
                    atk[j] = Math.max(1, parseIntOrDef(in[i], 1));
                    j += 2;
                }
                break;
            case "def":
                int personnelDef = Math.max(1, parseIntOrDef(in[1], 1));
                for (int i = 0; i < def.length; i += 2) {
                    def[i] = personnelDef;
                }
                int k = 1;
                for (int i = 2; i < in.length; i++) {
                    def[k] = Math.max(1, parseIntOrDef(in[i], 1));
                    k += 2;
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
        return name;
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

    public static String getUnitTypeNameUpper(int type) {
        if (type >= 0 && type < MAX_TYPES) {
            return Language.uppercaseFirstChar(TYPES[type]);
        }
        return "";
    }

    public static int getMaxTypes() {
        return MAX_TYPES;
    }

    public String toStringLong() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(name).append('\n');
        sb.append("Desc: ").append(desc).append('\n');
        sb.append("Speed: ").append(speed).append('\n');
        sb.append("Size: ").append(maxSize).append('\n');
        sb.append("Tech: ").append(minMilTech).append('\n');
        sb.append("HP: ").append(hp).append('\n');
        sb.append("ATK: ").append(toStringArr(atk)).append('\n');
        sb.append("DEF: ").append(toStringArr(def)).append('\n');
        return sb.toString();
    }
    public static String toStringArr(int[] a) {
        if (a == null)
            return "null";
        int iMax = a.length - 1;
        if (iMax == -1)
            return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(a[i]);
            if (i == iMax)
                return b.toString();
            b.append(':');
        }
    }
    public String getDesc() {
        return desc;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public int getMinMilTech() {
        return minMilTech;
    }

    public String getName() {
        return name;
    }

    public double getBaseScore() {
        return baseScore;
    }
}
