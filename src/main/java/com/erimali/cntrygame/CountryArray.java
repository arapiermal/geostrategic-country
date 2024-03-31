package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.*;

public class CountryArray implements Iterable<Country>, Serializable {
    protected static final int maxISO2Countries = 26 * 26;
    private final Set<String> countriesISO2;
    private final Country[] countries;
    public CountryArray() {
        countries = new Country[maxISO2Countries];

        countriesISO2 = new TreeSet<>();
    }
    public static short getIndexShort(String iso2){
        return (short) ((iso2.charAt(0) - 'A') * 26 + (iso2.charAt(1) - 'A'));
    }

    public static int getIndex(String iso2){
        return (iso2.charAt(0) - 'A') * 26 + (iso2.charAt(1) - 'A');
    }
    public static int getIndex(char c1, char c2){
        return (c1 - 'A') * 26 + (c2 - 'A');
    }
    public void put(String iso2, Country country){
        countries[getIndex(iso2)] = country;
        countriesISO2.add(iso2);
    }


    public void addCountry(String iso2, Country country) {
        int index = getIndex(iso2);
        if(countries[index] == null) {
            countries[index] = country;
            countriesISO2.add(iso2);
        }
    }

    public String addCountry(Country country){
        String iso2 = genISO2unlikeSet(country.getName(), countriesISO2);
        country.setIso2(iso2);
        countries[getIndex(iso2)] = country;
        countriesISO2.add(iso2);
        return iso2;
    }

    public void remove(char c1, char c2){
        countries[getIndex(c1,c2)] = null;
        countriesISO2.remove(""+c1+c2);
    }
    public void remove(String iso2){
        countries[getIndex(iso2)] = null;
        countriesISO2.remove(iso2);
    }

    public void remove(int i){
        countries[i] = null;
        countriesISO2.remove(getIndexISO2(i));
    }
    public Country get(char c1, char c2){
        int index =getIndex(c1,c2);
        if(index >= 0 && index < maxISO2Countries)
            return countries[index];
        else
            return null;
    }
    public Country get(String iso2) {
        int index = getIndex(iso2);
        if(index >= 0 && index < maxISO2Countries)
            return countries[index];
        else
            return null;
    }
    public Country get(int index){
        if(index >= 0 && index < maxISO2Countries){
            return countries[index];
        } else{
            return null;
        }
    }
    public boolean containsKey(int iso2){
        return countries[iso2] != null;
    }

    public boolean containsKey(short iso2){
        return countries[iso2] != null;
    }
    public boolean containsKey(String iso2){
        int index = getIndex(iso2);
        if(index >= 0 && index < maxISO2Countries)
            return countries[index] != null;
        else
            return false;
    }
    @Override
    public Iterator<Country> iterator() {
        return new Iterator<Country>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                while (currentIndex < maxISO2Countries && countries[currentIndex] == null) {
                    currentIndex++;
                }
                return currentIndex < maxISO2Countries;
            }

            @Override
            public Country next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return countries[currentIndex++];
            }
        };
    }


    public static String getIndexISO2(int index){
        char c2 = (char) ((index % 26) + 'A');
        index /= 26;
        char c1 = (char) ((index) + 'A');
        return "" + c1 + c2;
    }


    //ISO2 GENERATION FOR NEW COUNTRY
    public static String genISO2unlikeSet(String cName, Set<String> cList){
        for(int i = 0; i < cName.length();i++){
            for(int j = i+1; j < cName.length(); j++){
                String iso2 = "" + cName.charAt(i) + cName.charAt(j);
                if(!cList.contains(iso2)){
                    return iso2;
                }
            }
        }
        return genISO2alternative(cList);
    }
    public static String genISO2alternative(Set<String> cList){
        for(int i = 0; i < 26;i++){
            for(int j = 0; j < 26; j++){
                String iso2 = "" + ('A'+i) + ('A'+j);
                if(!cList.contains(iso2)){
                    return iso2;
                }
            }
        }
        return null;
    }


    public Set<String> getAllISO2(){
        return countriesISO2;
    }


}
