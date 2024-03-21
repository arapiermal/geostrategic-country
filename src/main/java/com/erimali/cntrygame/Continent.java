package com.erimali.cntrygame;

public enum  Continent {
    AF("Africa"), AS("Asia"), EU("Europe"), NA("North America"), SA("South America"), OC("Oceania"), AQ("Antarctica");
	Continent(String name) {
		this.name = name;
	}
	private String name;
	public String getName() {
		return name;
	}
}
