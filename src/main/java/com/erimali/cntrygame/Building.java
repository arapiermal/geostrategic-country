package com.erimali.cntrygame;
//if starts with MIL -> Destroyable by army, boolean (?)
public enum Building{
    //_BUILDING(-1,"Buildings", (byte) 0, 0),
    //_MIL(-2,"Military", (byte) 0, 0),
    MIL_BASE(0,"Military Base", (byte) 4, 1000000),
    MIL_TRAINING_CAMP(0,"Training Camp", (byte) 4,1000000),
    MIL_RESEARCH_FACILITY(0,"Mil Research Facility", (byte) 4,1000000),
    //__MIL_FACTORY(-3,"Military Factory",(byte) 0, 0),
    MIL_FACTORY_PRODUCTION(0,"Mil Factory Production", (byte) 4,1000000), //base parts
    MIL_FACTORY_WEAPONS(0,"Weapons Factory", (byte) 4,1000000),
    MIL_FACTORY_GROUND_VEHICLES(0,"Land Vehicles Factory", (byte) 4,1000000),
    MIL_FACTORY_WATER_VEHICLES(0,"Water Vehicles Factory", (byte) 4,1000000),
    MIL_FACTORY_AIRCRAFT(0,"Aircraft Factory", (byte) 4,1000000),
    MIL_NUCLEAR_FACILITY(0,"Nuclear Facility", (byte) 4,1000000),
    MIL_AIRPORT(0,"Military Airport", (byte) 4,1000000),
    //_DIP(-2,"Diplomatic", (byte) 0,0),
    DIP_INTELLIGENCE_AGENCY(1,"Intelligence Agency", (byte) 4,1000000),
    //_OTHERS(-2,"Other", (byte) 0,0),
    AIRPORT(2,"Airport", (byte) 4,1000000),
    FACTORY_PRODUCTION(2,"Factory Production", (byte) 4,1000000),
    RESEARCH_FACILITY(2,"Research Facility", (byte) 4,1000000),

    ;
    int type;
    String desc;
    byte stepsToBuild;//can be decreased after some decades since new tech
    int price;//$
    Building(int type, String desc, byte stepsToBuild, int price){
        this.type = type;
        this.desc = desc;
        this.stepsToBuild = stepsToBuild;
        this.price = price;
    }

    @Override
    public String toString() {
        return desc;
    }
    public boolean isMilitary(){
        return type == 0;
    }

    public boolean isDiplomatic(){
        return type == 1;
    }

    public boolean isOther(){
        return type == 2;
    }
    //public boolean isRootLike(){ return type < 0; }
}
