package com.erimali.cntrymilitary;

public enum MilLeaderType {
    CAPTAIN("Captain"),
    COLONEL("Colonel"),
    GENERAL("General");

    private final String type;

    MilLeaderType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
