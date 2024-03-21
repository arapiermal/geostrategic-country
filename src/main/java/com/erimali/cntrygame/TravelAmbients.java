package com.erimali.cntrygame;

public enum TravelAmbients {

	// terrain -> solid parts of WorldMap
	// water -> all else
	// air -> both
	// load from txt through constructor?
	TERRAIN, // implement difficulty of terrain in world map//mountainous, etc.
	WATER, //
	AIR, //
	SPACE; //

	String[][] SUBTYPES = { { "Grassy", "Muddy", "Mountainous", "Desert" }, //
			{ "Ocean","Sea", "Lake"}, //
			{ "Air" }, //
			{ "Space" } };//
	double[][] DIFFICULTY = { { 1, 1.3, 1.1, 1.2 }, //
			{ 1 , 1.05,1.1 }, //
			{ 1 }, //
			{ 1 } };//

	public String getTypeName(int i) {
		return SUBTYPES[this.ordinal()][i];
	}

	public double getTypeDifficulty(int i) {
		return DIFFICULTY[this.ordinal()][i];
	}

	public static void main(String[] args) {
		TESTING.print(TravelAmbients.TERRAIN.getTypeName(0), TravelAmbients.TERRAIN.getTypeDifficulty(0));
	}
}
