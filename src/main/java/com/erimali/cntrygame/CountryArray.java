package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.*;

public class CountryArray implements Iterable<Country>, Serializable {
    private static final int maxISO2Countries = 26 * 26;
    private final Set<String> countriesISO2;
    private final Country[] countries;

    public static int getMaxIso2Countries() {
        return maxISO2Countries;
    }

    public CountryArray() {
        countries = new Country[maxISO2Countries];
        countriesISO2 = new TreeSet<>();
    }

    public static short getIndexShort(String iso2) {
        return (short) getIndex(iso2);
    }

    public static int getIndexOrInt(String iso2) {
        try {
            int index = Integer.parseInt(iso2);
            return index;
        } catch (NumberFormatException e) {
            return getIndex(iso2);
        }
    }

    public static int getIndex(String iso2) {
        return (Character.toUpperCase(iso2.charAt(0)) - 'A') * 26 + (Character.toUpperCase(iso2.charAt(1)) - 'A');
    }

    public static int getIndex(char c1, char c2) {
        return (Character.toUpperCase(c1) - 'A') * 26 + (Character.toUpperCase(c2) - 'A');
    }

    public static int getIndexAdv(String s) {
        int index = -1;
        for (int i = 0; i < s.length(); i++) {
            char c = Character.toUpperCase(s.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                if (index == -1) {
                    index = (c - 'A') * 26;
                } else {
                    index += c - 'A';
                    break;
                }
            }
        }
        return index;
    }

    public void put(String iso2, Country country) {
        int index = getIndex(iso2);
        if (index >= 0 && index < maxISO2Countries) {
            countries[index] = country;
            countriesISO2.add(iso2);
        }
    }

    public void put(char c1, char c2, Country country) {
        int index = getIndex(c1, c2);
        if (index >= 0 && index < maxISO2Countries) {
            countries[index] = country;
            countriesISO2.add("" + c1 + c2);
        }
    }

    public void put(int index, Country country) {
        if (index >= 0 && index < maxISO2Countries) {
            countries[index] = country;
            countriesISO2.add(getIndexISO2(index));
        }
    }

    public void addCountry(String iso2, Country country) {
        int index = getIndex(iso2);
        if (index >= 0 && index < maxISO2Countries && !containsKey(index)) {
            countries[index] = country;
            countriesISO2.add(iso2);
        }
    }

    public String addCountry(Country country) {
        String iso2 = genISO2unlikeSet(country.getName(), countriesISO2);
        country.setIso2(iso2);
        countries[getIndex(iso2)] = country;
        countriesISO2.add(iso2);
        return iso2;
    }

    public void remove(String iso2) {
        countries[getIndex(iso2)] = null;
        countriesISO2.remove(iso2);
    }

    public void remove(char c1, char c2) {
        countries[getIndex(c1, c2)] = null;
        countriesISO2.remove("" + c1 + c2);
    }

    public void remove(int i) {
        if (i >= 0 && i < maxISO2Countries) {
            countries[i] = null;
            countriesISO2.remove(getIndexISO2(i));
        }
    }

    public Country get(String iso2) {
        int index = getIndex(iso2);
        return get(index);
    }

    public Country get(char c1, char c2) {
        int index = getIndex(c1, c2);
        return get(index);
    }

    public Country get(int index) {
        if (index >= 0 && index < maxISO2Countries) {
            return countries[index];
        } else {
            return null;
        }
    }

    public boolean containsKey(String iso2) {
        int index = getIndex(iso2);
        return containsKey(index);
    }

    public boolean containsKey(char c1, char c2) {
        int index = getIndex(c1, c2);
        return containsKey(index);
    }

    public boolean containsKey(int index) {
        if (index >= 0 && index < maxISO2Countries)
            return countries[index] != null;
        else
            return false;
    }


    public static String getIndexISO2(int index) {
        char c2 = (char) ((index % 26) + 'A');
        index /= 26;
        char c1 = (char) ((index) + 'A');
        return "" + c1 + c2;
    }

    //ISO2 GENERATION FOR NEW COUNTRY
    public static String genISO2unlikeSet(String cName, Set<String> cList) {
        for (int i = 0; i < cName.length(); i++) {
            for (int j = i + 1; j < cName.length(); j++) {
                String iso2 = "" + cName.charAt(i) + cName.charAt(j);
                if (!cList.contains(iso2)) {
                    return iso2;
                }
            }
        }
        return genISO2alternative(cList);
    }

    public static String genISO2alternative(Set<String> cList) {
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
                String iso2 = "" + ('A' + i) + ('A' + j);
                if (!cList.contains(iso2)) {
                    return iso2;
                }
            }
        }
        return null;
    }

    public Set<String> getAllISO2() {
        return countriesISO2;
    }

    public static short[] getShortArrFromStringArr(String in) {
        int indexOf = in.indexOf(':');
        if (indexOf >= 0) {
            in = in.substring(indexOf + 1);
        }
        return getShortArrFromStringArr(in.trim().split("\\s*,\\s*"));
    }

    public static short[] getShortArrFromStringArr(String[] in) {
        if (in == null)
            return null;
        short[] arr = new short[in.length];
        int i = 0;
        for (String s : in) {
            if (s.length() > 1)
                arr[i++] = (short) getIndexAdv(s);
        }
        if (i < in.length)
            return Arrays.copyOf(arr, i);
        return arr;
    }

    public static Set<Short> getShortSetFromStringArr(String[] in) {
        Set<Short> set = new HashSet<>();
        int i = 0;
        for (String s : in) {
            if (s.length() > 1)
                set.add((short) getIndexAdv(s));
        }
        if (set.isEmpty())
            return null;
        return set;
    }


    @Override
    public Iterator<Country> iterator() {
        return new Iterator<>() {
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

            @Override
            public void remove() {
                CountryArray.this.remove(currentIndex);
            }
        };
    }
}
