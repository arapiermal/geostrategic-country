package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.*;
enum DipOpinions{
    //Can they be put in one ?
    IMPROVED_RELATIONS,
    SENT_GIFT,
    DECLARED_WAR_ON_US,
    DECLARED_WAR_ON_NEIGHBOUR,
    DECLARED_WAR_ON_ALLY,
    DECLARED_WAR_ON_RIVAL,


    //private Map<DipOpinions, Short> opinions;
    //private Map<DipOpinions, Short>[] opinions;

}
//!!!!!!!!!!!!!!!!!!!!
public class Diplomacy implements Serializable {
    //private Country main;
    private static final short DEF_IMPROVE_RELATIONS_SIZE = 10;
    private int diplomaticStatus;
    private short improveRelationsSize;
    private short[] relations;
    private short[] espionage;

    private Set<Short> allies;
    private Set<Short> rivals;

    public Diplomacy() {
        this.improveRelationsSize = DEF_IMPROVE_RELATIONS_SIZE;
        this.allies = new HashSet<>();
        this.rivals = new HashSet<>();
        this.relations = new short[CountryArray.maxISO2Countries];
        resetRelations(); //Countries which don't exist are also reset (?)
    }

    public void resetRelations() {
        Arrays.fill(relations, (short) 0);
    }

    public void resetRelations(Set<Integer> countries) {
        for (int i : countries) {
            relations[i] = 0;
        }
    }

    // Relations
    public short getRelations(int c) {
        if (c >= 0 && c <= relations.length)
            return relations[c];
        else
            return Short.MIN_VALUE;
    }

    public void improveRelations(int c) {
        if (c >= 0 && c <= relations.length)
            relations[c] += improveRelationsSize;
    }

    public void improveRelations(int c, short amount) {
        if (c >= 0 && c <= relations.length)
            relations[c] += amount;
    }
    public boolean isAllyWith(int c) {
        return allies.contains((short) c);
    }
    public boolean isAllyWith(short c) {
        return allies.contains(c);
    }

    public Set<Short> getAllies() {
        return allies;
    }

    public void setAllies(Set<Short> allies) {
        this.allies = allies;
    }

    public Set<Short> getRivals() {
        return rivals;
    }

    public void setRivals(Set<Short> rivals) {
        this.rivals = rivals;
    }

    public void addRival(Short rival) {
        rivals.add(rival);
    }

    public void removeRival(Short rival) {
        rivals.remove(rival);
    }

    public void clearRivals() {
        rivals.clear();
    }

    public void addAlly(int ally) {
        allies.add((short) ally);
    }

    public void clearAllies() {
        allies.clear();
    }

    public void removeAlly(int ally) {
        allies.remove((short) ally);
    }

    public void addImproveRelationsSize(short amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Cannot be negative");
        improveRelationsSize += amount;
    }

    public void removeImproveRelationsSize(short amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Cannot be negative");
        improveRelationsSize -= amount;
        if (improveRelationsSize < 1)
            improveRelationsSize = 1;
    }

    public short[] getRelations() {
        return relations;
    }

    public void setRelations(short[] relations) {
        this.relations = relations;
    }

    public int getDiplomaticStatus() {
        return diplomaticStatus;
    }

    public void setDiplomaticStatus(int diplomaticStatus) {
        this.diplomaticStatus = diplomaticStatus;
    }

    public short getImproveRelationsSize() {
        return improveRelationsSize;
    }

    public void setImproveRelationsSize(short improveRelationsSize) {
        this.improveRelationsSize = improveRelationsSize;
    }

}
