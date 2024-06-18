package com.erimali.cntrymilitary;

import com.erimali.cntrygame.AdmDiv;

import java.util.ArrayList;
interface MilAttacker{
    int attack(MilDiv milDiv);
    int attack(MilUnit milUnit); //only rebels would be left with this (?) or ALL REBELS OF A TYPE IN MilDiv!!!!
    int attack(AdmDiv admDiv);
}
public class MilList extends ArrayList<MilUnit> {
    private int ownerId;


}
