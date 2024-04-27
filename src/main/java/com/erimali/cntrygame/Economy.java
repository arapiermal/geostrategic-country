package com.erimali.cntrygame;

import java.io.Serializable;

public class Economy implements Serializable {
    private double treasury;
    private double lastMonthBalance;
    private String currency;// USD, EUR, ALL,...
    //private String currencyLongName; in the Currencies class
    private double inflationRate; //devaluation of currency

    private double gdp; //$
    private double economicGrowthRate; // increase of GDP in a year
    private double taxation; //Government budget...

    private double unemploymentRate;


    //private List<Industry> industries;
    // EXPORT VS IMPORT
    private final TradeManagement tradeManagement;


    public Economy(String currency, double inflationRate, double gdp, double economicGrowthRate, double taxation) {
        this.currency = currency;
        this.inflationRate = inflationRate;
        this.gdp = gdp;
        this.economicGrowthRate = economicGrowthRate;
        this.taxation = taxation;
        tradeManagement = new TradeManagement();
    }

    public Economy(String[]... in) {
        switch (in.length) {
            case 4:
                unemploymentRate = getValueOrDef(in[2], 0, 5.0);
            case 3:
                taxation = getValueOrDef(in[2], 0, 2.5);
            case 2:
                gdp = getValueOrDef(in[1], 0, 1000000000);
                economicGrowthRate = getValueOrDef(in[1], 0, 1.0);
            case 1:
                currency = in[0][0];
                inflationRate = getValueOrDef(in[0], 1, 1.0);
        }

        tradeManagement = new TradeManagement();
    }
    public void defaultBasicValues(){
        if(taxation == 0)
            taxation = 2.5;
        if(gdp == 0)
            gdp = 1e9;
        if(economicGrowthRate == 0)
            economicGrowthRate = 1.0;
    }
    private double getValueOrDef(String[] strings, int i, double v) {
        try {
            double val = Double.parseDouble(strings[i]);
            return val;
        } catch (Exception e) {
            return v;
        }
    }

    public void monthlyTreasuryUpdate() {
        //revenue vs expenditures
        double revenue = taxation * (gdp / 12);

        double expenditures = 0;//research spendings, soldiers upkeep, gov spendings
        lastMonthBalance = tradeManagement.diffExportImport() + revenue - expenditures;
        treasury += lastMonthBalance;
    }

    public void yearlyTick() {
        gdp += (economicGrowthRate - inflationRate) * gdp;
    }

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

    public void addPercentGDP(double amount) {
        gdp += amount * (gdp * 0.01);
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
        if (treasury >= amount) {
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

    public double getTreasury() {
        return treasury;
    }

    public void incTreasury(double amount) {
        if (amount > 0)
            treasury += amount;
    }

    public void decTreasury(double amount) {
        if (amount > 0)
            treasury -= amount;
    }

    public double removeTreasury(double amount) {
        if (amount > 0) {
            if (treasury >= amount)
                treasury -= amount;
            else {
                double extra = treasury;
                treasury = 0;
                return extra;
            }
        }
        return 0;
    }

    public double getLastMonthBalance() {
        return lastMonthBalance;
    }
}
