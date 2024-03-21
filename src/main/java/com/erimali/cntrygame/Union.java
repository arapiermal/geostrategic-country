package com.erimali.cntrygame;

import java.util.List;
import java.util.Map;

public class Union {
	//Political union
	//Economic union
	//Military union

	private int stability;
	private String name;
	private Map<String, Integer> unionCountries; // hmmm
	// power?//Influence?
	private float centralization;
	private Military unionMilitary; // can be null
	// private UPanel panel;//accessible by the player for choices inside the union
	// event-like?
	String[][] choices;
	String[][] commands; // hmmm
	// Acts you can take

	List<Integer[]> votes;

	public void uniteAllCountries(String mainCountry, Map<String, Country> countries) {
		Country c = countries.get(mainCountry);
		for (String s : unionCountries.keySet()) {
			c.uniteWith(name, countries.get(s));
		}
		// DISSOLVE UNION
	}

	// change
	public void increaseCentralization(float inc) {
		this.centralization += inc;
		if (this.centralization > 100)
			this.centralization = 100;
	}

	public void decreaseCentralization(float dec) {
		this.centralization -= dec;
		if (this.centralization < -100)
			this.centralization = -100;
	}

	// how to remove from Map<String,Union> in World?
	// from gamestage?
	public boolean canDismantle(String cn) {
		if (unionCountries.containsKey(cn)) {
			if (unionCountries.get(cn) > 90) {
				return true;
			}
		}
		return false;
	}

	public void removeCountry(String cn) {
		if (unionCountries.containsKey(cn)) {
			unionCountries.remove(cn);
		}
	}
	/*
	 European Union type:economic,... membercountries:IT,... policies:free trade
	 (or index of sth in the code?),free movement,... events:more centralized?,
	 immigration (make panel for EU and other unions like hre in EU4?)
	 */
	
	public boolean hasMilitary() {
		return unionMilitary != null;
	}
}
