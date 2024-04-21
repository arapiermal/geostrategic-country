package com.erimali.cntrymilitary;

import java.io.Serializable;

public class Person implements Serializable {
    private static final char MALE = 'M';
    private static final char FEMALE = 'F';

    private String firstName;
    private String middleName;
    private String lastName;
    private char gender;

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
        if (s.length < 2) {
            throw new IllegalArgumentException("NOT ENOUGH INPUT");
        }
        int i = 0;
        this.firstName = s[i++];
        if (s.length == 4)
            this.middleName = s[i++];
        this.lastName = s[i++];
        if (s.length >= 3)
            this.gender = s[i].charAt(0);
    }

    public String toString() {
        return firstName + " " + lastName;
    }

    public String getFullName() {
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

    public boolean hasMiddleName() {
        return middleName != null;
    }

    public static char getMale() {
        return MALE;
    }

    public static char getFemale() {
        return FEMALE;
    }

    public boolean isMale(){
        return gender == MALE;
    }

    public boolean isFemale(){
        return gender == FEMALE;
    }
}
