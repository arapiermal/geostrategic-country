package com.erimali.cntrygame;

import com.erimali.cntrymilitary.Military;
import javafx.scene.paint.Paint;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

enum UnionPolicies {
    FREE_TRADE(Union.ECONOMIC),
    COMMON_CURRENCY(Union.ECONOMIC),
    INFRASTRUCTURE_INVESTMENT(Union.ECONOMIC),
    MIL_PROTECTION(Union.MILITARY),
    //Every year set mil tech to country with the biggest/ or +1
    MIL_SHARE_TECH(Union.MILITARY) {
        public void action() {

        }
    },
    //COMMON_GOV(Union.ECONOMIC | Union.POLITICAL) //2 in 1
    ;
    final int type;

    UnionPolicies(int type) {
        this.type = type;
    }

    public static EnumSet<UnionPolicies> getPoliciesType(int i) {
        EnumSet<UnionPolicies> set = EnumSet.noneOf(UnionPolicies.class);
        for (UnionPolicies p : UnionPolicies.values()) {
            if (p.type == i)
                set.add(p);
        }
        return set;
    }
}

//if UN -> fully centralized => WorldGovernment (!!!!!!!!!!!!!!!!!!!!!)
public class Union implements Serializable {
    protected final static int MAX_TYPES = 3;
    protected final static int DISUNITED = 0; //DisUnited Nations
    protected final static int POLITICAL = 1; //Diplomatic+Government
    protected final static int ECONOMIC = 2;
    protected final static int MILITARY = 4;
    protected final static int ALL_TYPES = addTypes(ECONOMIC, POLITICAL, MILITARY);

    public static String typeToString(int type) {
        return switch (type) {
            case POLITICAL -> "Political";
            case ECONOMIC -> "Economic";
            case MILITARY -> "Military";
            default -> "";
        };
    }

    public static int addTypes(int... types) {
        int type = 0;
        for (int i : types)
            if (i > 0 && (type & i) == 0)
                type |= i;
        return type;
    }

    protected static int genType(String in) {
        int type = 0;
        String[] s = in.trim().toUpperCase().split("\\s*,\\s*");
        for (int i = 0; i < s.length; i++) {
            if (s[i].length() < 3)
                continue;
            int val = switch (s[i].substring(0, 3)) {
                case "POL" -> POLITICAL;
                case "ECO" -> ECONOMIC;
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

    private final String shortName;
    private final World world; //CountryArray...
    private EnumSet<Continent> validContinents;
    private int type;
    private String name;
    private Paint color;
    private int stability; // for the UN -> the lower, the less
    private float centralization;
    private double funds;

    private Set<Integer> unionCountries;
    private byte[] influence;
    //invest can increase influence ... power of country...

    // power?//Influence?
    private Government govUnion; //can be null
    //UnionGovernment extends Government, UnionGovPolicies
    private Military milUnion; // can be null
    private EnumSet<UnionPolicies> policies;
    // private UPanel panel;//accessible by the player for choices inside the union
    // event-like?
    private String[][] choices;
    private String[][] commands; // hmmm
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
        for (int i : countries) {
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
        influence = new byte[CountryArray.getMaxIso2Countries()];
        for (int i = 0; i < influence.length; i++) {
            influence[i] = unionCountries.contains(i) ? (byte) 1 : (byte) -1;
        }
    }

    public void uniteAllCountries(int mainCountry) {
        CountryArray cArray = world.getCountries();
        Country c = cArray.get(mainCountry);
        c.uniteWith(name, cArray, unionCountries);
        world.removeUnion(shortName);
    }

    public boolean canUnite() {
        return type == ALL_TYPES && centralization >= 100;
    }

    public boolean canJoin(int i) {
        Country c = world.getCountry(i);
        if (c != null) {

        }
        return false;
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

    public void giveFunds(short cId, double amount) {
        if (unionCountries.contains(cId) && amount >= funds) {
            Country c = world.getCountry(cId);
            funds -= amount;
            c.getEconomy().incTreasury(amount);
        }
    }

    public void gainFunds(double amount) {
        for (int i : unionCountries) {
            Country c = world.getCountry(i);
            funds += amount;
            c.getEconomy().decTreasury(amount);

        }
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
        if (influence[i] > 100)
            influence[i] = 100;
    }

    public void decInfluence(short i, byte amount) {
        if (amount > 0 || i < 0 || i >= influence.length || !unionCountries.contains(i))
            return;
        influence[i] += amount;
        if (influence[i] < 0)
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

    public Set<Integer> getUnionCountries() {
        return unionCountries;
    }

    public double getFunds() {
        return funds;
    }

    public void setFunds(double funds) {
        this.funds = funds;
    }

    //vote to add type military to EU
    //POL + ECO + new MIL + 100% centralization = European Country
    public void voteAI(Country player, int vote, int choice) {
        //AI calc based on rel and vote on influence
        short[] v = votes.get(vote);
        int cId = player.getCountryId();
        v[choice] += influence[cId];
        for (int i : unionCountries) {
            short rel = player.getRelations(i);
            if (Math.random() * rel > Math.sqrt(rel)) {
                v[choice] += influence[i];
            } else {
                v[(int) (Math.random() * v.length)] += influence[i];
            }
        }
    }

    public void yearlyTick() {
        int n = unionCountries.size();
        if (hasType(ECONOMIC)) {
            if (funds > 0 && policies.contains(UnionPolicies.INFRASTRUCTURE_INVESTMENT)) {
                for (int i : unionCountries) {
                    funds -= funds / n;
                    Country c = world.getCountry(i);
                    c.incInfrastructure(c.getAdmDivRandom());
                    if (funds <= 0)
                        break;
                }
            }
        }
    }

    public String toStringType() {
        StringBuilder sb = new StringBuilder();
        if (hasType(POLITICAL))
            sb.append("Political");
        if (hasType(ECONOMIC)) {
            if (!sb.isEmpty())
                sb.append(" & ");
            sb.append("Economic");
        }
        if (hasType(MILITARY)) {
            if (!sb.isEmpty())
                sb.append(" & ");
            sb.append("Military");
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public boolean containsCountry(int playerId) {
        return unionCountries.contains(playerId);
    }

    public boolean applyToJoin(int playerId) {
        return false;
    }

    public boolean applyToLeave(int playerId) {
        return false;
    }

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


    public void setColor(Paint paint) {
        this.color = paint;
    }

    public void setColor(String s) {
        color = Paint.valueOf(s);
    }

    public Paint getColor() {
        return color;
    }
    //on declared war on member of union -> stability hit
}
