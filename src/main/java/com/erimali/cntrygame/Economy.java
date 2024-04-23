package com.erimali.cntrygame;

import java.io.Serializable;

public class Economy implements Serializable {
    private double treasury;
    private String currency;// USD, EUR, ALL,...
    private String currencyLongName;

    private double inflationRate; //devaluation of currency

    private double gdp; //$
    private double economicGrowthRate; // increase of GDP in a year
    private double taxation; //Government budget...

    private double unemploymentRate;


    //private List<Industry> industries;
    // EXPORT VS IMPORT
    private final TradeManagement tradeManagement;


    public Economy() {

        tradeManagement = new TradeManagement();
    }

    public void monthlyTreasuryUpdate() {
        //revenue vs expenditures
        double revenue = taxation * (gdp / 12);

        double expenditures = 0;//research spendings, soldiers upkeep, gov spendings

        treasury  += tradeManagement.diffExportImport() + revenue - expenditures;
    }
    public void yearlyTick(){

        gdp += (economicGrowthRate - inflationRate) * gdp;
    }
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyLongName() {
        return currencyLongName;
    }

    public void setCurrencyLongName(String currencyLongName) {
        this.currencyLongName = currencyLongName;
    }

    public double getGDP() {
        return gdp;
    }

    public void setGDP(double gdp) {
        this.gdp = gdp;
    }

    public double getEconomicGrowthRate() {
        return economicGrowthRate;
    }

    public void setEconomicGrowthRate(double economicGrowthRate) {
        this.economicGrowthRate = economicGrowthRate;
    }


    public void addGDP(double amount) {
        this.gdp += amount;
    }

    public void addGDP(String gdpString) {
        try {
            gdpString = gdpString.trim();
            if (gdpString.endsWith("%")) {
                gdpString = gdpString.substring(0, gdpString.length() - 1);
                gdp += gdp * (Double.parseDouble(gdpString) * 0.01);
            } else {
                double amount = Double.parseDouble(gdpString);
                gdp += amount;
            }

        } catch (NumberFormatException e) {
            ErrorLog.logError(e);
        }
    }

    public String formattedGDP() {
        return GUtils.doubleToString(gdp);
    }

    public void annex(Economy eco) {
        this.gdp += eco.gdp;
        this.treasury += eco.treasury;
        this.unemploymentRate = (this.unemploymentRate + eco.unemploymentRate) / 2;
    }

    public void giveMoney(Country o, double amount) {
        if(treasury >= amount){
            treasury -= amount;
            o.getEconomy().treasury += amount;
            o.getDiplomacy().improveRelations(o.getCountryId(), (short) Math.log(amount));
        }
    }

    public void incTaxation(double amount) {
        if (amount > 0)
            taxation += amount;
    }

    public void decTaxation(double amount) {
        if (amount > 0)
            taxation -= amount;
    }

    public double getTaxation() {
        return taxation;
    }


    public double getInflationRate() {
        return inflationRate;
    }

    public void setInflationRate(double inflationRate) {
        this.inflationRate = inflationRate;
    }

    public TradeManagement getTradeManagement() {
        return tradeManagement;
    }
}
