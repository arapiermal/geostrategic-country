package com.erimali.cntrygame;

import com.erimali.cntrymilitary.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.Serializable;
import java.util.EnumMap;

public class Government implements Serializable {
    //private static GovTypes; // load from file?
    //improve type?
    private String type;
    private boolean bothTheSame;
    private String headOfStateType;
    private Ruler headOfState;// ruling monarch, executive president -> main
    // constitutional monarch, non-executive president -> not main
    private boolean isHeadOfStateStronger;
    private String headOfGovernmentType;
    private Ruler headOfGovernment;
    private int stability;
    private float corruption; //yearly effect on stability
    private float corruptionGrowth; //monthly
    private int publicOpinion;

    //Elections/Ruler change


    private int lastElectionYear;
    private int electionPeriod;
    private int yearsUntilNextElection;

    // Import from txt? default policies for all countries, specific
    // policies that execute CommandLine? every year/month/day
    // private List<Policy> policies;

    // TAXATION
    // private double taxes;
    // Government spendings?

    //Policies
    private ObservableMap<GovPolicy, Integer> policies;

    public Government(String type, Ruler ruler) {
        this.type = type;
        this.headOfState = ruler;
        this.headOfGovernment = ruler;
        this.bothTheSame = true;
        this.policies = FXCollections.observableMap(new EnumMap<>(GovPolicy.class));
        setTypesFromRulers();
    }

    public Government(String type, Ruler headOfState, Ruler headOfGovernment) {
        this.type = type;
        this.headOfState = headOfState;
        this.headOfGovernment = headOfGovernment;
        this.policies = FXCollections.observableMap(new EnumMap<>(GovPolicy.class));
        setTypesFromRulers();
    }

    public Government(String type, Ruler headOfState, Ruler headOfGovernment, boolean isHeadOfStateStronger) {
        this.type = type;
        this.headOfState = headOfState;
        this.headOfGovernment = headOfGovernment;
        this.isHeadOfStateStronger = isHeadOfStateStronger;
        this.policies = FXCollections.observableMap(new EnumMap<>(GovPolicy.class));
        setTypesFromRulers();
    }

    public void setRulerSame(Ruler ruler) {
        this.headOfState = ruler;
        this.headOfGovernment = ruler;
        this.bothTheSame = true;
    }

    public void setRulerSame(Person person) {
        String type = isHeadOfStateStronger ? headOfState.getType() : headOfGovernment.getType();
        Ruler ruler = new Ruler(type, person);
        setRulerSame(ruler);
    }

    public void setTypesFromRulers() {
        headOfStateType = headOfState.getType();
        headOfGovernmentType = headOfGovernment.getType();
    }

    public boolean sameType(Government o) {
        return type.equalsIgnoreCase(o.type) && holdsElections() == o.holdsElections();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void incStability() {
        stability++;
        if (stability > 100)
            stability = 100;
    }

    public void decStability() {
        stability--;
        if (stability < -100)
            stability = -100;
    }

    public void incStability(int amount) {
        if(amount > 0) {
            stability += amount;
            if (stability > 100)
                stability = 100;
        }
    }

    public void decStability(int amount) {
        if(amount > 0) {
            stability -= amount;
            if (stability < -100)
                stability = -100;
        }
    }

    public void addStability(int amount){
        stability += amount;
        if (stability > 100)
            stability = 100;
        else if (stability < -100)
            stability = -100;
    }

    public int getStability() {
        return stability;
    }

    public void setStability(int stability) {
        if (stability > 100)
            stability = 100;
        else if (stability < -100)
            stability = -100;
        this.stability = stability;
    }


    public String toStringRulers() {
        StringBuilder sb = new StringBuilder();
        if (bothTheSame) {
            return headOfState.toString();
        } else {
            if (isHeadOfStateStronger) {
                sb.append("Head of State\n").append(headOfState.toString()).append("\nHead of Government\n")
                        .append(headOfGovernment.toString());
            } else {
                sb.append("Head of Government\n").append(headOfGovernment.toString()).append("\nHead of State\n")
                        .append(headOfState.toString());
            }
        }
        return sb.toString();
    }

    public String toStringMainRuler() {
        if (bothTheSame || isHeadOfStateStronger)
            return headOfState.toString();
        else
            return headOfGovernment.toString();
    }

    public ObservableMap<GovPolicy, Integer> getPolicies() {
        return policies;
    }


    public void addPolicy(GovPolicy policy, int years) {
        if (!policies.containsKey(policy))
            policies.put(policy, years);
    }

    public void setPolicy(GovPolicy policy, int years) {
        if (policies.containsKey(policy))
            policies.put(policy, years);
    }

    public void removePolicy(GovPolicy policy) {
        if (policy.isRemovable()) {
            policies.remove(policy);
        }
    }

    public void removePolicy(GovPolicy policy, boolean admin) {
        policies.remove(policy);
    }

    public void yearlyTick() {

        yearlyReduceFromPolicies();
    }

    public boolean hasElectionsThisYear() {
        if (holdsElections()) {
            yearsUntilNextElection--;
            if (yearsUntilNextElection == 0) {
                yearsUntilNextElection = electionPeriod;
                return true;
            }
        }
        return false;
    }

    public void changeLeadership(Person person) {
        String type = isHeadOfStateStronger ? headOfStateType : headOfGovernmentType;
        Ruler ruler = new Ruler(type, person);
        if (bothTheSame) {
            headOfGovernment = ruler;
            headOfState = ruler;
        } else if (isHeadOfStateStronger) {
            headOfState = ruler;
        } else {
            headOfGovernment = ruler;
        }
    }

    public void changeNonPrimaryLeadership(Person person) {
        if (bothTheSame)
            return;
        String type = !isHeadOfStateStronger ? headOfStateType : headOfGovernmentType;
        Ruler ruler = new Ruler(type, person);
        if (!isHeadOfStateStronger) {
            headOfState = ruler;
        } else {
            headOfGovernment = ruler;
        }
    }

    public void abolishElections() {
        electionPeriod = 0;
        yearsUntilNextElection = -1;
    }

    public boolean holdsElections() {
        return electionPeriod > 0;
    }

    public void yearlyReduceFromPolicies() {
        for (GovPolicy p : policies.keySet()) {
            int yearsLeft = policies.get(p) - 1;
            if (yearsLeft < 0) {
                policies.remove(p);
            } else {
                policies.put(p, yearsLeft);
            }
        }

    }

    public Ruler getHeadOfState() {
        return headOfState;
    }

    public void setHeadOfState(Ruler headOfState) {
        this.headOfState = headOfState;
    }

    public void setHeadOfState(Person headOfState) {
        this.headOfState = new Ruler(this.headOfState.getType(), headOfState);
    }

    public Ruler getHeadOfGovernment() {
        return headOfGovernment;
    }

    public void setHeadOfGovernment(Ruler headOfGovernment) {
        this.headOfGovernment = headOfGovernment;
    }

    public void setHeadOfGovernment(Person headOfGovernment) {
        this.headOfGovernment = new Ruler(this.headOfGovernment.getType(), headOfGovernment);
        ;
    }

    public boolean canDeclareWar() {
        return !(policies.containsKey(GovPolicy.NEUTRALITY) || policies.containsKey(GovPolicy.FORCED_NEUTRALITY) || policies.containsKey(GovPolicy.BANNED_MILITARY));
    }

    public int getElectionPeriod() {
        return electionPeriod;
    }

    public void setElectionPeriod(int electionPeriod) {
        this.electionPeriod = electionPeriod;
    }

    public int getYearsUntilNextElection() {
        return yearsUntilNextElection;
    }

    public int getLastElectionYear() {
        return lastElectionYear;
    }

    public void setLastElectionYear(int lastElectionYear) {
        this.lastElectionYear = lastElectionYear;
    }

    public void setYearsUntilNextElectionFromCurrYear(int currYear) {
        //error if lastElection > currYear
        if (electionPeriod <= 0)
            return;
        int periodsSinceLastElection = (currYear - lastElectionYear) / electionPeriod;
        int nextElectionYear = lastElectionYear + (periodsSinceLastElection + 1) * electionPeriod;
        this.yearsUntilNextElection = nextElectionYear - currYear;

    }

    public boolean isBothTheSame() {
        return bothTheSame;
    }

    public short researchBoost() {
        return policies.containsKey(GovPolicy.INCENTIVIZE_RESEARCH) ? (short) 10 : 0;
    }

    public boolean hasPolicy(GovPolicy govPolicy) {
        return policies.containsKey(govPolicy);
    }
}
