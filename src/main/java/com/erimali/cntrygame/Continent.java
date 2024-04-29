package com.erimali.cntrygame;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public enum Continent {
    AF("Africa", Color.LIGHTCYAN), AS("Asia", Color.ORANGERED), EU("Europe", Color.DARKBLUE),
    NA("North America", Color.LIGHTYELLOW), SA("South America", Color.PALEVIOLETRED),
    OC("Oceania", Color.LIGHTCORAL), AQ("Antarctica", Color.WHITESMOKE);

    Continent(String longName, Paint color) {
        this.longName = longName;
        this.color = color;
        this.countries = new HashSet<>();
    }

    private final String longName;
    private final Set<Short> countries;
    private Paint color;
    //private String[] regions;

    private void addCountry(short id) {
        countries.add(id);
    }

    private void addCountry(String id) {
        countries.add(CountryArray.getIndexShort(id));
    }

    public Set<Short> getCountries() {
        return countries;
    }

    public Paint getColor() {
        return color;
    }

    @Override
    public String toString() {
        return longName;
    }

    public static void setFromFolderCSV(String path, World world) {
        TESTING.print(path);
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            File[] csvFiles = folder.listFiles();
            if (csvFiles == null)
                return;
            for (File csvFile : csvFiles) {
                if (csvFile != null) {
                    String name = csvFile.getName().toUpperCase();
                    if (name.endsWith(".CSV") && name.length() >= 6) {
                        try {
                            Continent continent = Continent.valueOf(name.substring(0, name.length() - 4).trim());
                            setFromFileCSV(csvFile, continent, world);
                        } catch (EnumConstantNotPresentException e) {

                        }

                    }
                }
            }

        }
    }

    public static void setFromFileCSV(File file, Continent continent, World world) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            CountryArray cArr = world.getCountries();
            String line = br.readLine();
            String[] k = line.trim().split("\\s*,\\s*");
            if (k.length < 4)
                return;
            while ((line = br.readLine()) != null) {
                k = line.trim().split("\\s*,\\s*");
                short cId = (short) CountryArray.getIndexAdv(k[1]);
                continent.addCountry(cId);
                //can also get iso3
                if (cArr.containsKey(cId)) {
                    cArr.get(cId).addContinent(continent);
                    //and region

                }
            }
        } catch (IOException e) {
            ErrorLog.logError(e);
        }
    }

}
