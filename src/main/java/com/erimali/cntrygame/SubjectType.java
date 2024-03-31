package com.erimali.cntrygame;

public enum SubjectType {
    //Control all actions
    PUPPET("Puppet state") {

    },
    //
    SATELLITE("Satellite state"){

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
    private int autonomy;
    SubjectType(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }

}