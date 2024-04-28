package com.erimali.cntrygame;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public enum Continent {
    AF("Africa"), AS("Asia"), EU("Europe"), NA("North America"), SA("South America"), OC("Oceania"), AQ("Antarctica");

    Continent(String longName) {
        this.longName = longName;
        this.countries = new HashSet<>();
    }

    private final String longName;
    private final Set<Short> countries;
    private void addCountry(short id){
        countries.add(id);
    }
    private void addCountry(String id){
        countries.add(CountryArray.getIndexShort(id));
    }
    private void addCountry(int id){
        countries.add((short) id);
    }
    public Set<Short> getCountries(){
        return countries;
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
                    TESTING.print(name);
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
            if(k.length < 4)
                return;
            while ((line = br.readLine()) != null) {
                k = line.trim().split("\\s*,\\s*");
                continent.addCountry(k[1]);
                //can also get iso3
                if(cArr.containsKey(k[1])){
                    cArr.get(k[1]).addContinent(continent);
                    TESTING.print(k[1], continent);
                    //and region
                }
            }
        } catch (IOException e) {
            ErrorLog.logError(e);
        }
    }
}
