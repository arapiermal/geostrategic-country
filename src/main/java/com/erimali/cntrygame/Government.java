package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public class Government implements Serializable {
    //private static GovTypes; // load from file?
    //improve type?
    private String type;
    private boolean bothTheSame;
    private Ruler headOfState;// ruling monarch, executive president -> main
    // constitutional monarch, non-executive president -> not main
    private boolean isHeadOfStateStronger;
    private Ruler headOfGovernment;
    private int stability;

    private int publicOpinion;
    // Import from txt? default policies for all countries, specific
    // policies that execute CommandLine? every year/month/day
    // private List<Policy> policies;

    // TAXATION
    // private double taxes;
    // Government spendings?


    //Policies
    private Map<GovPolicy, Integer> policies;


    public Government(String type, Ruler ruler) {
        this.type = type;
        this.headOfState = ruler;
        this.headOfGovernment = ruler;
        this.bothTheSame = true;
        this.policies = new EnumMap<>(GovPolicy.class);
    }

    public Government(String type, Ruler headOfState, Ruler headOfGovernment) {
        this.type = type;
        this.headOfState = headOfState;
        this.headOfGovernment = headOfGovernment;
        this.policies = new EnumMap<>(GovPolicy.class);

    }

    public Government(String type, Ruler headOfState, Ruler headOfGovernment, boolean isHeadOfStateStronger) {
        this.type = type;
        this.headOfState = headOfState;
        this.headOfGovernment = headOfGovernment;
        this.isHeadOfStateStronger = isHeadOfStateStronger;
        this.policies = new EnumMap<>(GovPolicy.class);

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getStability() {
        return stability;
    }

    public void setStability(int stability) {
        this.stability = stability;
    }

    public String toStringRulers() {
        StringBuilder sb = new StringBuilder();
        if (bothTheSame) {
            return headOfState.toString();
        } else {
            //maybe head of state always at beginning?
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

    public Map<GovPolicy, Integer> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<GovPolicy, Integer> policies) {
        this.policies = policies;
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

    //by default CPolicy not removable (?)
    public void reduceOneYearFromPolicies() {
        //for(Map.Entry<CPolicy, Integer> entry : this.policies)
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

    public Ruler getHeadOfGovernment() {
        return headOfGovernment;
    }

    public void setHeadOfGovernment(Ruler headOfGovernment) {
        this.headOfGovernment = headOfGovernment;
    }

}
