package com.erimali.cntrygame;

//instead abstract ... CPolicy, and positive/negative/removable
public enum CPolicy {
	//Positive
	//Negative/imposed policies
	BANNEDMILITARY(false);

	CPolicy(boolean canRemove) {
		this.setCanRemove(canRemove);
	}
	CPolicy(boolean b, int year) {
		// TODO Auto-generated constructor stub
		// can you substract year from policy?
	}
	public boolean isCanRemove() {
		return canRemove;
	}
	public void setCanRemove(boolean canRemove) {
		this.canRemove = canRemove;
	}
	
	private boolean canRemove;
}
