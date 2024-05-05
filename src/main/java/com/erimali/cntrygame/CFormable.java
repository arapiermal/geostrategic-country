package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class CFormable {

    public static class FirstAdmDivs {
        private final short[][] firstProvinces;
        //private final EnumMap<Continent, Set<Short>> firstCountryContinents;

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

        public boolean ownsAtLeastOneISO2(AdmDiv[] admDivs, Country c, String... o) {
            for (String s : o) {
                short i = (short) CountryArray.getIndexAdv(s);
                if (owns(admDivs, c, i))
                    return true;
            }
            return false;
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
                return true;//!
            int cId = c.getCountryId();
            for (short s : oProvs) {
                AdmDiv a = admDivs[s];
                if (a == null)
                    continue;
                if (a.getOwnerId() != cId) {
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
                return true;//! if it doesn't exist
            int cId = c.getCountryId();
            Set<Integer> subjects = c.getSubjects().keySet();
            for (short s : oProvs) {
                AdmDiv a = admDivs[s];
                if (a == null)
                    continue;
                int admOwnerId = a.getOwnerId();
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
                    short provId = (short) admDiv.getProvId();
                    firstProvinces[cId][i++] = provId;
                }
            }
        }

        public boolean hasOriginalCountryInProvinces(int i) {
            return firstProvinces[i] != null;
        }
    }

    public static short[] stringArrToShortArr(String[] in) {
        if (in == null)
            return null;
        short[] arr = new short[in.length];
        int i = 0;
        for (String string : in) {
            try {
                arr[i] = Short.parseShort(string);
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
    private boolean evenSubjects;
    private List<String> commandsOnComplete;
    private Set<Short> formableBy;
    private short[] reqCountries;
    private short[] reqProvinces; //required

    public CFormable(String name, boolean evenSubjects, String desc, Set<Short> formableBy, short[] reqCountries, short[] reqProvinces, List<String> commandsOnComplete) {
        this.name = name;
        this.desc = desc;
        this.formableBy = formableBy;
        this.reqCountries = reqCountries;
        this.reqProvinces = reqProvinces;
        this.commandsOnComplete = commandsOnComplete;
    }

    public CFormable(String name, boolean evenSubjects, String desc, String[] formableBy, String[] reqCountries, String[] reqProvinces, List<String> commandsOnComplete) {
        this.name = name;
        this.evenSubjects = evenSubjects;
        this.desc = desc;
        this.formableBy = CountryArray.getShortSetFromStringArr(formableBy);
        this.reqCountries = CountryArray.getShortArrFromStringArr(reqCountries);
        this.reqProvinces = stringArrToShortArr(reqProvinces);
        //TESTING.print(formableBy, Arrays.toString(reqCountries), Arrays.toString(reqProvinces));
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

    public boolean formCountry(World world, Country c) {
        return formCountry(world.getInitialProvinces(), world.getProvinces(), c);
    }

    public boolean formCountry(FirstAdmDivs first, AdmDiv[] provinces, Country c) {
        if (hasRequiredProvinces(provinces, c.getCountryId()) && hasRequiredCountries(first, provinces, c)) {
            c.setName(name);
            executeAllCommands();
            return true;
        }
        return false;
    }

    public boolean hasRequiredCountries(FirstAdmDivs first, AdmDiv[] provinces, Country c) {
        if (reqCountries == null)
            return true;
        if (evenSubjects)
            return first.ownsOrHasSubjectsAll(provinces, c, reqCountries);
        else
            return first.ownsAll(provinces, c, reqCountries);
    }

    public boolean hasRequiredProvinces(AdmDiv[] provinces, int countryId) {
        if (reqProvinces == null)
            return true;
        for (int reqProv : reqProvinces) {
            AdmDiv a = provinces[reqProv];
            if(a == null)
                continue;
            if (a.getOwnerId() != countryId)
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

    //the part with countries would only work if caching the string
    public String toStringRequirements(AdmDiv[] provinces, CountryArray cArr) {
        StringBuilder sb = new StringBuilder();
        if (reqCountries != null) {
            sb.append("Countries required to own").append(evenSubjects ? " or have as subjects: " : ": ").append("\n");
            for (int i = 0; i < reqCountries.length - 1; i++) {
                Country c = cArr.get(reqCountries[i]);
                if (c != null)
                    sb.append(c.getName()).append(", ");
            }
            Country c = cArr.get(reqCountries.length - 1);
            if (c != null)
                sb.append(c.getName());
            sb.append('\n');
        }
        if (reqProvinces != null) {
            sb.append("Provinces required: ").append("\n");
            for (int i = 0; i < reqProvinces.length - 1; i++) {
                AdmDiv a = provinces[reqProvinces[i]];
                if (a != null)
                    sb.append(a.getName()).append(", ");
            }
            AdmDiv a = provinces[reqProvinces[reqProvinces.length - 1]];
            if (a != null)
                sb.append(a.getName());
        }
        return sb.toString();
    }

    public String toStringRequirements(AdmDiv[] provinces, FirstAdmDivs first) {
        StringBuilder sb = new StringBuilder();
        if (reqCountries != null) {
            sb.append("Countries required to own").append(evenSubjects ? " or have as subjects: " : ": ").append("\n");
            for (int i = 0; i < reqCountries.length - 1; i++) {
                if (first.hasOriginalCountryInProvinces(reqCountries[i]))
                    sb.append(CountryArray.getIndexISO2(reqCountries[i])).append(", ");
            }
            int temp = reqCountries[reqCountries.length - 1];
            if (first.hasOriginalCountryInProvinces(temp))
                sb.append(CountryArray.getIndexISO2(temp));
            sb.append('\n');
        }
        if (reqProvinces != null) {
            sb.append("Provinces required: ").append("\n");
            for (int i = 0; i < reqProvinces.length - 1; i++) {
                AdmDiv a = provinces[reqProvinces[i]];
                if (a != null)
                    sb.append(a.getName()).append(", ");
            }
            AdmDiv a = provinces[reqProvinces[reqProvinces.length - 1]];
            if (a != null)
                sb.append(a.getName());
        }
        return sb.toString();
    }

    public String toStringCommands() {
        StringBuilder sb = new StringBuilder();
        for (String c : commandsOnComplete) {
            sb.append(c).append("\n");
        }
        return sb.toString();
    }


    public static List<CFormable> loadFormables(String path) {
        List<CFormable> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;
                try {
                    String[] nameType = line.trim().split("\\s*:\\s*");
                    boolean evenSubjects = false;
                    if (nameType.length >= 2)
                        evenSubjects = Boolean.parseBoolean(nameType[1]);
                    CFormable formable = loadFormable(nameType[0], evenSubjects, br);
                    if (formable != null)
                        list.add(formable);
                } catch (Exception e) {
                    ErrorLog.logError(e);
                }
            }
            return list;
        } catch (IOException e) {
            ErrorLog.logError(e);
            return list;
        }
    }

    public static CFormable loadFormable(String name, boolean evenSubjects, BufferedReader br) {
        try {
            String desc = br.readLine().trim();
            String line;
            int dI = 0;
            String[][] data = new String[3][];
            while ((line = br.readLine()) != null && !line.contains("~~") && dI < 3) {
                if (!(line = line.trim()).isBlank()) {
                    char ch = line.charAt(0);
                    data[getDataIndex(ch)] = getValuesFromLine(line, line.indexOf(':'));
                    dI++;
                }
            }
            List<String> commands = new LinkedList<>();
            while ((line = br.readLine()) != null && !line.contains("~~~")) {
                if (!(line = line.trim()).isBlank())
                    commands.add(line);
            }
            return new CFormable(name, evenSubjects, desc, data[0], data[1], data[2], commands);
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

}
