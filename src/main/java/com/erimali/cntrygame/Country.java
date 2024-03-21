package com.erimali.cntrygame;

import java.util.*;

public class Country {
    // CONSTANTS
    //
    private String name;
    private String iso2;
    private long population;
    private double populationIncrease;
    private double area;
    private boolean landlocked; // if false -> no navy // River Flotilla !!!
    private EnumSet<Continent> continents;
    private String capital;
    private String[] infoElectronic;
    private List<String> languages;

    private String admDivisionType; // county,district,etc.
    private List<AdmDiv> admDivisions;
    // !!!!!!!!!!!!!!!!!!!!
    private Set<Integer> annexedCountries; // !!!!!!!!!!!!!!!!!!!!!!!
    private Diplomacy dip;
    private Economy economy;
    private Government gov; // Republic of Albania toString()
    private Military military;
    // Other countries
    private Set<Integer> neighbours;

    // int -> type (satellite state, autonomous region, colony, etc.)
    private Map<String, CSubject> subjects;
    private CSubject subjectOf;
    // SOME COUNTRIES CAN START AS SUBJECTS OF OTHERS;

    // Constructors
    public Country(String name, long population, double area, boolean landlocked, String capital,
                   String[] infoElectronic, String admDivisionType, List<AdmDiv> admDivisions, List<String> languages,
                   Set<String> neighbours, Government gov, Economy economy, Military military) {
        this.name = name;
        this.population = population;
        this.area = area;
        this.landlocked = landlocked;
        this.capital = capital;
        this.setInfoElectronic(infoElectronic);

        this.admDivisionType = admDivisionType;
        this.admDivisions = admDivisions;
        this.languages = languages;
        this.economy = economy;
        this.gov = gov;
        this.dip = new Diplomacy();
        //this.neighbours = ;

        this.subjects = new HashMap<>();
        // FOR CONSISTENCY
        fixPopulation();
    }

    public Country(String name, long population, double area, boolean landlocked, String capital,
                   String[] infoElectronic, String admDivisionType, List<AdmDiv> admDivisions, String[] languages,
                   String[] neighbours, Government gov, Economy economy, Military military) {
        this.name = name;
        this.population = population;
        this.area = area;
        this.landlocked = landlocked;
        this.capital = capital;
        this.setInfoElectronic(infoElectronic);
        this.admDivisionType = admDivisionType;
        this.admDivisions = admDivisions;
        this.languages = Arrays.asList(languages);
        this.gov = gov;
        this.economy = economy;
        this.military = military;
        this.dip = new Diplomacy();

        ////???this.neighbours = new HashSet<>(Arrays.asList(languages));
        this.neighbours = new TreeSet<>();
        for (String n : neighbours) {
            int ind = CountryArray.getIndex(n);
            this.neighbours.add(ind);
        }

        this.subjects = new HashMap<>();

        // FOR CONSISTENCY
        fixPopulation();

    }

    public Country(String name) {
        this.name = name;
    }


    // toString()...
    @Override
    public String toString() {
        return this.name;
    }

    public String toStringLong() {
        if (isNotSubject())
            return this.gov.getType() + " of " + this.name;
        else
            return this.gov.getType() + " of " + subjectOf.toString();
    }

    public String toStringAdmDivs() {
        StringBuilder sb = new StringBuilder();
        sb.append("Type of Administrative Divisions: ").append(admDivisionType).append("\n");
        for (AdmDiv a : admDivisions) {
            sb.append(a.toString()).append("\n");
        }
        return sb.toString();
    }

    public String toString(int extendedInfo) {
        switch (extendedInfo) {
            case 0:
                return toStringLong() + "\nPopulation: " + population + "\nArea: " + area + " km^2";
            default:
                return toString();
        }
    }

    public String toStringRulers() {
        return this.gov.toStringRulers();
    }

    // SPECIAL GETs
    public double getPopulationPerCapita() {
        if (area > 0) {
            return population / area;
        } else {
            return 0.0;
        }
    }

    public double getGDPPerCapita() {
        return this.economy.getGDP() / population;
    }

    // ACTIONS
    public void addGDP(double gdp) {
        economy.addGDP(gdp);
    }

    public void addGDP(String gdp) {
        economy.addGDP(gdp);
    }

    // War
    public War declareWar(Country op, CasusBelli casusBelli) {
        return new War(this, op, casusBelli);
    }

    //Neighbours
    public boolean hasNeighbour(String c) {
        return hasNeighbour(CountryArray.getIndex(c));
    }

    public boolean hasNeighbour(int i) {
        return neighbours.contains(i);
    }

    public Set<Integer> getNeighbours() {
        return neighbours;
    }

    // Annexation
    public void annexCountry(Country op) {
        if (this.landlocked) {
            if (!op.landlocked) {
                this.landlocked = false;
            }
        }
        this.population += op.population;
        this.area += op.area;
        this.admDivisions.addAll(op.admDivisions);
        addLanguages(op.getLanguages());
        // Get the economy

        // Get the military equipment of the one who lost
        // Soldiers disbanded (EXCEPT RARE EVENT?)

        // delete opponent country
        op = null; // doesn't do the job
    }

    public void removeLanguages(String... langs) {
        for (String l : langs) {
            languages.remove(l);
        }
    }

    public void addLanguages(String... langs) {
        for (String l : langs) {
            if (!languages.contains(l)) {
                languages.add(l);
            }
        }
    }

    public void addLanguages(List<String> langs) {
        for (String l : langs) {
            if (!languages.contains(l)) {
                languages.add(l);
            }
        }
    }

    public String getAdmDivType() {
        return admDivisionType;
    }

    public void setAdmDivType(String admDivisionType) {
        this.admDivisionType = admDivisionType;
    }


    public List<AdmDiv> getAdmDivs() {
        return admDivisions;
    }

    public void setAdmDivs(List<AdmDiv> admDivisions) {
        this.admDivisions = admDivisions;
    }


    // POPULATION
    // yearly?
    public void incPopulation() {
        for (AdmDiv a : admDivisions) {
            this.population += a.incPopulation(populationIncrease);
        }
    }

    public double getPopulationIncrease() {
        return populationIncrease;
    }

    public void setPopulationIncrease(double populationIncrease) {
        this.populationIncrease = populationIncrease;
    }

    // FOR NOT UPDATED COUNTRY ADMDIVISION DATA
    public boolean checkPopulationAdmDiv() {
        long totalAdmDivPop = 0;
        for (AdmDiv a : admDivisions) {
            totalAdmDivPop += a.getPopulation();
        }
        return totalAdmDivPop == this.population;
    }

    // Even if old data -> based on the percentage -> assume/calc new data
    public void fixPopulation() {
        if (checkPopulationAdmDiv()) {
            return;
        }
        long totalAdmDivPop = 0;
        for (AdmDiv a : admDivisions) {
            totalAdmDivPop += a.getPopulation();
        }
        int extraPop = (int) (this.population - totalAdmDivPop);
        int substractedPop = 0;
        for (AdmDiv a : admDivisions) {
            double percent = (double) a.getPopulation() / (double) totalAdmDivPop;
            int sp = (int) (extraPop * percent);
            a.addPopulation(sp);
            substractedPop += sp;
        }
        if (substractedPop > extraPop) {
            admDivisions.get(0).subtractPopulation(substractedPop - extraPop);
        } else if (substractedPop < extraPop) {
            admDivisions.get(0).addPopulation(extraPop - substractedPop);
        }
    }

    public String toStringAdmDiv(int i) {
        return admDivisionType + " of " + admDivisions.get(i).toStringLong();
    }

    // isSubject -> instanceof CSubject
    public CSubject makeSubject(Country c, int type) {
        c.clearAlliesAndRivals();
        return new CSubject(this, c, type);
    }


    public boolean isAllyWith(int c) {
        return dip.getAllies().contains((short) c);
    }
    public void improveRelations(int c) {
        dip.improveRelations(c);
    }

    public void improveRelations(String c) {
        dip.improveRelations(CountryArray.getIndex(c));
    }

    public void improveRelations(String c, short val) {
        dip.improveRelations(CountryArray.getIndexShort(c), val);
    }

    public int getRelations(String c) {
        return dip.getRelations(CountryArray.getIndex(c));
    }

    public int getRelations(int c) {
        return dip.getRelations(c);
    }

    public void clearAlliesAndRivals() {
        dip.clearAllies();
        dip.clearRivals();
    }

    public void addAlly(int c) {
        dip.addAlly(c);
    }

    public void addAlly(String c) {
        dip.addAlly((short) CountryArray.getIndex(c));
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!
    public void subjugateCountry(Country op, int type) {
        // gain access to water for navy
        if (this.landlocked) {
            if (!op.landlocked) {
                this.landlocked = false;
            }
        }
        CSubject cs = makeSubject(op, type);
        subjects.put(op.getIso2(), cs);
    }

    // WAR FOR INDEPENDENCE?!?
    public void releaseSubject(String iso2) {
        subjects.remove(iso2);
    }

    public void checkSubjects() {
        // CHECK FOR REBELLION
    }

    public Map<String, CSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Map<String, CSubject> subjects) {
        this.subjects = subjects;
    }

    // FIX
    public void loadLanguage(World w) {
        for (String l : languages) {
            w.addLanguage(l);
        }
    }

    public void uniteWith(String name, Country... countries) {
        this.name = name;
        for (Country c : countries) {
            this.annexCountry(c);
        }
    }

    public String getPhonePrefix() {
        return infoElectronic[0];
    }

    public void setPhonePrefix(String phonePrefix) {
        this.infoElectronic[1] = phonePrefix;
    }

    public String getInternetDomain() {
        return infoElectronic[1];
    }

    public void setInternetDomain(String internetDomain) {
        this.infoElectronic[1] = internetDomain;
    }

    public String[] getInfoElectronic() {
        return infoElectronic;
    }

    public void setInfoElectronic(String[] infoElectronic) {
        this.infoElectronic = infoElectronic;
    }

    public void changeGovType(String type) {
        gov.setType(type);
    }

    public void changeGovRuler(int i, String ruler) {
        if (i == 0) {
            gov.setHeadOfState(new Ruler(ruler));
        } else if (i == 1) {
            gov.setHeadOfGovernment(new Ruler(ruler));
        }
    }

    public boolean isNotSubject() {
        return getSubjectOf() == null;
    }

    public CSubject getSubject(String iso2) {
        return getSubject(CountryArray.getIndex(iso2));
    }

    public CSubject getSubject(int i) {
        return subjects.get(i);
    }

    public CSubject getSubjectOf() {
        return subjectOf;
    }

    public void setSubjectOf(CSubject subjectOf) {
        this.subjectOf = subjectOf;
    }

    public boolean hasSubject(String cn) {
        return subjects.containsKey(cn);
    }

    //Country c as input for more ?
    public boolean sendAllianceRequest(int c) {
        //!!!!!!!!!!!!
        boolean goodRelations = false; //other reasons, why would AI accept

        if (this.getRelations(c) > 100 || goodRelations) {
            this.addAlly(c);
            return true;
        } else {
            return false;
        }

    }

    public void addContinent(Continent cont) {
        continents.add(cont);
    }

    public void setContinents(String[] in) {
        for (String s : in) {
            try {
                continents.add(Continent.valueOf(s));
            } catch (IllegalArgumentException iae) {
                continue;
            }
        }
    }
    // Simple Getters/Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPopulation() {
        return population;
    }

    public void setPopulation(long population) {
        if (population >= 2)
            this.population = population;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        if(area > 0.0)
            this.area = area;
    }

    public boolean isLandlocked() {
        return landlocked;
    }

    public void setLandlocked(boolean landlocked) {
        this.landlocked = landlocked;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public Economy getEconomy() {
        return economy;
    }

    public void setEconomy(Economy economy) {
        this.economy = economy;
    }

    public String getCurrency() {
        return economy.getCurrency();
    }

    public void setCurrency(String currency) {
        this.economy.setCurrency(currency);
    }

    public Government getGovernment() {
        return gov;
    }

    public void setGovernment(Government gov) {
        this.gov = gov;
    }

    public Military getMilitary() {
        return military;
    }

    public void setMilitary(Military military) {
        this.military = military;
    }

    public Diplomacy getDiplomacy() {
        return dip;
    }

    public void setDiplomacy(Diplomacy dip) {
        this.dip = dip;
    }

    public String getIso2() {
        return iso2;
    }

    public void setIso2(String iso2) {
        this.iso2 = iso2;
    }

}
