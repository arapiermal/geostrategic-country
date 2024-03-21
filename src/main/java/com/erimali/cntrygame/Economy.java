package com.erimali.cntrygame;

public class Economy {
	// TRADE??
	private String currency;// USD, EUR, ALL,...
	private String currencyLongName;
	private double GDP;
	private double economicGrowthRate; // increase of GDP in a year
	// private double unemploymentRate;
	// private double povertyRate;
	//private List<Industry> industries;
	// EXPORT VS IMPORT
	private double totalExport;
	private double totalImport;
	//List of trades with other countries,

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getGDP() {
		return GDP;
	}

	public void setGDP(double gdp) {
		GDP = gdp;
	}

	public double getEconomicGrowthRate() {
		return economicGrowthRate;
	}

	public void setEconomicGrowthRate(double economicGrowthRate) {
		this.economicGrowthRate = economicGrowthRate;
	}

	public String getCurrencyLongName() {
		return currencyLongName;
	}

	public void setCurrencyLongName(String currencyLongName) {
		this.currencyLongName = currencyLongName;
	}

	public void addGDP(double GDP) {
		this.GDP += GDP;
	}
	/*
			int i = 0;
			double val=0;
			while(i < gdpString.length()){
				char c = gdpString.charAt(i);
				if(Character.isDigit(c)){
					val*=10;
					val+=c-'0';
				} else if(c == '.'){
					i++;
					break;
				} else if(c == '%'){
					this.GDP += this.GDP * val;
					return;
				}
				i++;
			}
			double decVal = 0;
			while(i<gdpString.length()){
				//Is it worth it for a little gain
				i++;
			}
			*/
	public void addGDP(String gdpString) {
		try {
			gdpString = gdpString.trim();
			if(gdpString.endsWith("%")) {
				gdpString = gdpString.substring(0, gdpString.length()-1);
				this.GDP += this.GDP * (Double.parseDouble(gdpString) * 0.01);
			} else {
				double GDP = Double.parseDouble(gdpString);
				this.GDP += GDP;
			}

		} catch(NumberFormatException e) {
			ErrorLog.logError(e);
		}
	}
	public String formattedGDP() {
		return GUtils.doubleToString(GDP);
	}
}
