package com.erimali.cntryrandom;

import javafx.scene.paint.Color;

import java.util.List;

public class RandCountry {
    private int countryId;
    private String name;
    private Color color;
    private List<RandProvince> provinces;

    public RandCountry(int countryId){
        this.countryId = countryId;

    }


    public void setColor(Color color){
        this.color = color;
        for(RandProvince prov : provinces)
            prov.setColor(color);
    }
}
