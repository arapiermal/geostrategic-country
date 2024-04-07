package com.erimali.cntrygame;

import java.io.Serializable;

public class CSubject implements Serializable {
	private Country main;
	private Country subject;
	private SubjectType subjectType;
	private int independenceDesire;
	
	public CSubject(Country main, Country subject, SubjectType subjectType) {
		this.main = main;
		this.subject = subject;
		this.subjectType = subjectType;
		this.subject.setSubjectOf(this);
	}
	//If by war independence desire bigger by default
	public CSubject(Country main, Country subject, SubjectType subjectType, int independenceDesire) {
		this.main = main;
		this.subject = subject;
		this.subjectType = subjectType;
		this.independenceDesire = independenceDesire;
		this.subject.setSubjectOf(this);
	}

	public Country getMain() {
		return main;
	}

	public void setMain(Country main) {
		this.main = main;
	}

	public int getIndependenceDesire() {
		return independenceDesire;
	}

	public void setIndependenceDesire(int independenceDesire) {
		this.independenceDesire = independenceDesire;
	}

	@Override
	public String toString() {
		return subject.getName() + "\n" + toStringSubjectType() + " of " + main.getName();
	}
	public String toStringShort() {
		return toStringSubjectType() + " of " + main.getName();
	}
	public String toStringSubjectType() {
		return subjectType.toString();
	}

	// WAR FOR INDEPENDENCE
	public void declareIndependence() {
		main.releaseSubject(subject.getCountryId());
		War independenceWar = new War(subject, main, CasusBelli.INDEPENDENCE);
		//CommandLine.getGs().getGame()
		//where to put war
	}

	public void changeSubjectType(int i){
		this.subjectType = SubjectType.values()[i];
	}
	public void changeSubjectType(SubjectType s){
		this.subjectType = s;
	}
	public Country getSubject() {
		return subject;
	}

	public void setSubject(Country subject) {
		this.subject = subject;
	}
	public int getMainId() {
		return main.getCountryId();
	}
	public int getSubjectId() {
		return subject.getCountryId();
	}
}
