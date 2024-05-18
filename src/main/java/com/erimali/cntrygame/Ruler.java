package com.erimali.cntrygame;

import com.erimali.cntrymilitary.Person;

enum RulerType {

}

public class Ruler extends Person {
    private static final int MAX_AGE = 125;
    private String type;
    private int age;

    //add properties for bonuses in Country game
    public Ruler(String type, int age, String firstName, String lastName, char gender) {
        super(firstName, lastName, gender);
        this.type = type;
        if(age < 18){
            this.age = (int) (Math.random() * 60) + 18;
        }
    }

    public Ruler(String input) {
        super(input.substring(input.indexOf("->") + 2));//!!!!!!!!!!!!!
        int index = input.indexOf("->");
        this.type = input.substring(0, index).trim();
        int ageCheck = this.type.indexOf(',');
        if (ageCheck > 0) {
            this.age = GUtils.parseI(this.type.substring(ageCheck + 1));
            this.type = this.type.substring(0, ageCheck);
        }
    }

    public Ruler(String type, Person person) {
        super(person.getFirstName(), person.getMiddleName(), person.getLastName(), person.getGender());
        this.type = type;
        this.age = (int) (Math.random() * 60) + 18;
    }

    @Override
    public String toString() {
        return type + " - " + super.toString();
    }

    public String toStringLong() {
        return toString() + " (" + super.getGender() + ") " + age + " years old";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getOlder() {
        age++;
        double probOfDeath = (double) age / MAX_AGE;
        return Math.random() < probOfDeath;
    }
}
