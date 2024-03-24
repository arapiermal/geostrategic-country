package com.erimali.cntrygame;

public class Trade {
    static class TradeAgreement{
        Resource resource;
        int amount;
        Country buyingCountry, sellingCountry;
        public TradeAgreement(Resource resource, int amount, Country buying, Country selling) {
            this.resource = resource;
            this.amount = amount;
            this.buyingCountry = buying;
            this.sellingCountry = selling;
        }
    }

    //int or Country...
    public static TradeAgreement makeTrade(Resource resource, int amount, Country buying, Country selling){
        return new TradeAgreement(resource, amount, buying, selling);
    }

}
