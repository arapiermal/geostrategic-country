package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class World {
    private String name;
    private double totalLandArea;
    private static final String ENDDELIMITER = Syntax.END.getName();
    private List<Language> languages; // change (?)

    private CountryArray countries;
    //private Map<String, Country> countries;//only one to be left?
    private Map<String, Union> unions;
    private AdmDiv[] provinces;

    // AL -> Albania
    // DEFAULT DATA
    public World() {
        try {
            name = "Earth";
            totalLandArea = 148940000;
            countries = new CountryArray();
            Path dir = Paths.get(GLogic.RESOURCESPATH + "countries");
            loadLanguages();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path entry : stream) {
                    try {
                        String shortName = entry.getFileName().toString();
                        if (!shortName.endsWith(".txt"))
                            continue;
                        shortName = shortName.substring(0, shortName.length() - 4); // Removing '.txt'
                        Country country = countryFromFile(entry.toFile());
                        country.setIso2(shortName);
                        countries.put(shortName, country);
                    } catch (Exception e) {
                        ErrorLog.logError(e);
                    }
                }
            } catch (IOException e) {
                ErrorLog.logError(e);
            }
        } catch (Exception e) {
            ErrorLog.logError(e);
        }

    }

    public World(int type) {
        try {
            switch (type) {
                case 0:
                    break;
                case 1:
                    name = "Moon";
                    totalLandArea = 38e6;
                    break;
                case 2:
                    name = "Mars";
                    totalLandArea = 144.4e6;
                    break;
                default:

            }
            countries = new CountryArray();

        } catch (Exception e) {
            ErrorLog.logError(e);
        }
    }

    public void initiateProvinces(SVGProvince[] wmProvinces) {
        provinces = new AdmDiv[wmProvinces.length];
        //set after loading data for provinces in individual countries and after WorldMap
        for (int i = 0; i < wmProvinces.length; i++) {
            Country c = countries.get(wmProvinces[i].getOwnerId());
            if (c != null) {
                List<AdmDiv> countryAdmDivs = c.getAdmDivs();
                if (countryAdmDivs == null)
                    continue;
                for (AdmDiv a : countryAdmDivs) {
                    if (wmProvinces[i].getId().equalsIgnoreCase(a.getName())) {
                        a.setSvgProvince(wmProvinces[i]);
                        provinces[i] = a;
                        //TESTING.print(provinces[i],i);
                        //or opposite can be done (making array in World class unnecessary)
                        // AdmDiv inside SVGProvince?!?
                        break;
                    }
                }
            }
        }
    }

    public void loadLanguagesOld() {
        Map<String, Language> languages = new HashMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(GLogic.RESOURCESPATH + "countries/languages"))) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (!name.endsWith(".txt"))
                    continue;
                name = name.substring(0, name.length() - 4);
                try {
                    languages.put(name, new Language(name, true));
                } catch (Exception e) {
                    ErrorLog.logError(e.getMessage());
                }
            }

        } catch (IOException e) {
            ErrorLog.logError(e.getMessage());
        }
    }

    public void loadLanguages() {
        //REMOVING THE MAP<STRING,LANGUAGE>
        //Language[] languages;
        languages = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(GLogic.RESOURCESPATH + "countries/languages"))) {
            List<String> langList = new LinkedList<>();
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (!name.endsWith(".txt"))
                    continue;
                name = name.substring(0, name.length() - 4);
                langList.add(Language.upperFirstLowerRestLetters(name));

            }
            Collections.sort(langList);

            /*
            int i = 0;
            languages = new Language[langList.size()];
            for(String l : langList){
                try {
                    languages[i] = new Language(l,true);
                    i++;
                } catch (Exception e) {
                    ErrorLog.logError(e.getMessage());
                }
            }
             */
            for (String s : langList) {
                try {
                    Language l = new Language(s, true);
                    languages.add(l);
                } catch (Exception e) {
                    ErrorLog.logError(e);
                }
            }
        } catch (IOException e) {
            ErrorLog.logError(e.getMessage());
        }
    }

    public Country countryFromFile(File p) {
        try (BufferedReader br = new BufferedReader(new FileReader(p))) {
            String name = br.readLine();
            double area = Double.parseDouble(getVal(br.readLine()));
            long population = Long.parseLong(getVal(br.readLine()));
            boolean landlocked = Boolean.parseBoolean(getVal(br.readLine()));
            // Maybe City class
            String capital = getVal(br.readLine());
            String[] infoElectronic = getValues(br.readLine());
            String[] langNameArr = getValues(br.readLine());
            List<Short> languages = genLanguageIndexList(langNameArr);
            String[] neighbours = getValues(br.readLine());
            String admDivisionType = getVal(br.readLine());
            List<AdmDiv> admDivisions = admDivisionsFromFile(br, languages);


            br.readLine();
            Government government = governmentFromFile(br);
            br.readLine();
            Economy economy = economyFromFile(br);
            br.readLine();
            Military military = militaryFromFile(br);
            return new Country(name, population, area, landlocked, capital, infoElectronic, admDivisionType,
                    admDivisions, languages, neighbours, government, economy, military);
        } catch (Exception e) {
            return null;
        }
    }

    public Government governmentFromFile(BufferedReader br) {
        try {
            String type = getVal(br.readLine());
            String l = br.readLine().trim();
            if (l.toLowerCase().startsWith("single")) {
                String[] r = getValues(l);
                Ruler ruler = new Ruler(r[0], r[1], r[2], r[3].charAt(0));
                return new Government(type, ruler);
            } else {
                String[] r1 = getValues(l);
                Ruler headOfState = new Ruler(r1[0], r1[1], r1[2], r1[3].charAt(0));
                String[] r2 = getValues(br.readLine());
                Ruler headOfGovernment = new Ruler(r2[0], r2[1], r2[2], r2[3].charAt(0));
                return new Government(type, headOfState, headOfGovernment);
            }

        } catch (Exception e) {
            return null; // new Government() empty??
        }
    }

    public Economy economyFromFile(BufferedReader br) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private Military militaryFromFile(BufferedReader br) {
        try {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /////////////////////////////////////
/////////////////////////////////////
/////////////////////////////////////
/////////////////////////////////////
	/*
	public static String getVal(String s) {
		int startIndex = s.indexOf(":") + 1;
		return s.substring(startIndex).trim();
	}
	*/
    public static String getVal(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) != ':') {
            i++;
        }
        i++;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
        int end = s.length() - 1;
        while (end > i && Character.isWhitespace(s.charAt(end))) {
            end--;
        }
        return s.substring(i, end + 1);
    }

    public static String[] getValues(String s) {
        return Arrays.stream(s.substring(s.indexOf(":") + 1).split(",")).map(String::trim).toArray(String[]::new);
    }

    public static String[] getValues2(String s) {
        return Arrays.stream(s.split(",")).map(String::trim).toArray(String[]::new);
    }

    public static String[] getValues3(String s) {
        return s.trim().split("\\s+,\\s+");
    }

    public List<Short> genLanguageIndexList(String... langs) {
        List<Short> indexes = new ArrayList<>();
        for (String lang : langs) {
            short r = (short) binarySearchLanguage(lang);
            if (r < 0) {
                short sh = (short) addLangugage(new Language(lang));
                indexes.add(sh);
            } else {
                indexes.add(r);
                TESTING.print(r);
            }
        }
        TESTING.print(indexes);
        return indexes;
    }

    //If individual files, needs BIG CHANGES
    public List<AdmDiv> admDivisionsFromFile(BufferedReader br, List<Short> indexLangs) {
        try {
            List<AdmDiv> list = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith(ENDDELIMITER)) {
                    break;
                }
                String[] vals = getValues2(line);
                if (vals.length == 3) {
                    list.add(new AdmDiv(vals[0], vals[1], vals[2], indexLangs.getFirst()));
                } else if (vals.length == 4) {
                    list.add(new AdmDiv(vals[0], vals[1], vals[2], indexLangs.get(GUtils.parseI(vals[3]))));
                }
            }
            //FOR BINARY SEARCH ? list.sort(...);
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // LOAD RESOURCES/COUNTRIES
    // FILENAME -> String key
    // INSIDE -> Country data
    //
    public Country getCountry(String ISO2) {
        return countries.get(ISO2);
    }

    public Country getCountry(int id) {
        return countries.get(id);
    }

    public CountryArray getCountries() {
        return countries;
    }


    public void subjugateCountry(String cn1, String cn2, int type) {
        Country c1 = countries.get(cn1);
        Country c2 = countries.get(cn2);
        c1.subjugateCountry(c2, type);
    }

    public int addLangugage(Language l) {
        if (!languages.contains(l)) {
            // Find the correct index to insert the new language
            int i = 0;
            while (i < languages.size() && languages.get(i).compareTo(l) < 0) {
                i++;
            }
            languages.add(i, l);
            return i;
        }
        return -1;
    }

    public boolean removeLangugage(Language l) {
        return languages.remove(l);
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public Map<String, Union> getUnions() {
        return unions;
    }

    public void setUnions(Map<String, Union> unions) {
        this.unions = unions;
    }


    public Person langGenerateMale(int i) {
        return languages.get(i).generateMale();
    }

    // [Albanian:John],[Albanian],birthday?? now+rand/now+rand/now - rand min30
    // max50
    // replace THIS: with actual language?
    /*public String randLangName(String input) {
        input = input.replaceAll("\\s+", "");
		if (input.contains(":")) {
			String[] parts = input.split(":");
			if(parts[1].toUpperCase().equals("SELF")) {
				//parts[1] = player;
				// replace all self with playerName in command??
			}
			return languages.get(parts[0]).translateNameFromEnglish(parts[1]);
		} else {
		}
        return languages.get(input).generateMName();
    }*/

    public String randLangSurname(int i) {
        return languages.get(i).generateSurname();
    }

    public int binarySearchLanguage(String s) {
        return Collections.binarySearch(languages, new Language(s));
    }
    public int binarySearchLanguage(Language l) {
        return Collections.binarySearch(languages, l);
    }
    public double getTotalLandArea() {
        return totalLandArea;
    }

    public void setTotalLandArea(double totalLandArea) {
        this.totalLandArea = totalLandArea;
    }

    public double calcTotalUsedLandArea() {
        double usedArea = 0;
        for (Country cn : countries) {
            usedArea += cn.getArea();
        }
        return usedArea;
    }

    public AdmDiv getAdmDiv(int id) {
        if (id >= 0 && id < provinces.length)
            return provinces[id];
        else
            return null;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getProvInfo(int selectedProv) {
        if (provinces[selectedProv] == null)
            return "NO DATA";
        return provinces[selectedProv].toStringLong();
    }
}
