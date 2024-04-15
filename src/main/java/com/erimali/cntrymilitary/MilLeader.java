package com.erimali.cntrymilitary;

import com.erimali.cntrygame.Person;

import java.io.Serial;
import java.io.Serializable;

public class MilLeader extends Person implements Serializable {
    private static int CURR_ID = 0;
    private MilLeaderType type;
    private int strategy;
    private int logistics;

    public MilLeader(String input) {
        super(input.substring(input.indexOf("->") + 2));//!!!!!!!!!!!!!
        int index = input.indexOf("->");
        this.type = MilLeaderType.valueOf(input.substring(0, index).trim());
        this.strategy = 1 + (int) (Math.random() * 10);
        this.strategy = 1 + (int) (Math.random() * 10);
    }

    public MilLeader() {
        super("Filan", "Fisteku " + CURR_ID++);
        this.type = MilLeaderType.values()[(int) (Math.random() * MilLeaderType.values().length)];
        this.strategy = 1 + (int) (Math.random() * 10);
        this.strategy = 1 + (int) (Math.random() * 10);
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

    public double atkBonus() {
        return type.ordinal() * strategy * 0.1;
    }

    public double defBonus() {
        return type.ordinal() * logistics * 0.1;
    }

    public int getLogistics() {
        return logistics;
    }

    public void setLogistics(int logistics) {
        this.logistics = logistics;
    }

    public int getStrategy() {
        return strategy;
    }

    public void setStrategy(int strategy) {
        this.strategy = strategy;
    }

}
