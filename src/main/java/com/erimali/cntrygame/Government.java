package com.erimali.cntrygame;

import java.util.Map;

public class Government {
	//private static GovTypes; // load from file?
	//improve type?
	private String type;
	private boolean bothTheSame;
	private Ruler headOfState;// ruling monarch, executive president -> main
	// constitutional monarch, non-executive president -> not main
	private boolean isHeadOfStateStronger;
	private Ruler headOfGovernment;
	private byte stability;
	
	private byte publicOpinion;
	// Import from txt? default policies for all countries, specific
	// policies that execute CommandLine? every year/month/day
	// private List<Policy> policies;

	// TAXATION
	// private double taxes;
	// Government spendings?
	
	

	//Policies
	private Map<CPolicy, Integer> policies;
	

	public Government(String type, Ruler ruler) {
		this.type = type;
		this.headOfState = ruler;
		this.headOfGovernment = ruler;
		this.bothTheSame = true;
	}

	public Government(String type, Ruler headOfState, Ruler headOfGovernment) {
		this.type = type;
		this.headOfState = headOfState;
		this.headOfGovernment = headOfGovernment;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte getStability() {
		return stability;
	}

	public void setStability(byte stability) {
		this.stability = stability;
	}

	public String toStringRulers() {
		StringBuilder sb = new StringBuilder();
		if (bothTheSame) {
			return this.headOfState.toString();
		} else {
			//maybe head of state always at beginning?
			if (isHeadOfStateStronger) {
				sb.append("Head of State\n").append(this.headOfState.toString()).append("\nHead of Government\n")
						.append(this.headOfGovernment.toString());
			} else {
				sb.append("Head of Government\n").append(this.headOfGovernment.toString()).append("\nHead of State\n")
						.append(this.headOfState.toString());
			}
		}
		return sb.toString();
	}
	public String toStringMainRuler() {
		if(bothTheSame || isHeadOfStateStronger)
			return this.headOfState.toString();
		else
			return this.headOfGovernment.toString();
	}
	public Map<CPolicy, Integer> getPolicies() {
		return policies;
	}

	public void setPolicies(Map<CPolicy, Integer> policies) {
		this.policies = policies;
	}
	
	public void addPolicy(CPolicy policy, int years) {
		if(!this.policies.containsKey(policy))
			this.policies.put(policy, years);
	}
	public void setPolicy(CPolicy policy, int years) {
		if(this.policies.containsKey(policy))
			this.policies.put(policy, years);
	}
	public void removePolicy(CPolicy policy) {
		if(policy.isCanRemove()) {
			this.policies.remove(policy);
		}
	}
	public void reduceOneYearFromPolicies() {
		//for(Map.Entry<CPolicy, Integer> entry : this.policies) 
		for(CPolicy p : policies.keySet()) {
			int yearsLeft = policies.get(p) - 1;
			if(yearsLeft < 0) {
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
