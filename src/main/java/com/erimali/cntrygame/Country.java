package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.*;

public class Country implements Serializable {
    //private World world;
    private String name;
    private String iso2;


    private int countryId;
    private long population;
    private double populationIncrease;
    private double area;
    private boolean landlocked; // if false -> no navy // River Flotilla !!!
    private EnumSet<Continent> continents;
    private String capital;
    private String[] infoElectronic;
    private List<Short> languages;
    private String admDivisionType; // county,district,etc.
    private List<AdmDiv> admDivisions;
    // !!!!!!!!!!!!!!!!!!!!
    //private Set<Integer> annexedCountries; // !!!!!!!!!!!!!!!!!!!!!!!
    private Diplomacy dip;
    private Economy eco;
    private Government gov; // Republic of Albania toString()
    private Military mil;
    // Other countries
    private Set<Integer> neighbours;

    // int -> type (satellite state, autonomous region, colony, etc.)
    private Map<Integer, CSubject> subjects;
    private CSubject subjectOf;
    private List<Union> uni;

    // SOME COUNTRIES CAN START AS SUBJECTS OF OTHERS;

    // Constructors
    public Country(String name, double area, long population, double populationIncrease, boolean landlocked, String capital,
                   String[] infoElectronic, String admDivisionType, List<AdmDiv> admDivisions, List<Short> languages,
                   Set<Integer> neighbours, Government gov, Economy eco, Military mil, Diplomacy dip) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.populationIncrease = populationIncrease;
        this.landlocked = landlocked;
        this.capital = capital;
        this.infoElectronic = infoElectronic;
        this.admDivisionType = admDivisionType;
        this.admDivisions = admDivisions;
        this.languages = languages;
        this.eco = eco;
        this.gov = gov;
        this.dip = dip;
        this.mil = mil;
        this.neighbours = neighbours;

        this.subjects = new HashMap<>();
        this.uni = new LinkedList<>();

        // FOR CONSISTENCY
        fixPopulation();
    }

    public Country(String name, double area, long population, double populationIncrease, boolean landlocked, String capital,
                   String[] infoElectronic, String admDivisionType, List<AdmDiv> admDivisions, List<Short> languages,
                   String[] neighbours, Government gov, Economy eco, Military military) {
        this.name = name;
        this.area = area;
        this.population = population;
        this.populationIncrease = populationIncrease;
        this.landlocked = landlocked;
        this.capital = capital;
        this.infoElectronic = infoElectronic;
        this.admDivisionType = admDivisionType;
        this.admDivisions = admDivisions;
        this.languages = languages;
        this.gov = gov;
        this.eco = eco;
        this.mil = military;
        this.dip = new Diplomacy();

        this.neighbours = new TreeSet<>();
        for (String n : neighbours) {
            int ind = CountryArray.getIndex(n);
            this.neighbours.add(ind);
        }

        this.subjects = new HashMap<>();
        this.uni = new LinkedList<>();

        // FOR CONSISTENCY
        fixPopulation();

    }

    public Country(String name) {
        this.name = name;
    }

    public void yearlyTick() {
        incPopulation();
        //incEconomy();
        gov.reduceOneYearFromPolicies();
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
        StringBuilder sb = new StringBuilder();
        switch (extendedInfo) {
            case 0:
                sb.append(toStringLong()).append("\nPopulation: ").append(population).append("\nArea: ").append(area).append(" km^2");
                break;
            case 1:
                sb.append(toString(0)); //
                sb.append("\n").append(gov.getHeadOfState().toString()).append("\n").append(gov.getHeadOfGovernment().toString());
                break;
            default:
                sb.append(toString());
        }
        return sb.toString();
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
        return this.eco.getGDP() / population;
    }

    // ACTIONS
    public void addGDP(double gdp) {
        eco.addGDP(gdp);
    }

    public void addGDP(String gdp) {
        eco.addGDP(gdp);
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
    public void annexCountry(CountryArray cArray, int ind, boolean... cond) {
        Country op = cArray.get(ind);
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
        annexAllAdmDivs(op);
        // Get the military equipment of the one who lost/got annexed
        //this.uniteMilVehicles(op);
        // Soldiers disbanded (EXCEPT when union)
        switch (cond.length) {
            case 3:

            case 2:

            case 1:
                //this.uniteMilPersonnel(op);
                break;
            default:

        }

        // delete opponent country
        cArray.remove(ind);
    }

    public void annexAdmDivs(Country o, int... i) {
        //Comparable based on provId... PriorityQueue ?
        //i based on provId (?)
    }

    //are they happy with annexation ... , change
    public void annexAllAdmDivs(Country o, boolean... args) {
        List<AdmDiv> l = o.getAdmDivs();
        while (!l.isEmpty()) {
            AdmDiv a = l.removeFirst();
            a.setOwnerId(countryId);
            admDivisions.add(a);
        }

    }

    public void removeLanguages(short... langs) {
        for (short l : langs) {
            languages.remove(l);
        }
    }

    public void addLanguages(short... langs) {
        for (short l : langs) {
            if (!languages.contains(l)) {
                languages.add(l);
            }
        }
    }

    public void addLanguages(List<Short> langs) {
        for (Short l : langs) {
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
    // yearly

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
        // Interpolating data in the first
        if (substractedPop > extraPop) {
            admDivisions.getFirst().subtractPopulation(substractedPop - extraPop);
        } else if (substractedPop < extraPop) {
            admDivisions.getFirst().addPopulation(extraPop - substractedPop);
        }
    }

    public String toStringAdmDiv(int i) {
        return admDivisionType + " of " + admDivisions.get(i).toStringLong();
    }

    // isSubject -> instanceof CSubject


    public boolean isAllyWith(int c) {
        return dip.isAllyWith(c);
    }

    public boolean isAllyWith(short c) {
        return dip.isAllyWith(c);
    }

    public void improveRelations(int c) {
        dip.improveRelations(c);
    }

    public void improveRelations(int c, short val) {
        dip.improveRelations(c, val);
    }

    public int getRelations(String c) {
        return dip.getRelations(CountryArray.getIndexShort(c));
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
    public void subjugateCountry(Country op, SubjectType type) {
        // gain access to water for navy
        if (this.landlocked) {
            if (!op.landlocked) {
                this.landlocked = false;
            }
        }
        CSubject cs = makeSubject(op, type);
        subjects.put(CountryArray.getIndex(op.getIso2()), cs);
    }

    public CSubject makeSubject(Country c, SubjectType type) {
        c.clearAlliesAndRivals();
        //gain their subjects?
        return new CSubject(this, c, type);
    }

    // WAR FOR INDEPENDENCE?!?
    public void releaseSubject(String iso2) {
        subjects.remove(iso2);
    }

    public void checkSubjects() {
        // CHECK FOR REBELLION
    }

    public Map<Integer, CSubject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Map<Integer, CSubject> subjects) {
        this.subjects = subjects;
    }

    public void uniteWith(String name, CountryArray cArray, int... countries) {
        this.name = name;
        for (int i : countries) {
            if (cArray.containsKey(i))
                this.annexCountry(cArray, i);
        }
    }

    public void uniteWith(String name, CountryArray cArray, Set<Short> countries) {
        this.name = name;
        for (short i : countries) {
            if (cArray.containsKey(i))
                this.annexCountry(cArray, i);
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

    public boolean hasSubject(String iso2) {
        return subjects.containsKey(CountryArray.getIndex(iso2));
    }

    public boolean hasSubject(int c) {
        return subjects.containsKey(c);
    }

    //Country c as input for more ?
    public boolean sendAllianceRequest(int c) {
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
            }
        }
    }

    public List<Union> getUnions() {
        return uni;
    }

    public Union getUnion(int i) {
        return uni.get(i);
    }

    public void addUnion(Union u) {
        uni.add(u);
    }

    public void removeUnion(Union u) {
        uni.remove(u);
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
        if (area > 0.0)
            this.area = area;
    }

    public boolean isLandlocked() {
        return landlocked;
    }

    public void setLandlocked(boolean landlocked) {
        this.landlocked = landlocked;
    }

    public List<Short> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Short> languages) {
        this.languages = languages;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public Economy getEconomy() {
        return eco;
    }

    public void setEconomy(Economy eco) {
        this.eco = eco;
    }

    public String getCurrency() {
        return eco.getCurrency();
    }

    public void setCurrency(String currency) {
        this.eco.setCurrency(currency);
    }

    public Government getGovernment() {
        return gov;
    }

    public void setGovernment(Government gov) {
        this.gov = gov;
    }

    public Military getMilitary() {
        return mil;
    }

    public void setMilitary(Military military) {
        this.mil = military;
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
        this.iso2 = iso2.toUpperCase();
        this.countryId = CountryArray.getIndex(iso2);
    }

    public int getCountryId() {
        return countryId;
    }

    public short getMainLanguage() {
        return languages.getFirst();
    }

}
