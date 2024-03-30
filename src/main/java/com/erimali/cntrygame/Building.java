package com.erimali.cntrygame;
//if starts with MIL -> Destroyable by army, boolean (?)
public enum Building{
    _BUILDING("Buildings", (byte) 0),
    _MIL("Military", (byte) 0),
    MIL_BASE("Military Base", (byte) 4),
    MIL_TRAINING_CAMP("Training Camp", (byte) 4),
    MIL_FACTORY_PRODUCTION("Mil Factory Production", (byte) 4), //base parts
    MIL_FACTORY_WEAPONS("Mil Factory - Weapons", (byte) 4),
    MIL_FACTORY_GROUND_VEHICLES("Mil Factory - Land vehicles", (byte) 4),
    MIL_FACTORY_WATER_VEHICLES("Mil Factory - Ships", (byte) 4),
    MIL_FACTORY_AIRCRAFT("Mil Factory - Aircraft", (byte) 4),
    MIL_NUCLEAR_FACILITY("Mil Nuclear Facility", (byte) 4),
    MIL_AIRPORT("Mil Airport", (byte) 4),
    MIL_RESEARCH_FACILITY("Mil Research Facility", (byte) 4),
    _DIP("Diplomatic", (byte) 0),
    DIP_INTELLIGENCE_AGENCY("Intelligence Agency", (byte) 4),
    _OTHERS("Other", (byte) 0),
    AIRPORT("Airport", (byte) 4),
    FACTORY_PRODUCTION("Factory Production", (byte) 4),
    RESEARCH_FACILITY("Research Facility", (byte) 4),

    ;
    String desc;
    byte stepsToBuild;//can be decreased after some decades since new tech
    Building(String desc, byte stepsToBuild){
        this.desc = desc;
        this.stepsToBuild = stepsToBuild;
    }

    @Override
    public String toString() {
        return desc;
    }
    public boolean isMilitary(){
        return this.name().startsWith("MIL");
    }

    public boolean isDiplomatic(){
        return this.name().startsWith("DIP");
    }
}
