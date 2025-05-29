package com.erimali.cntryrandom;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RandCountry {
    private int countryId;
    private String name;
    private Color color;
    private List<RandProvince> provinces;
    private Set<RandCountry> neighbours;
    private RandCultures culture;

    public RandCountry(int countryId){
        this.countryId = countryId;
        this.culture = RandCultures.getRandomCulture();
        this.provinces = new ArrayList<>();
        this.neighbours = new HashSet<>();

    }

    public void generateNamesForProvinces(){
        for(RandProvince prov : provinces){
            prov.setName(RandProvNameGen.generateName(culture));
        }
    }



    public void setColor(Color color){
        this.color = color;
        for(RandProvince prov : provinces)
            prov.setColor(color);
    }


}
