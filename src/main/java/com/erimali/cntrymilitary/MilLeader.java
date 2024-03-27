package com.erimali.cntrymilitary;

import com.erimali.cntrygame.Person;

enum MilLeaderType {
    CAPTAIN("Captain"),
    COLONEL("Colonel"),
    GENERAL("General");

    private String type;

    MilLeaderType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}

public class MilLeader extends Person {
    private MilLeaderType type;

    public MilLeader(String input, String type) {
        super(input.substring(input.indexOf("->") + 2));//!!!!!!!!!!!!!
        int index = input.indexOf("->");
        this.type = MilLeaderType.valueOf(input.substring(0, index).trim());
    }


    public MilLeaderType getType() {
        return type;
    }
    public int getRank() {
        return type.ordinal();
    }
    public void setType(MilLeaderType type) {
        this.type = type;
    }
    @Override
    public String toString() {
        return type + " - " + super.toString();
    }
}
