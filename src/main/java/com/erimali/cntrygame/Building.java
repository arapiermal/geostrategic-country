package com.erimali.cntrygame;

import java.util.EnumSet;
import java.util.Set;

//if starts with MIL -> Destroyable by army, boolean (?)
public enum Building {
    MIL_BASE(0, "Military Base", (byte) 4, 1000000),
    MIL_TRAINING_CAMP(0, "Training Camp", (byte) 4, 1000000),
    MIL_RESEARCH_FACILITY(0, "Mil Research Facility", (byte) 4, 2000000),
    MIL_FACTORY_PRODUCTION(0, "Mil Factory Production", (byte) 4, 1000000), //base parts
    MIL_FACTORY_WEAPONS(0, "Weapons Factory", (byte) 4, 1000000),
    MIL_FACTORY_GROUND_VEHICLES(0, "Land Vehicles Factory", (byte) 4, 1000000),
    MIL_FACTORY_WATER_VEHICLES(0, "Water Vehicles Factory", (byte) 4, 1000000),
    MIL_FACTORY_AIRCRAFT(0, "Aircraft Factory", (byte) 4, 1000000),
    MIL_NUCLEAR_FACILITY(0, "Nuclear Facility", (byte) 8, 1000000),
    MIL_AIRPORT(0, "Military Airport", (byte) 4, 1000000),

    DIP_INTELLIGENCE_AGENCY(1, "Intelligence Agency", (byte) 4, 1000000),

    AIRPORT(2, "Airport", (byte) 4, 1000000), //economy/tourism boost
    FACTORY_PRODUCTION(2, "Factory Production", (byte) 4, 1000000),
    RESEARCH_FACILITY(2, "Research Facility", (byte) 4, 1000000),

    ;

    private int type;
    private String desc;
    private byte stepsToBuild; //can be decreased after some decades since new tech
    private int price;//$

    Building(int type, String desc, byte stepsToBuild, int price) {
        this.type = type;
        this.desc = desc;
        if (stepsToBuild > 1) {
            this.stepsToBuild = stepsToBuild;
        } else {
            this.stepsToBuild = 2;
        }
        this.price = price;
    }

    @Override
    public String toString() {
        return desc;
    }

    public boolean isMilitary() {
        return type == 0;
    }

    public boolean isDiplomatic() {
        return type == 1;
    }

    public boolean isOther() {
        return type == 2;
    }

    public byte getStepsToBuild() {
        return stepsToBuild;
    }

    public void setStepsToBuild(byte stepsToBuild) {
        this.stepsToBuild = stepsToBuild;
    }


    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    //public boolean isRootLike(){ return type < 0; }
}
