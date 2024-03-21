package com.erimali.cntrygame;

public enum  Continent {
    AF("Africa"), AS("Asia"), EU("Europe"), NA("North America"), SA("South America"), OC("Oceania"), AQ("Antarctica");
	Continent(String longName) {
		this.longName = longName;
	}
	private String longName;
	public String getLongName() {
		return longName;
	}

}
