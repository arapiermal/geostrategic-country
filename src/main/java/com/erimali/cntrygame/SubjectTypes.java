package com.erimali.cntrygame;

public enum SubjectTypes {
    PUPPET("Puppet state") {

    },
    SATELLITE("Satellite state"){

    },
    PROTECTORATE("Protectorate") {

    },
    //For AdmDiv area < x km^2
    CITY_STATE("City-State") {

    },
    SPACE_COLONY("Space colony") {

    },
    ;
    private final String desc;
    private int autonomy;
    SubjectTypes(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }

}