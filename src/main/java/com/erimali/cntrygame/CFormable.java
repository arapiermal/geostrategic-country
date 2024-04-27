package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class CFormable {
    public static class FirstAdmDivs {
        public short[][] firstProvinces;

        public FirstAdmDivs(CountryArray cArray) {
            firstProvinces = new short[CountryArray.getMaxIso2Countries()][];
            setFirstProvinces(cArray);
        }

        public boolean ownsAllISO2(AdmDiv[] admDivs, Country c, String... o) {
            for (String s : o) {
                short i = (short) CountryArray.getIndexAdv(s);
                if (!owns(admDivs, c, i))
                    return false;
            }
            return true;
        }

        public boolean ownsAll(AdmDiv[] admDivs, Country c, short... o) {
            for (short i : o)
                if (!owns(admDivs, c, i))
                    return false;
            return true;
        }

        public boolean owns(AdmDiv[] admDivs, Country c, short o) {
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
                short i = (short) CountryArray.getIndexAdv(s);
                if (!ownsOrHasSubject(admDivs, c, i))
                    return false;
            }
            return true;
        }

        public boolean ownsOrHasSubjectsAll(AdmDiv[] admDivs, Country c, short... o) {
            for (short i : o)
                if (!ownsOrHasSubject(admDivs, c, i))
                    return false;
            return true;
        }

        public boolean ownsOrHasSubject(AdmDiv[] admDivs, Country c, short o) {
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

    public static short[] stringArrToShortArr(String[] s) {
        if(s == null)
            return null;
        short[] arr = new short[s.length];
        int i = 0;
        for (int j = 0; j < s.length; j++) {
            try {
                arr[i] = Short.parseShort(s[j]);
                i++;
            } catch (NumberFormatException nfe) {

            }
        }
        if (i < arr.length)
            return Arrays.copyOf(arr, i);
        return arr;
    }

    //Releasable for liberation vs Form-able through unification
    //Form Greater Albania !!
    private String name;
    private String desc;
    private List<String> commandsOnComplete;
    private Set<Short> formableBy;
    private short[] reqCountries;
    private short[] reqProvinces; //required

    public CFormable(String name, String desc, Set<Short> formableBy, short[] reqCountries, short[] reqProvinces, List<String> commandsOnComplete) {
        this.name = name;
        this.desc = desc;
        this.formableBy = formableBy;
        this.reqCountries = reqCountries;
        this.reqProvinces = reqProvinces;
        this.commandsOnComplete = commandsOnComplete;
    }

    public CFormable(String name, String desc, String[] formableBy, String[] reqCountries, String[] reqProvinces, List<String> commandsOnComplete) {
        this.name = name;
        this.desc = desc;
        this.formableBy = CountryArray.getShortSetFromStringArr(formableBy);
        this.reqCountries = CountryArray.getShortArrFromStringArr(reqCountries);
        this.reqProvinces = stringArrToShortArr(reqProvinces);
        this.commandsOnComplete = commandsOnComplete;
    }

    public boolean isAvailable(short cId) {
        if (formableBy == null)
            return true;
        return formableBy.contains(cId);
    }

    private void executeAllCommands() {
        if (commandsOnComplete != null)
            CommandLine.executeAll(commandsOnComplete);
    }

    private void formCountry(FirstAdmDivs first, Country c, AdmDiv[] provinces, boolean ownsOrSubject) {
        if (hasRequiredProvinces(provinces, c.getCountryId()) && hasRequiredCountries(first, provinces, c, ownsOrSubject)) {
            c.setName(name);
            executeAllCommands();
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

    @Override
    public String toString() {
        return name;
    }
    public String toStringLong() {
        return name + "\n" + desc;
    }

    public static List<CFormable> loadFormables(String path) {
        List<CFormable> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                CFormable formable = loadFormable(line.trim(), br);
                if (formable != null)
                    list.add(formable);
            }
            return list;
        } catch (IOException e) {
            ErrorLog.logError(e);
            return list;
        }
    }

    public static CFormable loadFormable(String name, BufferedReader br) {
        try {
            String desc = br.readLine().trim();
            String line;
            line = br.readLine().trim();
            char ch = line.charAt(0);
            int indCol = line.indexOf(':');
            int dI = 0;
            String[][] data = new String[3][];
            while ((line = br.readLine()) != null && !line.contains("~~") && dI < 3) {
                if (!(line = line.trim()).isBlank()) {
                    data[getDataIndex(ch)] = getValuesFromLine(line, indCol);
                    dI++;
                }
            }
            List<String> commands = new LinkedList<>();
            while ((line = br.readLine()) != null && !line.contains("~~~")) {
                if (!(line = line.trim()).isBlank())
                    commands.add(line);
            }
            return new CFormable(name, desc, data[0], data[1], data[2], commands);
        } catch (IOException e) {
            ErrorLog.logError(e);
            return null;
        }

    }

    public static int getDataIndex(char c) {
        return switch (Character.toUpperCase(c)) {
            case 'F' -> 0;
            case 'C' -> 1;
            case 'P' -> 2;
            default -> -1;
        };
    }

    public static String[] getValuesFromLine(String line, int indCol) {
        return line.substring(indCol + 1).trim().split("\\s*,\\s*");
    }

    //Events based on formables
    public static GEvent loadGEvent(BufferedReader br) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        List<CFormable> formables = loadFormables(GLogic.RESOURCESPATH + "data/formables.txt");
        for(CFormable c : formables){
            TESTING.print(c.toStringLong(), c.commandsOnComplete);
        }
    }
}
