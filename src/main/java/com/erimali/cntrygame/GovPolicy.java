package com.erimali.cntrygame;

import javafx.collections.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.controlsfx.control.CheckListView;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

//instead abstract ... CPolicy, and positive/negative/removable
public enum GovPolicy {
    NEUTRALITY("Neutrality", 0, 1),
    MULTI_LINGUALISM("Embrace Linguistic Diversity", 16, 3.5),
    FORCED_NEUTRALITY("Forced Neutrality", -1),
    DEMILITARIZED("Demilitarized", -8),
    BANNED_MILITARY("Banned Military", -16),
    //based on pop of country?
    INCENTIVIZE_BIRTHS("Incentivize Births", 4, 2, 4) {
        @Override
        public void tick(Country c) {
            c.incPopulationIncrease(0.01);
        }
    },
    INCENTIVIZE_HEALTHINESS("Incentivize Healthiness", 4, 3),

    //increase base research (?)
    INCENTIVIZE_RESEARCH("Incentivize Research", 4, 1.5),

    NUCLEAR_PROGRAM("Nuclear Program", 256, 5, 3){
        @Override
        public void tick(Country c) {
            //nuclearTech
            //nuclearTech lvl 3 + enriched uranium =>
        }
    },

    ;


    public void tick(Country c) {
    }

    //Positive
    //Negative/imposed policies
    //value for AI to know whether it is a positive or negative policy
    private final String desc;
    private final int val;
    private double price;
    private int periodTick;

    GovPolicy(String desc, int val) {
        this.desc = desc;
        this.val = val;
    }

    GovPolicy(String desc, int val, double price) {
        this.desc = desc;
        this.val = val;
        this.price = price;
    }

    GovPolicy(String desc, int val, double price, int periodTick) {
        this.desc = desc;
        this.val = val;
        this.price = price;
        this.periodTick = periodTick;
    }

    public double getPrice() {
        return price;
    }

    public boolean isRemovable() {
        return val >= 0;
    }

    @Override
    public String toString() {
        return desc;
    }


    public boolean hasMonthlyTick() {
        return periodTick == 3;
    }
    public boolean hasYearlyTick() {
        return periodTick == 4;
    }
}
