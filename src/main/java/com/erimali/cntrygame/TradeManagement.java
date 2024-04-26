package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TradeManagement implements Serializable {
    static class TradeAgreement {
        Resource resource;
        int amount;
        Country buyingCountry, sellingCountry;

        public TradeAgreement(Resource resource, int amount, Country buying, Country selling) {
            this.resource = resource;
            this.amount = amount;
            this.buyingCountry = buying;
            this.sellingCountry = selling;
            buying.getEconomy().getTradeManagement().totalImport += calcPrice();
            selling.getEconomy().getTradeManagement().totalExport += calcPrice();
        }

        public void incAmount(int a) {
            if (a > 0)
                amount += a;
        }

        public void decAmount(int a) {
            if (a > 0)
                amount -= a;
        }

        public double calcPrice() {
            return resource.getValue() * amount;
        }
    }

    public TradeManagement(){

        this.tradeAgreements = new ArrayList<>();
    }

    public TradeManagement(double totalExport, double totalImport) {
        this.totalExport = totalExport;
        this.totalImport = totalImport;
        this.tradeAgreements = new ArrayList<>();
    }

    private double totalExport;
    private double totalImport;
    //List of trades with other countries,
    List<TradeAgreement> tradeAgreements;

    public double diffExportImport() {
        return totalExport - totalImport;
    }
    public void addTradeAgreement(){

    }
}
