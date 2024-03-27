package com.erimali.cntrygame;

public class Ruler extends Person {
	private String type;

	public Ruler(String type, String firstName, String lastName, char gender) {
		super(firstName, lastName, gender);
		this.type = type;
	}
	public Ruler(String input) {
		super(input.substring(input.indexOf("->")+2));//!!!!!!!!!!!!!
		int index = input.indexOf("->");
		this.type = input.substring(0,index).trim();
	}
	@Override
	public String toString() {
		return type + " - " + super.toString();
	}
}
