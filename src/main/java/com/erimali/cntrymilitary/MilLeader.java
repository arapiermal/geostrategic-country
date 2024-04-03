package com.erimali.cntrymilitary;

import com.erimali.cntrygame.Person;

import java.io.Serial;
import java.io.Serializable;

public class MilLeader extends Person implements Serializable {
    private MilLeaderType type;

    public MilLeader(String input) {
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
