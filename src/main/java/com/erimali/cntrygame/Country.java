package com.erimali.cntrygame;

import com.erimali.cntrymilitary.Military;
import com.erimali.cntrymilitary.Person;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Country implements Serializable, Comparable<Country> {
    private String name;
    private int countryId;
    private long population;
    private double populationIncrease;
    private double area;
    private int waterProvinces; // isLandlocked(){return waterProvinces == 0;}
    private EnumSet<Continent> continents;
    private AdmDiv capital;
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

    private Map<Integer, CSubject> subjects;
    private CSubject subjectOf;


    private CFormable isFormed;
    private List<Union> unions;

    //List<ActiveRebellion> rebellions; // give in to demands

    private short[] availableBuildings;
    // SOME COUNTRIES CAN START AS SUBJECTS OF OTHERS;


    private boolean randGenerated;

    // Constructors
    public Country(String name, double area, long population, double populationIncrease, String capital,
                   String admDivisionType, List<AdmDiv> admDivisions, List<Short> languages,
                   Government gov, Economy eco, Military mil, Diplomacy dip) {
        this.name = name;
        this.continents = EnumSet.noneOf(Continent.class);
        this.area = area;
        this.population = population;
        this.populationIncrease = populationIncrease;
        this.admDivisionType = admDivisionType;
        this.admDivisions = admDivisions;
        this.languages = languages;
        this.eco = eco;
        this.gov = gov;
        this.dip = dip;
        this.mil = mil;
        AdmDiv cap = getAdmDiv(capital);
        this.capital = cap != null ? cap : admDivisions.getFirst();

        this.subjects = new HashMap<>();
        this.unions = new LinkedList<>();

        // FOR CONSISTENCY
        fixPopulation();
        fixGDPPerCapita();
        initAvailableBuildings();
    }

    public Country(String name, double area, long population, double populationIncrease, String capital,
                   String admDivisionType, List<AdmDiv> admDivisions, List<Short> languages,
                   Government gov, Economy eco, Military military) {
        this.name = name;
        this.continents = EnumSet.noneOf(Continent.class);
        this.area = area;
        this.population = population;
        this.populationIncrease = populationIncrease;
        this.admDivisionType = admDivisionType;
        this.admDivisions = admDivisions;
        this.languages = languages;
        this.gov = gov;
        this.eco = eco;
        this.mil = military;
        this.dip = new Diplomacy();

        AdmDiv cap = getAdmDiv(capital);
        this.capital = cap != null ? cap : admDivisions.isEmpty() ? null : admDivisions.getFirst();

        this.subjects = new HashMap<>();
        this.unions = new LinkedList<>();

        // FOR CONSISTENCY
        fixPopulation();

        initAvailableBuildings();
    }

    public Country(String name) {
        this.name = name;
    }

    public void yearlyTick() {
        incPopulation();
        incEconomy();
        gov.yearlyTick();
        yearlySubjectsTick();
    }

    private void incEconomy() {
        eco.yearlyTick();
    }

    public void monthlyTick() {
        //mil/tech progress
        short milResearchBonus = (short) (gov.researchBoost() + 4 * availableBuildings[Building.MIL_RESEARCH_FACILITY.ordinal()]);
        double gdp = eco.getGDP();
        double milExpenditures = mil.monthlyTick(population, gdp, milResearchBonus);
        eco.monthlyTreasuryUpdate(milExpenditures);
        if (eco.getTreasury() < 0)
            mil.stopAllResearch();
        taxSubjects();


        //if is researching...
    }

    public void yearlySubjectsTick() {
        for (CSubject s : subjects.values())
            s.yearlyTick();
    }

    public void taxSubjects() {
        for (CSubject s : subjects.values()) {
            s.taxSubject();
        }
    }

    // toString()...
    @Override
    public String toString() {
        return name;
    }

    public String toStringLong() {
        if (isNotSubject())
            return gov.getType() + " of " + this.name;
        else
            return gov.getType() + " of " + subjectOf.toString();
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
        return gov.toStringRulers();
    }

    public String toStringEconomy() {
        return eco.toStringLong();
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
        return eco.getGDP() / population;
    }

    // ACTIONS
    public void addGDP(double gdp) {
        eco.addGDP(gdp);
    }

    public void addGDP(String gdp) {
        eco.addGDP(gdp);
    }

    public void reputationHitFromWar(Country op, CountryArray cArr, CasusBelli casusBelli) {
        for (Union u : unions) {
            if (u.containsCountry(op.getCountryId())) {
                for (int i : u.getUnionCountries()) {
                    if (i != countryId) {
                        dip.worsenRelations(i, casusBelli.getPerceivedAggressiveness());
                    }
                }
            }
        }
        if (neighbours != null)
            for (int i : neighbours) {
                if (cArr.containsKey(i)) {
                    Country c = cArr.get(i); //(?)
                    if (c.getDiplomacy().isRivalWith(op.countryId)) {
                        dip.improveRelations(i, casusBelli.getPerceivedAggressiveness());
                    } else if (c.getDiplomacy().isAllyWith(op.countryId)) {
                        dip.worsenRelations(i, (short) (2 * casusBelli.getPerceivedAggressiveness()));
                    } else {
                        dip.worsenRelations(i, casusBelli.getPerceivedAggressiveness());
                    }
                }
            }
    }

    // War
    public War declareWar(int opId, GLogic game, CasusBelli casusBelli) {
        CountryArray cArr = game.getWorldCountries();
        Country op = cArr.get(opId);
        if (op == null)
            return null;
        if (gov.canDeclareWar()) {
            Set<Integer> opUnionAllies = new HashSet<>();
            for (Union u : op.unions) {
                if (u.hasType(u.MILITARY) && !u.containsCountry(this.countryId)) {
                    opUnionAllies.addAll(u.getUnionCountries());
                }
            }
            if (!opUnionAllies.isEmpty()) {
                opUnionAllies.remove(op.countryId);
            }
            reputationHitFromWar(op, cArr, casusBelli);
            return new War(game, this, op, casusBelli);
        }
        return null;
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

    public void updateNeighbours(CountryArray cArr, Country o) {
        for (int i : o.neighbours) {
            if (i != countryId && !neighbours.contains(i)) {
                neighbours.add(i);
                Country temp = cArr.get(i);
                if (temp != null)
                    temp.neighbours.add(countryId);
            }
        }
    }

    // Full Annexation
    public void annexCountry(CountryArray cArray, int ind, boolean... cond) {
        Country op = cArray.get(ind);

        updateNeighbours(cArray, op);
        population += op.population;
        area += op.area;
        admDivisions.addAll(op.admDivisions);
        addLanguages(op.getLanguages());
        // Get the economy
        eco.annex(op.eco);
        annexAllAdmDivs(op);

        // Soldiers disbanded (EXCEPT when union)
        switch (cond.length) {
            case 3:

            case 2:

            case 1:
                if (cond[0])
                    //Peaceful annexation
                    mil.takeDivisions(op.mil);
                else
                    // Get the military equipment of the one who lost/got annexed
                    mil.seizeVehicles(op.mil);
                break;
            default:

        }

        // delete opponent country
        cArray.remove(ind);
        cArray.getWorld().getMap().makeUpdateTextCountriesNames(cArray);
    }

    //are they happy with annexation ... , change
    public void annexAllAdmDivs(Country o, boolean... args) {
        List<AdmDiv> l = o.getAdmDivs();
        while (!l.isEmpty()) {
            AdmDiv a = l.removeFirst();

            addAdmDiv(a);
            //admDivisions.add(a);
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

    public int getAdmDivsSize() {
        return admDivisions.size();
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

    public long incPopulation(double populationIncrease) {
        long prePop = population;
        for (AdmDiv a : admDivisions) {
            this.population += a.incPopulation(populationIncrease);
        }
        return population - prePop;
    }

    public void incPopulationIncrease(double amount) {
        if (amount > 0.0)
            populationIncrease += amount;
    }

    public void decPopulationIncrease(double amount) {
        if (amount > 0.0)
            populationIncrease -= amount;
    }

    public double getPopulationIncrease() {
        return populationIncrease;
    }

    public void setPopulationIncrease(double populationIncrease) {
        this.populationIncrease = populationIncrease;
    }

    public void addPopulation(long popAmount) {
        int perAdmDiv = (int) (popAmount / admDivisions.size());
        int extra = (int) (popAmount % admDivisions.size());
        for (AdmDiv a : admDivisions) {
            a.addPopulation(perAdmDiv);
        }
        admDivisions.getFirst().addPopulation(extra);
        this.population += popAmount;
    }

    public int addIncPopulation(double popInc, AdmDiv admDiv) {
        int amount;
        if (popInc >= -1 && popInc <= 1) {
            amount = admDiv.incPopulation(popInc);
            this.population += amount;
        } else {
            amount = (int) popInc;
            admDiv.addPopulation(amount);
            this.population += amount;
        }
        return amount;
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

    public boolean isAtWarWith(int selectedCountry) {
        return mil.isAtWarWith(selectedCountry);
    }

    public boolean isAllyWith(int c) {
        return dip.isAllyWith(c);
    }

    public boolean isAllyWith(short c) {
        return dip.isAllyWith(c);
    }

    public void worsenRelations(int c, short amount) {
        dip.worsenRelations(c, amount);
    }

    public void improveRelations(int c) {
        dip.improveRelations(c);
    }

    public void improveRelations(int c, short val) {
        dip.improveRelations(c, val);
    }

    public short getRelations(String c) {
        return dip.getRelations(CountryArray.getIndexShort(c));
    }

    public short getRelations(int c) {
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
        dip.addAlly(CountryArray.getIndexShort(c));
    }

    public void removeAlly(int c) {
        dip.removeAlly(c);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!
    public void subjugateCountry(Country op, SubjectType type) {
        if (subjectOf == null) {
            // gain access to water for navy //boolean ONLY HERE IMPORTANT!
            //waterAccess
            incAvailableBuildings(op);
            CSubject cs = makeSubject(op, type);
            subjects.put(op.getCountryId(), cs);
        } else {
            subjugateCountry(subjectOf.getMain(), type);
        }
    }


    public CSubject makeSubject(Country c, SubjectType type) {
        c.clearAlliesAndRivals();
        //taking subjects
        for (Map.Entry<Integer, CSubject> entry : c.subjects.entrySet()) {
            entry.getValue();
        }
        return new CSubject(this, c, type);
    }

    public boolean releaseSubject(String iso2) {
        return releaseSubject(CountryArray.getIndex(iso2));
    }

    // WAR FOR INDEPENDENCE OR RELEASE
    public boolean releaseSubject(int iso2) {
        if (subjects.containsKey(iso2)) {
            decAvailableBuildings(subjects.get(iso2).getSubject());
            subjects.get(iso2).getSubject().subjectOf = null;
            subjects.remove(iso2);
            return true;
        }
        return false;
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
                this.annexCountry(cArray, i, true);
        }
    }

    public void uniteWith(String name, CountryArray cArray, Set<Integer> countries) {
        this.name = name;
        for (int i : countries) {
            if (cArray.containsKey(i))
                this.annexCountry(cArray, i, true);
        }
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
        return subjectOf == null;
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

    public boolean hasSubject(int c) {
        return subjects.containsKey(c);
    }

    //Country c as input for more ?
    public boolean sendAllianceRequest(GLogic game, int c) {
        Country o = game.getCountry(c);
        //other reasons, why would AI accept
        boolean goodRelations = (this.getMainLanguage() == o.getMainLanguage());

        if (o.isNotSubject() && (this.getRelations(c) > 100 || goodRelations)) {
            this.addAlly(c);
            o.addAlly(this.countryId);
            return true;
        } else {
            return false;
        }

    }
    public boolean sendSubjectRequest(GLogic game, int c) {
        Country o = game.getCountry(c);
        //other reasons, why would AI accept
        boolean goodRelations = (this.getMainLanguage() == o.getMainLanguage());

        if (o.isNotSubject() && (this.getRelations(c) > 200 || (goodRelations && Math.random() > 0.6))) {
            subjugateCountry(o, SubjectType.SATELLITE);
            return true;
        } else {
            return false;
        }

    }

    public boolean breakAlliance(GLogic game, int c) {
        Country o = game.getCountry(c);
        if (isAllyWith(c)) {
            removeAlly(c);
            o.removeAlly(countryId);
            worsenRelations(c, (short) 20);
            if(hasSubject(c)){
                releaseSubject(c);
            }
            return true;
        }
        return false;
    }

    public void removeContinent(Continent cont) {
        continents.remove(cont);
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
        return unions;
    }

    public Union getUnion(int i) {
        return unions.get(i);
    }

    public void addUnion(Union u) {
        unions.add(u);
    }

    public void removeUnion(Union u) {
        unions.remove(u);
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
        //return landlocked && waterProvinces == 0;
        return waterProvinces == 0; // && waterSubjectProvinces
    }

    public List<Short> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Short> languages) {
        this.languages = languages;
    }

    public AdmDiv getCapital() {
        return capital;
    }

    public void setCapital(AdmDiv capital) {
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
        return CountryArray.getIndexISO2(countryId);
    }

    public void setIso2(String iso2) {
        this.countryId = CountryArray.getIndex(iso2);
    }

    public void setIso2(int iso2) {
        this.countryId = iso2;
    }

    public int getCountryId() {
        return countryId;
    }

    public short getMainLanguage() {
        return languages.getFirst();
    }

    //world / countryarray for new country if its being formed
    public void releaseAdmDivTo(Country o, short... indAdmDiv) {
        List<AdmDiv> independentList = removeAndGetAdmDivs(indAdmDiv);
        o.addAdmDivs(independentList);
    }

    private void addAdmDivs(List<AdmDiv> admDivs) {
        for (AdmDiv d : admDivs)
            addAdmDiv(d);
    }

    public List<AdmDiv> removeAndGetAdmDivs(short... indAdmDiv) {
        Collections.sort(admDivisions);
        Arrays.sort(indAdmDiv);
        ListIterator<AdmDiv> iterator = admDivisions.listIterator();
        List<AdmDiv> independentList = new ArrayList<>();
        int i = 0;
        while (iterator.hasNext() && i < indAdmDiv.length) {
            AdmDiv a = iterator.next();
            if (a.getProvId() == indAdmDiv[i]) {
                independentList.add(a);
                removeAdmDivAttr(a);
                iterator.remove();
                i++;
            }
        }
        return independentList;
    }

    public static long calcTotalPop(List<AdmDiv> admDivs) {
        long pop = 0;
        for (AdmDiv a : admDivs) {
            pop += a.getPopulation();
        }
        return pop;
    }

    public static double calcTotalArea(List<AdmDiv> admDivs) {
        double area = 0;
        for (AdmDiv a : admDivs) {
            area += a.getPopulation();
        }
        return area;
    }

    public static List<Short> calcTotalLanguages(List<AdmDiv> admDivs) {
        Map<Short, Integer> count = new HashMap<>();
        for (AdmDiv a : admDivs) {
            short ml = a.getMainLanguage();
            if (count.containsKey(ml)) {
                count.put(ml, count.get(ml) + 1);
            } else {
                count.put(ml, 1);
            }
        }
        Stream<Map.Entry<Short, Integer>> sorted = count.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));
        List<Short> l = sorted.map(Map.Entry::getKey).collect(Collectors.toList());

        return l;
    }

    public static Country formCountryFromProvinces(CountryArray cArray, int iso2, String name, List<AdmDiv> admDivs) {
        double area = calcTotalArea(admDivs);
        long population = calcTotalPop(admDivs);
        List<Short> languages = calcTotalLanguages(admDivs);
        Country c = new Country(name, area, population, 0.1, "", "", admDivs, languages, null, null, null, null);
        c.setIso2(iso2);
        return c;
    }


    public AdmDiv getAdmDiv(int index) {
        return admDivisions.get(index);
    }

    public AdmDiv getAdmDiv(String name) {
        for (AdmDiv a : admDivisions) {
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    public AdmDiv getAdmDivProvId(int provId) {
        for (AdmDiv a : admDivisions) {
            if (a.getProvId() == provId)
                return a;
        }
        return null;
    }

    public boolean hasAdmDiv(AdmDiv a) {
        return admDivisions.contains(a);
    }

    public boolean isRandGenerated() {
        return randGenerated;
    }

    public void setRandGenerated(boolean randGenerated) {
        this.randGenerated = randGenerated;
    }

    public void addAdmDiv(AdmDiv a) {
        a.setOwnerId(countryId);
        admDivisions.add(a);
        population += a.getPopulation();
        area += a.getArea();
        //if (landlocked && a.hasWaterAccess()){landlocked = false;}
        //landlocked = landlocked && !a.hasWaterAccess();// PROBLEM, need amount of wateraccess provinces
        if (a.hasWaterAccess())
            waterProvinces++;
    }

    //call in end
    public void calcSetWaterAccess() {
        waterProvinces = 0;
        for (AdmDiv a : admDivisions)
            if (a.hasWaterAccess())
                waterProvinces++;
    }

    public void removeAdmDiv(AdmDiv a) {
        if (admDivisions.remove(a)) {
            removeAdmDivAttr(a);
        }
    }

    public void removeAdmDivAttr(AdmDiv a) {
        population -= a.getPopulation();
        area -= a.getArea();
        if (a.hasWaterAccess())
            waterProvinces--;
    }

    public CFormable getIsFormed() {
        return isFormed;
    }

    public void setIsFormed(CFormable isFormed) {
        this.isFormed = isFormed;
    }

    public void initAvailableBuildings() {
        this.availableBuildings = new short[Building.values().length];
        Arrays.fill(availableBuildings, (short) 0);
    }

    public void addAvailableBuilding(Building b) {
        availableBuildings[b.ordinal()]++;
        if (subjectOf != null) {
            subjectOf.getMain().addAvailableBuilding(b);
        }
    }

    //might be redundant
    public void addAvailableBuildings(Building b, short amount) {
        if (amount > 0) {
            availableBuildings[b.ordinal()] += amount;
            if (subjectOf != null) {
                subjectOf.getMain().addAvailableBuildings(b, amount);
            }
        }
    }

    public void removeAvailableBuilding(Building b) {
        availableBuildings[b.ordinal()]--;
        if (subjectOf != null) {
            subjectOf.getMain().removeAvailableBuilding(b);
        }
    }

    public void removeAvailableBuildings(Building b, short amount) {
        if (amount > 0) {
            availableBuildings[b.ordinal()] -= amount;
            if (subjectOf != null) {
                subjectOf.getMain().removeAvailableBuildings(b, amount);
            }
        }
    }

    private void incAvailableBuildings(Country sub) {
        for (int i = 0; i < availableBuildings.length; i++) {
            availableBuildings[i] += sub.availableBuildings[i];
        }
    }

    public void decAvailableBuildings(Country sub) {
        for (int i = 0; i < availableBuildings.length; i++) {
            availableBuildings[i] -= sub.availableBuildings[i];
        }
    }

    public boolean hasBuildingType(Building b) {
        return availableBuildings[b.ordinal()] > 0;
    }

    public short[] calcTotalBuildings() {
        return calcTotalBuildings(admDivisions);
    }
    //on subjugate

    public static short[] calcTotalBuildings(List<AdmDiv> admDivisions) {
        short[] bArr = new short[Building.values().length];
        Arrays.fill(bArr, (short) 0);
        for (AdmDiv a : admDivisions) {
            for (Building b : a.getBuildings()) {
                bArr[b.ordinal()]++;
            }
        }
        return bArr;
    }

    public short[] getAvailableBuildings() {
        return availableBuildings;
    }

    @Override
    public int compareTo(Country o) {
        return Integer.compare(countryId, o.countryId);
    }

    public double getTreasury() {
        return eco.getTreasury();
    }

    public int getAdmDivRandomIndex() {
        return (int) (Math.random() * admDivisions.size());
    }

    public AdmDiv getAdmDivRandom() {
        return admDivisions.get(getAdmDivRandomIndex());
    }

    public void incInfrastructure(int i) {
        //dangerous not based on provId
        if (i < 0 || i >= admDivisions.size())
            return;
        AdmDiv admDiv = admDivisions.get(i);
        incInfrastructure(admDiv);
    }

    public void incInfrastructure(AdmDiv admDiv) {
        //eco.addPercentGDP(1.0 / admDivisions.size());
        double popRatio = admDiv.getPopulation() / population;
        admDiv.incInfrastructure();
        eco.addMulGDP(popRatio * 0.5);
    }

    public Country releaseCountry(World world, int cId) {
        //world.countryFromFile(cId);
        return null;
    }

    public void liberateAllSubjects(boolean... args) {
        switch (args.length) {

            case 1:

                break;
        }
        for (int i : subjects.keySet()) {
            releaseSubject(i);
        }
    }

    public List<Integer> admDivIdList() {
        List<Integer> list = new LinkedList<>();
        for (AdmDiv a : admDivisions) {
            list.add(a.getProvId());
        }
        return list;
    }

    public void fixGDPPerCapita() {
        double tempGDP = eco.getGDP();
        long tempPop = population;
        for (AdmDiv a : admDivisions) {
            float val = a.getGdpPerCapita();
            if (val > 0) {
                tempGDP -= a.getGDP();
                tempPop -= a.getPopulation();
            }
        }
        float perCapitaRest = (float) (tempGDP / tempPop);
        for (AdmDiv a : admDivisions) {
            if (a.getGdpPerCapita() <= 0) {
                a.setGdpPerCapita(perCapitaRest);
            }
        }

    }

    public void setMilPopConscriptionRate(double value) {
        mil.setPopConscriptionRate(value);
    }

    public void setMilPopConscriptionRate(int index) {
        mil.setPopConscriptionRate(index);
    }

    public void spendTreasury(double value) {
        eco.decTreasury(value);
    }

    public void addTreasury(double value) {
        eco.incTreasury(value);
    }

    public int getCapitalId() {
        return capital.getProvId();
    }

    public boolean hasElectionsThisYear() {
        return gov.hasElectionsThisYear();
    }

    public void incWaterProvCount() {
        waterProvinces++;
    }

    public void setNeighbours(Set<Integer> neighbours) {
        this.neighbours = neighbours;
    }

    public void removePopulation(int popDec) {
        if (popDec > 0) {
            this.population -= popDec;
            if (popDec < 0)
                population = 0;
        }
    }

    public boolean hasNukes() {
        return mil.hasNukes();
    }

    public void removeAtWarWith(int countryId) {
        mil.getAtWarWith().remove(countryId);
    }
}
