package com.erimali.cntrygame;
enum BuildingType{
    FACTORY,
    AIRPORT,
    MIL_LANDVEHICLE,
    MIL_AIRCRAFT
}
public class Building {
    //In AdmDiv. Or what about making enum and EnumSet<Building> for efficiency, which allows you to do things if you have the building
    BuildingType type;

}
