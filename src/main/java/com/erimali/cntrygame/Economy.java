package com.erimali.cntrygame;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.Serializable;
import java.util.Arrays;

public class Economy implements Serializable {
    private final DoubleProperty treasury;
    private double lastMonthBalance;
    private String currency;// USD, EUR, ALL,...
    //private String currencyLongName; in the Currencies class
    private double inflationRate; //devaluation of currency

    private double gdp; //$
    private double economicGrowthRate; // increase of GDP in a year
    private double taxation; //Government budget...

    private double unemploymentRate;

    // private List<Industry> industries;
    // EXPORT VS IMPORT
    private TradeManagement tradeManagement;


    public Economy(String currency, double inflationRate, double gdp, double economicGrowthRate, double taxation) {
        this.currency = currency;
        this.inflationRate = inflationRate;
        this.gdp = gdp;
        this.economicGrowthRate = economicGrowthRate;
        this.taxation = taxation;
        this.tradeManagement = new TradeManagement(this);
        this.treasury = new SimpleDoubleProperty(calcBaseTreasuryInit());
    }

    public Economy(int len, String[]... in) {
        //if len == 5, len>4>3>2>1...
        switch (len) {
            case 5:
                tradeManagement = new TradeManagement(this, getValueOrDef(in[4], 0, 1.1e9), getValueOrDef(in[4], 1, 9e8));
            case 4:
                unemploymentRate = getValueOrDef(in[3], 0, 0.05);
            case 3:
                taxation = getValueOrDef(in[2], 0, 0.025);
            case 2:
                gdp = getValueOrDef(in[1], 0, 1e9);
                economicGrowthRate = getValueOrDef(in[1], 1, 0.01);
            case 1:
                currency = in[0][0];
                inflationRate = getValueOrDef(in[0], 1, 0.01);
        }
        if (tradeManagement == null)
            tradeManagement = new TradeManagement(this);
        this.treasury = new SimpleDoubleProperty(calcBaseTreasuryInit());
    }

    public double calcBaseTreasuryInit() {
        return Math.ceil(gdp * taxation / (12 - economicGrowthRate));
    }

    public void defaultBasicValues() {
        if (taxation == 0)
            taxation = 0.025;
        if (gdp == 0)
            gdp = 1e9;
        if (economicGrowthRate == 0)
            economicGrowthRate = 0.01;
    }

    private double getValueOrDef(String[] strings, int i, double v) {
        try {
            double val = GUtils.parseDoubleAndPercentThrow(strings[i]);
            return val;
        } catch (Exception e) {
            return v;
        }
    }

    public void monthlyTreasuryUpdate(double milExpenditures) {
        //revenue vs expenditures
        double revenue = taxation * (gdp / 12);
        //research spendings, soldiers upkeep, gov spendings
        double expenditures = milExpenditures;
        lastMonthBalance = tradeManagement.diffExportImportMonthly() + revenue - expenditures;
        treasury.set(treasury.get() + lastMonthBalance);
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

    public void addMulGDP(double amount) {
        if (amount > 0)
            gdp += amount * gdp;
    }

    public void removeMulGDP(double amount) {
        if (amount > 0)
            gdp -= amount * gdp;
    }

    public String formattedGDP() {
        return GUtils.doubleToString(gdp);
    }

    public void annex(Economy eco) {
        this.gdp += eco.gdp;
        this.treasury.set(treasury.get() + eco.treasury.get());
        this.unemploymentRate = (this.unemploymentRate + eco.unemploymentRate) / 2;
    }

    public void giveMoney(Country o, double amount) {
        if (treasury.get() >= amount) {
            treasury.set(treasury.get() - amount);
            DoubleProperty oTreasury = o.getEconomy().treasury;
            oTreasury.set(oTreasury.get() + amount);
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
        return treasury.get();
    }

    public void incTreasury(double amount) {
        if (amount > 0)
            treasury.set(treasury.get() + amount);
    }

    public void decTreasury(double amount) {
        if (amount > 0)
            treasury.set(treasury.get() - amount);
    }

    public void addTreasuryOrPercent(double amount) {
        if (amount < 2 && amount > -2) {
            treasury.set(treasury.get() + Math.abs(treasury.get()) * amount);
        } else {
            treasury.set(treasury.get() + amount);
        }
    }


    public double removeTreasury(double amount) {
        if (amount > 0) {
            if (treasury.get() >= amount)
                treasury.set(treasury.get() - amount);
            else {
                double extra = treasury.get();
                treasury.set(0);
                return extra;
            }
        }
        return 0;
    }

    public double getLastMonthBalance() {
        return lastMonthBalance;
    }

    public DoubleProperty treasuryProperty() {
        return treasury;
    }

    private static String stringFormatToPercent(double val) {
        return String.format("%.2f", val * 100);
    }

    public String toStringLong() {
        StringBuilder sb = new StringBuilder();
        sb.append("Currency: ").append(currency).append('\n');
        sb.append("Inflation: ").append(stringFormatToPercent(inflationRate)).append('%').append('\n');
        sb.append("GDP:$").append(GUtils.doubleToString(gdp)).append('\n');
        sb.append("GDP yearly growth rate: ").append(stringFormatToPercent(economicGrowthRate)).append('%').append('\n');
        sb.append("Taxation: ").append(stringFormatToPercent(taxation)).append('%').append('\n');
        sb.append("Unemployment rate: ").append(stringFormatToPercent(unemploymentRate)).append('%').append('\n');

        return sb.toString();
    }


    public double calcScore(){
        double score = 0;
        // GDP: Log-scaled to prevent rich countries from dominating score entirely
        if (gdp > 0) {
            score += Math.log10(gdp) * 100; // E.g., 1 trillion GDP ≈ score of 120
        }
        // Economic Growth Rate (positive impact)
        score += economicGrowthRate * 1000; // e.g., 0.03 growth = +30
        // Inflation (negative impact)
        score -= inflationRate * 500; // high inflation hurts economy
        // Taxation (represents government fiscal capacity)
        score += taxation * 500; // balanced weight
        // Unemployment (negative impact)
        score -= unemploymentRate * 400; // 10% = -40
        // Trade Balance (positive or negative)
        if (tradeManagement != null) {
            double tradeBalance = tradeManagement.diffExportImportMonthly() * 12; // annualized
            if (tradeBalance != 0) {
                score += Math.signum(tradeBalance) * Math.min(50, Math.log10(Math.abs(tradeBalance) + 1) * 10);
            }
        }

        return score; //Math.max(score, 0);
    }
}
