package com.erimali.cntrygame;

import java.io.Serializable;

public class Person implements Serializable {
	private String firstName;
	private String middleName;
	private String lastName;
	private char gender;
	private static final char MALE = 'M';
	private static final char FEMALE = 'F';
	public Person(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}
	public Person(String firstName, String middleName, String lastName) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
	}

	public Person(String firstName, String lastName, char gender) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
	}
	public Person(String firstName, String middleName, String lastName, char gender) {
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.gender = gender;
	}

	public Person(String input) {
		String[] s = input.split(","); //\\s+,\\s+
		if(s.length < 3) {
			throw new IllegalArgumentException("NOT ENOUGH INPUT");
		}
		int i = 0;
		this.firstName = s[i++];
		if(s.length == 4)
			this.middleName = s[i++];
		this.lastName = s[i++];
		this.gender = s[i].charAt(0);
	}

	public String toString() {
		return firstName + " " + lastName;
	}

	public String getFullName(){
		return firstName + " " + middleName + " " + lastName;
	}

	public String getFirstName() {
		return firstName;
	}


	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
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

	public boolean hasMiddleName(){
		return middleName != null;
	}

	public static char getMale() {
		return MALE;
	}


	public static char getFemale() {
		return FEMALE;
	}


}
