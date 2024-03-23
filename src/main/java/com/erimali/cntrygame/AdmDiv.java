package com.erimali.cntrygame;

//DIJKSTRA TO TRAVERSE?????
//int terrain; int timeToTraverse;
//AdmDiv[] neighbours;
//Adjacency matrix?
public class AdmDiv {
	// County,district,...
	private SVGProvince svgProvince;

	private String name;
	private String nativeName; // Durrës vs Durres ?

	private double area;
	private int population;
	private short mainLanguage; // + culture?

	private byte seperatism; //separationism separatism

	//SEPARATIONIST SENTIMENT
	// Subdivisions?

	//private short[] claimedBy; (previous owners) ...
	//

	public String toString() {
		return this.name;
	}

	public String toStringLong() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("\nArea: ").append(area).append(" km^2\nPopulation: ").append(population);
		return sb.toString();
	}

	public AdmDiv(String name, double area, int population) {
		this.name = name;
		this.area = area;
		this.population = population;
	}

	public AdmDiv(String name, String area, String population) {
		this.name = name;
		try {
			this.area = Double.parseDouble(area);
			this.population = Integer.parseInt(population);
		} catch (NumberFormatException e) {

		}
	}
	public void addPopulation(int pop) {
		this.population += pop;
	}
	public void subtractPopulation(int pop) {
		this.population -= pop;
		if(this.population < 0)
			this.population = 0;
	}
	public int incPopulation(double incPop) {
		int pop = (int) (this.population * incPop);
		this.population += pop;
		return pop;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public int getPopulation() {
		return population;
	}

	public void setPopulation(int population) {
		this.population = population;
	}

	public int getProvId(){
		return svgProvince.getProvId();
	}
	public int getOwnerId(){
		return svgProvince.getOwnerId();
	}
	public void setOwnerId(int id){
		svgProvince.setOwnerId(id);
	}
	public SVGProvince getSvgProvince() {
		return svgProvince;
	}

	public void setSvgProvince(SVGProvince svgProvince) {
		this.svgProvince = svgProvince;
	}
}
