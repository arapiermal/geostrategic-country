package com.erimali.cntrygame;

public class Resource implements Comparable<Resource>{
    private int category;
    private String name;
    private String desc;
    private int value;
    //private String/int unitOfMeasurement;

    @Override
    public int compareTo(Resource o) {
        //category or name (?)
        return this.name.compareTo(o.name);
    }
}
