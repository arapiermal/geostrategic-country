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
    FORCED_NEUTRALITY("Forced Neutrality", -1),
    DEMILITARIZED("Demilitarized", -8),
    BANNED_MILITARY("Banned Military", -16),
    //based on pop of country?
    INCENTIVIZE_BIRTHS("Incentivize Births", 4, 2) {
        public void action(Country c) {
            c.incPopulationIncrease(0.02);
        }
    },
    INCENTIVIZE_HEALTHINESS("Incentivize Healthiness", 4, 3),
    //TAX_CUT("", 8),

    //CYBERSECURITY_MEASURES("",16),
    //VACCINATION_MANDATES("",4),
    ;

    //Positive
    //Negative/imposed policies
    //value for AI to know whether it is a positive or negative policy
    private final String desc;
    private final int val;
    private double price;

    GovPolicy(String desc, int val) {
        this.desc = desc;
        this.val = val;
    }

    GovPolicy(String desc, int val, double price) {
        this.desc = desc;
        this.val = val;
        this.price = price;
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

}
