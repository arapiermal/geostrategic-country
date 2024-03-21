package com.erimali.cntrygame;
//abstract class for both BigAdmDiv and Country?
//Country can have BigAdmDiv
public class BigAdmDiv extends Country {
	public BigAdmDiv(String name) {
		super(name);
	}
	//Scotland,Northern Ireland, England,Wales; USA states
	private String bigAdmDivType;

	public String getBigAdmDivType() {
		return bigAdmDivType;
	}

	public void setBigAdmDivType(String bigAdmDivType) {
		this.bigAdmDivType = bigAdmDivType;
	}
}
