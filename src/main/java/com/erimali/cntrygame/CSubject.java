package com.erimali.cntrygame;

public class CSubject {
	private Country main;
	private Country subject;
	private int subjectType;
	private int independenceDesire;
	
	public CSubject(Country main, Country subject, int subjectType) {
		this.main = main;
		this.subject = subject;
		this.subjectType = subjectType;
		this.subject.setSubjectOf(this);
	}
	//If by war independence desire bigger by default
	public CSubject(Country main, Country subject, int subjectType, int independenceDesire) {
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

	public int getSubjectType() {
		return subjectType;
	}

	public void setSubjectType(int subjectType) {
		this.subjectType = subjectType;
	}

	@Override
	public String toString() {
		return subject.getName() + "\n" + toStringSubjectType() + " of " + main.getName();
	}
	public String toStringShort() {
		return toStringSubjectType() + " of " + main.getName();
	}
	public String toStringSubjectType() {
		return SubjectTypes.values()[subjectType].getName();
	}

	// WAR FOR INDEPENDENCE
	public void declareIndependence() {
		main.releaseSubject(subject.getIso2());
		War independenceWar = new War(subject, main, "Independence");
		//CommandLine.getGs().getGame()
		//where to put war
	}

	public Country getSubject() {
		return subject;
	}

	public void setSubject(Country subject) {
		this.subject = subject;
	}
	public String getMainIso2() {
		return main.getIso2();
	}
	public String getSubjectIso2() {
		return subject.getIso2();
	}
}
