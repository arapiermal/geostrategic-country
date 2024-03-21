package com.erimali.cntrygame;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
//!!!!!!!!!!!!!!!!!!!!
public class Diplomacy {
	//private Country main;
	private static final short DEF_IMPROVERELATIONSSIZE = 10;
	private int diplomaticStatus;
	private short improveRelationsSize;

	private Map<String, Short> relations;
	private Set<String> allies;
	private Set<String> rivals;

	public Diplomacy() {
		this.setImproveRelationsSize(DEF_IMPROVERELATIONSSIZE);
		this.allies = new HashSet<>();
		this.rivals = new HashSet<>();

		this.relations = new HashMap<>();
	}

	// Relations
	public int getRelations(String c) {
		if (!relations.containsKey(c)) {
			relations.put(c, (short) 0);
		}
		return relations.get(c);
	}

	public void improveRelations(String c) {
		if (relations.containsKey(c)) {
			relations.put(c, (short) (relations.get(c) + getImproveRelationsSize()));
		} else {
			relations.put(c, getImproveRelationsSize());
		}
	}

	public void improveRelations(String c, short amount) {
		if (relations.containsKey(c)) {
			relations.put(c, (short) (relations.get(c) + amount));
		} else {
			relations.put(c, amount);
		}
	}

	public Set<String> getAllies() {
		return allies;
	}

	public void setAllies(Set<String> allies) {
		this.allies = allies;
	}

	public Set<String> getRivals() {
		return rivals;
	}

	public void setRivals(Set<String> rivals) {
		this.rivals = rivals;
	}
	public void addRival(String rival) {
		rivals.add(rival);
	}
	public void removeRival(String rival) {
		rivals.remove(rival);
	}
	public void clearRivals() {
		rivals.clear();
	}
	public void addAlly(String ally) {
		allies.add(ally);
	}
	public void clearAllies() {
		allies.clear();
	}
	public void removeAlly(String ally) {
		allies.remove(ally);
	}
	public Map<String, Short> getRelations() {
		return relations;
	}

	public void setRelations(Map<String, Short> relations) {
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
	public void addImproveRelationsSize(short amount) {
		if(amount < 0)
			throw new IllegalArgumentException("Cannot be negative");
		this.improveRelationsSize += amount;
	}
	public void removeImproveRelationsSize(short amount) {
		if(amount < 0)
			throw new IllegalArgumentException("Cannot be negative");
		this.improveRelationsSize -= amount;
	}
}
