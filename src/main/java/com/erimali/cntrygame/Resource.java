package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.List;

enum ResourceCategory{
    //For MilVehicles
    METAL,
    //For nukes
    RADIOACTIVE,
    ;
    //private String/int unitOfMeasurement;

}

public class Resource implements Comparable<Resource>, Serializable {
    private ResourceCategory category;
    private String name;
    private String desc;
    private double value;
    public Resource(ResourceCategory category, String name, String desc, int value) {
        this.category = category;
        this.name = name;
        this.desc = desc;
        this.value = value;
    }
    public Resource(String in) {
        String[] k = in.trim().split("\\s*:\\s*");
        if(k.length < 3)
            throw new IllegalArgumentException("NOT ENOUGH RESOURCE INPUT");
        int i = 0;
        this.category = ResourceCategory.valueOf(k[i++].toUpperCase());
        this.name = k[i++];
        if(k.length > 3)
            this.desc = k[i++];
        this.value = GUtils.parseD(k[i]);
        if(this.value <= 0.0)
            this.value = 1.0;
    }
    @Override
    public int compareTo(Resource o) {
        //category or name (?)
        return this.name.compareTo(o.name);
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public void setCategory(ResourceCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
    public static List<Resource>[] loadResources(){
        //[] -> categories ?
        return null;
    }
}
