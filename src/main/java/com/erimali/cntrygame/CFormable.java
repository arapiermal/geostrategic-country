package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class CFormable {
    public static class FirstAdmDivs {
        public short[][] firstProvinces;

        public FirstAdmDivs(CountryArray cArray) {
            firstProvinces = new short[CountryArray.getMaxIso2Countries()][];
            setFirstProvinces(cArray);
        }

        public boolean ownsAllISO2(AdmDiv[] admDivs, Country c, String... o) {
            for (String s : o) {
                int i = CountryArray.getIndexAdv(s);
                if (!owns(admDivs, c, i))
                    return false;
            }
            return true;
        }

        public boolean ownsAll(AdmDiv[] admDivs, Country c, int... o) {
            for (int i : o)
                if (!owns(admDivs, c, i))
                    return false;
            return true;
        }

        public boolean owns(AdmDiv[] admDivs, Country c, int o) {
            short[] oProvs = firstProvinces[o];
            if (oProvs == null)
                return false;
            int cId = c.getCountryId();
            for (short s : oProvs) {
                if (admDivs[s].getOwnerId() != cId) {
                    return false;
                }
            }
            return true;
        }

        public boolean ownsOrHasSubjectsAllISO2(AdmDiv[] admDivs, Country c, String... o) {
            for (String s : o) {
                int i = CountryArray.getIndexAdv(s);
                if (!ownsOrHasSubject(admDivs, c, i))
                    return false;
            }
            return true;
        }

        public boolean ownsOrHasSubjectsAll(AdmDiv[] admDivs, Country c, int... o) {
            for (int i : o)
                if (!ownsOrHasSubject(admDivs, c, i))
                    return false;
            return true;
        }

        public boolean ownsOrHasSubject(AdmDiv[] admDivs, Country c, int o) {
            short[] oProvs = firstProvinces[o];
            if (oProvs == null)
                return false;
            int cId = c.getCountryId();
            Set<Integer> subjects = c.getSubjects().keySet();
            for (short s : oProvs) {
                int admOwnerId = admDivs[s].getOwnerId();
                if (admOwnerId != cId && !subjects.contains(admOwnerId)) {
                    return false;
                }
            }
            return true;
        }

        public void setFirstProvinces(CountryArray cArray) {
            for (Country c : cArray) {
                int cId = c.getCountryId();
                firstProvinces[cId] = new short[c.getAdmDivs().size()];
                int i = 0;
                for (AdmDiv admDiv : c.getAdmDivs()) {
                    firstProvinces[cId][i++] = (short) admDiv.getProvId();
                }
            }
        }
    }

    //Releasable for liberation vs Form-able through unification
    //Form Greater Albania !!
    private String name;
    private int[] reqCountries;
    private int[] reqProvinces; //required

    public CFormable(String name, int[] reqCountries, int[] reqProvinces) {
        this.name = name;
        this.reqCountries = reqCountries;
        this.reqProvinces = reqProvinces;
    }

    private void formCountry(FirstAdmDivs first, Country c, AdmDiv[] provinces, boolean ownsOrSubject) {
        if (hasRequiredProvinces(provinces, c.getCountryId()) && hasRequiredCountries(first, provinces, c, ownsOrSubject)) {
            c.setName(name);

        }
    }

    private boolean hasRequiredCountries(FirstAdmDivs first, AdmDiv[] provinces, Country c, boolean ownsOrSubject) {
        if (reqCountries == null)
            return true;
        if (ownsOrSubject)
            return first.ownsOrHasSubjectsAll(provinces, c, reqCountries);
        else
            return first.ownsAll(provinces, c, reqCountries);
    }

    public boolean hasRequiredProvinces(AdmDiv[] provinces, int countryId) {
        if (reqProvinces == null)
            return true;
        for (int reqProv : reqProvinces) {
            if (provinces[reqProv].getOwnerId() != countryId)
                return false;
        }
        return true;
    }


    public static List<CFormable> loadFormables(String path) {
        List<CFormable> l = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {

            }
            return l;
        } catch (IOException e) {
            ErrorLog.logError(e);
            return l;
        }
    }

    //Events based on formables
    public static GEvent loadGEvent(BufferedReader br) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
