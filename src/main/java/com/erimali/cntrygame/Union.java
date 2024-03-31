package com.erimali.cntrygame;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

enum UnionPolicies {
    FREE_TRADE(Union.ECONOMIC),
    COMMON_CURRENCY(Union.ECONOMIC),
    INFRASTRUCTURE_INVESTMENT(Union.ECONOMIC), //double unionFunds; (?)
    SHARE_MIL_TECH(Union.MILITARY); //Every year set mil tech to country with the biggest/ or +1
    int type;
    UnionPolicies(int type) {
        this.type = type;
    }
}

//if UN -> fully centralized => WorldGovernment (!!!!!!!!!!!!!!!!!!!!!)
public class Union {
    protected final static int ECONOMIC = 1;
    protected final static int POLITICAL = 2; //Diplomatic+Government
    protected final static int MILITARY = 4;
    protected final static int MAX_TYPES = 3;
    int type;

    //Bitwise operations
    public boolean hasType(int check) {
        return (type & check) != 0;
    }

    public void addType(int add) {
        type |= add;
    }

    public void removeType(int remove) {
        type &= ~remove;
    }

    protected static int genType(String in) {
        int type = 0;
        String[] s = in.trim().toUpperCase().split("\\s*,\\s*");
        for (int i = 0; i < MAX_TYPES; i++) {
            int val = switch (s[i].substring(0, 3)) {
                case "ECO" -> ECONOMIC;
                case "POL" -> POLITICAL;
                case "MIL" -> MILITARY;
                default -> 0;
            };
            if (val > 0) {
                if ((type & val) == 0) {
                    type |= val;
                }
            }
        }
        return type;
    }

    private String shortName;
    private String name;
    private World world; //CountryArray...
    private int stability;
    private float centralization;
    private Set<Short> unionCountries;
    private byte[] influence;
    //invest can increase influence ... power of country...

    // power?//Influence?
    private Government govUnion; //can be null
    //UnionGovernment extends Government, UnionGovPolicies
    private Military milUnion; // can be null
    Set<UnionPolicies> policies;
    // private UPanel panel;//accessible by the player for choices inside the union
    // event-like?
    String[][] choices;
    String[][] commands; // hmmm
    // Acts you can take

    List<short[]> votes;
    //2...5 choices up to short max value votes/choice
    //AI influence vote by who has most influence...

    public Union(World world, String shortName, String name, int type, short... countries) {
        this.world = world;
        this.shortName = shortName;
        this.name = name;
        this.type = type;
        this.stability = 50;
        unionCountries = new TreeSet<>();
        for (short i : countries) {
            Country c = world.getCountry(i);
            if (c != null) {
                unionCountries.add(i);
                c.addUnion(this);
            }
        }

        initInfluence();
    }

    @Override
    public String toString() {
        return name;
    }

    public void initInfluence() {
        influence = new byte[CountryArray.maxISO2Countries];
        for (short i = 0; i < influence.length; i++) {
            influence[i] = unionCountries.contains(i) ? (byte) 1 : (byte) -1;
        }
    }

    public void uniteAllCountries(int mainCountry) {
        CountryArray cArray = world.getCountries();
        Country c = cArray.get(mainCountry);
        c.uniteWith(name, cArray, unionCountries);
        // DISSOLVE UNION

    }

    // change
    public void incCentralization(float inc) {
        this.centralization += inc;
        if (this.centralization > 100)
            this.centralization = 100;
    }

    public void decCentralization(float dec) {
        this.centralization -= dec;
        if (this.centralization < -100)
            this.centralization = -100;
    }

    // how to remove from Map<String,Union> in World?
    // from gamestage?
    public boolean canDismantle(int c) {
        if (c < 0 || c > influence.length)
            return false;
        return influence[c] > 85;
    }

    public void forceDismantle() {
        world.removeUnion(shortName);
    }

    public boolean dismantle(int c) {
        if (canDismantle(c)) {
            world.removeUnion(shortName);
        }
        return false;
    }

    public void removeCountry(short c) {
        unionCountries.remove(c);
        influence[c] = -2;
    }

    /*
     European Union type:economic,... membercountries:IT,... policies:free trade
     (or index of sth in the code?),free movement,... events:more centralized?,
     immigration (make panel for EU and other unions like hre in EU4?)
     */

    public void incInfluence(short i, byte amount) {
        if (amount < 0 || i < 0 || i >= influence.length || !unionCountries.contains(i)) //or allow other countries to increase influence to join
            return;
        influence[i] += amount;
        if(influence[i] > 100)
            influence[i] = 100;
    }

    public void decInfluence(short i, byte amount) {
        if (amount > 0 || i < 0 || i >= influence.length || !unionCountries.contains(i))
            return;
        influence[i] += amount;
        if(influence[i] < 0)
            influence[i] = 0;
    }
    public boolean hasGovernment() {
        return govUnion != null;
    }

    public boolean hasMilitary() {
        return milUnion != null;
    }

    public String getShortName() {
        return shortName;
    }

    public Set<Short> getUnionCountries() {
        return unionCountries;
    }
}
