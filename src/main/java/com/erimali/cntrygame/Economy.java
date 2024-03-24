package com.erimali.cntrygame;

import java.util.List;

public class Economy {
	// TRADE??
	private String currency;// USD, EUR, ALL,...
	private String currencyLongName;
	private double inflationRate;

	private double gdp;
	private double economicGrowthRate; // increase of GDP in a year

	private double taxation; //Government budget...

	private double unemploymentRate;
	//private List<Industry> industries;
	// EXPORT VS IMPORT
	private double totalExport;
	private double totalImport;
	//List of trades with other countries,
	List<Trade.TradeAgreement> tradeAgreements;
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getGDP() {
		return gdp;
	}

	public void setGDP(double gdp) {
		gdp = gdp;
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

	public void addGDP(double amount) {
		this.gdp += amount;
	}

	public void addGDP(String gdpString) {
		try {
			gdpString = gdpString.trim();
			if(gdpString.endsWith("%")) {
				gdpString = gdpString.substring(0, gdpString.length()-1);
				gdp += gdp * (Double.parseDouble(gdpString) * 0.01);
			} else {
				double amount = Double.parseDouble(gdpString);
				gdp += amount;
			}

		} catch(NumberFormatException e) {
			ErrorLog.logError(e);
		}
	}
	public String formattedGDP() {
		return GUtils.doubleToString(gdp);
	}
}
