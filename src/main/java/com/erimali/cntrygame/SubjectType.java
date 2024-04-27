package com.erimali.cntrygame;

public enum SubjectType {
    //Control all actions
    PUPPET("Puppet state", 0.5) {

    },
    //
    SATELLITE("Satellite state",0.25){

    },
    PROTECTORATE("Protectorate") {

    },
    //For AdmDiv area < x km^2
    CITY_STATE("City-state") {

    },
    SPACE_COLONY("Space colony") {

    },
    ;
    private final String desc;
    private double taxation;
    private int autonomy;
    SubjectType(String desc) {
        this.desc = desc;
    }
    SubjectType(String desc, double taxation) {
        this.desc = desc;
        this.taxation = taxation;
    }

    public boolean isTaxable(){
        return taxation > 0.0;
    }
    public double getTaxation(){
        return taxation;
    }
    @Override
    public String toString() {
        return desc;
    }

}