package com.erimali.cntrygame;

import com.erimali.cntrymilitary.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class World implements Serializable {
    private GLogic game;
    private String name;
    private double totalLandArea;
    private static final String ENDDELIMITER = "kaq";
    private List<Language> languages; // change (?)

    private CountryArray countries;
    //private Map<String, Country> countries;//only one to be left?
    private ObservableMap<String, Union> unions;
    private AdmDiv[] provinces;
    //set from world map
    private short[][] initialProvinces;
    //private List<Resource> resources;

    // AL -> Albania
    // DEFAULT DATA
    public World(GLogic game) {
        this.game = game;
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
                        shortName = shortName.substring(0, shortName.length() - 4).trim().toUpperCase(); // Removing '.txt'
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
            //this.unions = new HashMap<>();
            this.unions = FXCollections.observableMap(new HashMap<>());
            loadUnions();
        } catch (Exception e) {
            ErrorLog.logError(e);
        }

    }

    public World(GLogic game, int type) {
        this.game = game;
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

    public void correlateProvinces(SVGProvince[] wmProvinces) {
        for (Country c : countries) {
            for (AdmDiv admDiv : c.getAdmDivs()) {
                int provId = admDiv.getProvId();
                int ownerId = admDiv.getOwnerId();
                if (wmProvinces[provId].getOwnerId() != ownerId) {
                    wmProvinces[provId].setOwnerId(admDiv.getOwnerId());
                    admDiv.setSvgProvince(wmProvinces[provId]);
                    //update map ...
                }
            }
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
                    if (a.sameName(wmProvinces[i].getId())) {
                        //CHANGED!!!
                        a.setFromSVGProvince(wmProvinces[i]);
                        provinces[i] = a;

                        break;
                    }
                }
            }
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
            double populationGrowthRate = Double.parseDouble(getVal(br.readLine()));
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
            return new Country(name, area, population, populationGrowthRate, landlocked, capital, infoElectronic, admDivisionType,
                    admDivisions, languages, neighbours, government, economy, military);
        } catch (Exception e) {
            e.printStackTrace();
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
            String[][] data = new String[4][];
            int i = 0;
            String line;
            while((line = br.readLine()) != null && i < 4){
                data[i++] = getValues2(line);
            }
            return new Economy(data);
        } catch (Exception e) {
            return null; //assume new economy based on population and area (?)
        }
    }

    private Military militaryFromFile(BufferedReader br) {
        try {
            return new Military();
        } catch (Exception e) {
            return new Military();
        }
    }

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

    public static String[] getValues2(String s, String separator) {
        return Arrays.stream(s.split(separator)).map(String::trim).toArray(String[]::new);
    }

    public static String[] getValues3(String s) {
        return s.trim().split("\\s+,\\s+");
    }

    public List<Short> genLanguageIndexList(String... langs) {
        List<Short> indexes = new ArrayList<>();
        for (String lang : langs) {
            short r = (short) binarySearchLanguage(lang);
            if (r < 0) {
                //Problem: the indexes have to be updated (?)
                short sh = (short) addLangugage(new Language(lang));
                indexes.add(sh);
            } else {
                indexes.add(r);
            }
        }
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
                String[][] vals = getValuesArrArr(line);
                try {
                    AdmDiv admDiv = null;
                    if (vals.length >= 2) {
                        if(vals[1].length == 2){
                            admDiv = new AdmDiv(vals[0][0], vals[1][0], vals[1][1], indexLangs.getFirst());
                        }else if (vals[1].length == 3) {
                            admDiv = new AdmDiv(vals[0][0], vals[1][0], vals[1][1], indexLangs.get(GUtils.parseI(vals[1][2])));
                        }
                        if(admDiv != null) {
                            if (vals[0].length == 2)
                                admDiv.setNativeName(vals[0][1]);
                            list.add(admDiv);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //FOR BINARY SEARCH ? list.sort(...);
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String[][] getValuesArrArr(String line) {
        String[] toSplit = line.split(":");
        String[][] res = new String[toSplit.length][];
        for(int i = 0; i < toSplit.length; i++){
            res[i] = getValues2(toSplit[i]);
        }
        return res;
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


    public void subjugateCountry(int ind1, int ind2, SubjectType type) {
        Country c1 = countries.get(ind1);
        Country c2 = countries.get(ind2);
        if (c1 != null && c2 != null && c2.isNotSubject()) {
            c1.subjugateCountry(c2, type);
        }
    }

    //PROBLEM UPDATING INDEXES...
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

    public ObservableMap<String, Union> getUnions() {
        return unions;
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

    public Language searchLanguage(String name) {
        return languages.get(binarySearchLanguage(name));
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
        AdmDiv prov = provinces[selectedProv];
        if (prov == null)
            return "NO DATA";
        return prov.toStringBuilderLong().append("\nMain language: ").append(languages.get(prov.getMainLanguage())).toString();
    }

    public void monthlyUpdate() {
        //keep track with set for less overhead
        for (AdmDiv a : provinces) {
            if (a != null) {
                a.buildingTick();
            }
        }
    }

    public void yearlyUpdate() {
        for (Country c : countries) {
            c.yearlyTick();
        }
        for (AdmDiv a : provinces) {
            if (a != null) {
                a.yearlyTick();
            }
        }
    }


    //Check if valid...
    public void buildBuilding(int provId, Building b) {
        if (provId >= 0 && provId < provinces.length) {
            AdmDiv a = provinces[provId];
            if (a != null) {
                a.buildBuilding(b);
            }
        }
    }

    public void addUnion(String shortName, String name, String type, String countries) {
        shortName = shortName.replace("\\s", "").toUpperCase();

        int t = Union.genType(type);
        short[] c = CountryArray.getShortArrFromStringArr(countries);
        Union u = new Union(this, shortName, name, t, c);
        unions.put(shortName, u);
    }

    public void loadUnions() {
        if (unions == null)
            unions = FXCollections.observableMap(new HashMap<>());
        Path dir = Paths.get(GLogic.RESOURCESPATH + "/countries/unions");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                try {
                    String shortName = entry.getFileName().toString();
                    if (!shortName.endsWith(".txt"))
                        continue;
                    shortName = shortName.substring(0, shortName.length() - 4).trim().toUpperCase();
                    unionFromFile(shortName, entry.toFile());
                } catch (Exception e) {
                    ErrorLog.logError(e);
                }
            }
        } catch (IOException e) {
            ErrorLog.logError(e);
        }
    }

    private void unionFromFile(String shortName, File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String name = br.readLine();
            int t = Union.genType(br.readLine());
            short[] c = CountryArray.getShortArrFromStringArr(br.readLine());
            Union u = new Union(this, shortName, name, t, c);
            unions.put(shortName, u);
        }
    }

    public void addUnion(Union u) {
        unions.put(u.getShortName(), u);
    }

    public boolean removeUnion(String shortName) {
        Union u = unions.remove(shortName);
        if(u == null)
            return false;
        for (int i : u.getUnionCountries()) {
            Country c = countries.get(i);
            if (c != null) {
                c.removeUnion(u);
            }
        }
        return true;
    }

    public void removeUnion(String shortName, int c) {
        unions.get(shortName).dismantle(c);
    }

    public void makeMilUnit(int ownerId, int provId, MilUnitData data, int size) {
        if (size <= 0) {
            MilUnit u = data.isVehicle() ? new MilVehicles(data, ownerId) : new MilSoldiers(data, ownerId);
            provinces[provId].setUnitRecruitingBuild(u);

        } else {
            MilUnit u = data.isVehicle() ? new MilVehicles(data, ownerId) : new MilSoldiers(data, ownerId);
            u.incSize(size);
            provinces[provId].addUnit(u);
        }
    }

    public void recruitBuildMilUnit(MilUnit unit, int provId) {
        provinces[provId].setUnitRecruitingBuild(unit);

    }

    public void weeklyMilTick(Set<Integer> activeRecruitingBuildProv) {
        for (int provId : activeRecruitingBuildProv) {
            if (provinces[provId].recruitBuild() > 0) {
                //take care of extra manpower/resources (!)
                game.contMilUnit(provId);

            } else {
                //FIX!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // or maybe when finished recruiting/building show
                // if units.size() of province > 0 show milimg
                //friendly dependent on owner id
                //worldMap.makeMilSVG(provinces[provId].getUnitRecruitingBuild().getType(), provId, 0);

            }
        }

    }

    public void proceduralCountryMakeover() {
        SVGProvince[] svgMap = game.getWorldMap().getMapSVG();
        double mapW = game.getWorldMap().getWidth();
        double mapH = game.getWorldMap().getHeight();
        int worldPopDensity = 16;
        for (SVGProvince svg : svgMap) {
            int o = svg.getOwnerId();
            int p = svg.getProvId();
            if (!countries.containsKey(o)) {
                String iso2 = CountryArray.getIndexISO2(o);
                Country c = new Country(iso2);
                c.setIso2(iso2);
                //either no lang or english as default
                countries.put(o, c);
            }
            if (provinces[p] == null) {
                double area = svg.calcAvgArea(mapW, mapH);
                provinces[p] = new AdmDiv(svg.getId(), area, (int) (area * worldPopDensity), true, (short) -1);
                countries.get(o).addAdmDiv(provinces[p]);
            }
        }
    }


    public Union getUnion(String s) {
        return unions.get(s);
    }

    public void moveMilUnits(int src, int dst) {
        AdmDiv srcProv = provinces[src];
        AdmDiv dstProv = provinces[dst];
        List<MilUnit> l;
    }

    public void occupyAdmDiv(int occupierId, int provId) {
        AdmDiv admDiv = provinces[provId];
        if (admDiv != null) {
            if (admDiv.getOwnerId() == occupierId) {
                admDiv.setUnoccupied();
            } else {
                admDiv.setOccupierId(occupierId);
            }
        }
    }

    public void occupyAllAdmDiv(int occupierId, String cId) {
        Country c = countries.get(cId);
        if (c != null)
            for (AdmDiv admDiv : c.getAdmDivs()) {
                if (admDiv.getOwnerId() == occupierId) {
                    admDiv.setUnoccupied();
                } else {
                    admDiv.setOccupierId(occupierId);
                }
            }
    }
}
