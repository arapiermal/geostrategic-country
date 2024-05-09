package com.erimali.cntrygame;

import java.io.Serializable;

public class CSubject implements Serializable {
    private Country main;
    private Country subject;
    private SubjectType subjectType;
    private double taxation;
    private int independenceDesire;

    public CSubject(Country main, Country subject, SubjectType subjectType) {
        this.main = main;
        this.subject = subject;
        this.subjectType = subjectType;
        this.subject.setSubjectOf(this);
        this.taxation = subjectType.getTaxation();
    }

    //If by war independence desire bigger by default
    public CSubject(Country main, Country subject, SubjectType subjectType, int independenceDesire) {
        this.main = main;
        this.subject = subject;
        this.subjectType = subjectType;
        this.independenceDesire = independenceDesire;
        this.subject.setSubjectOf(this);
        this.taxation = subjectType.getTaxation();
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

    public void incTaxation(double amount) {
        if (amount > 0) {
            taxation += amount;
            if (taxation > 1.0)
                taxation = 1.0;
            incIndDesire((int) (amount * 15));
        }
    }

    public void decTaxation(double amount) {
        if (amount > 0) {
            taxation -= amount;
            if (taxation < 0.0)
                taxation = 0.0;
            decIndDesire((int) (amount * 15));
        }
    }

    public void incIndDesire(int amount) {
        if (amount > 0) {
            independenceDesire += amount;
            if (independenceDesire > 100) {
                independenceDesire = 100;
                //declareIndependence();
            }
        }
    }

    public void decIndDesire(int amount) {
        if (amount > 0) {
            independenceDesire -= amount;
            if (independenceDesire < 0)
                independenceDesire = 0;

        }
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
    public War declareIndependence() {
        main.releaseSubject(subject.getCountryId());
        War independenceWar = new War(subject, main, CasusBelli.INDEPENDENCE);

        return independenceWar;
    }

    public void changeSubjectType(int i) {
        this.subjectType = SubjectType.values()[i];
    }

    public void changeSubjectType(SubjectType s) {
        this.subjectType = s;
    }

    public Country getSubject() {
        return subject;
    }

    public int getMainId() {
        return main.getCountryId();
    }

    public int getSubjectId() {
        return subject.getCountryId();
    }

    public void taxSubject() {
        if (taxation <= 0.0)
            return;
        double lastMonthBalance = subject.getEconomy().getLastMonthBalance();
        double amount;
        if (lastMonthBalance > 0)
            amount = lastMonthBalance * taxation;
        else
            amount = subject.getEconomy().getTreasury() * taxation / 12;
        if (amount > 0) {
            main.getEconomy().incTreasury(amount);
            subject.getEconomy().decTreasury(amount);
        }
    }

    public void yearlyTick() {
        decIndDesire(10);
    }

    public boolean isAtGoodTerms() {
        short rel = main.getRelations((short) subject.getCountryId());
        return (independenceDesire == 0 && rel >= 0) || (independenceDesire < 50 && rel >= 200);
    }
}
