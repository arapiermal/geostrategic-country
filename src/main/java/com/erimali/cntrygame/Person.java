package com.erimali.cntrygame;

public class Person {
	private String firstName;
	private String lastName;
	private char gender;
	private static final char MALE = 'M';
	private static final char FEMALE = 'F';
	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public Person(String firstName, String lastName, char gender) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
	}
	public Person(String input) {
		String[] s = input.split(","); //\\s+,\\s+
		if(s.length < 3) {
			throw new IllegalArgumentException("LITTLE INPUT");
		}
		this.firstName = s[0];
		this.lastName = s[1];
		this.gender = s[2].charAt(0);
	}

	public String toString() {
		return getFirstName() + " " + getLastName();
	}
	

	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}


	public String getLastName() {
		return lastName;
	}


	public void setLastName(String lastName) {
		this.lastName = lastName;
	}


	public char getGender() {
		return gender;
	}


	public void setGender(char gender) {
		this.gender = gender;
	}


	public static char getMale() {
		return MALE;
	}


	public static char getFemale() {
		return FEMALE;
	}
	
}
