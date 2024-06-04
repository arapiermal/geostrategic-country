package com.erimali.cntrygame;

public class GDate extends BaseDate {
    protected int dayOfWeek;
    protected static final String[] DAYS_OF_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
            "Sunday"};

    public GDate(int day, int month, int year) throws IllegalArgumentException {
        super(day, month, year);
        this.dayOfWeek = calcDayOfWeek(day, month, year);
    }

    public GDate(int day, int month, int year, boolean incorrect) {
        super(day, month, year, incorrect);
    }

    public GDate(String date) throws IllegalArgumentException {
        super(date);
    }

    public void nextDay() {
        dayOfWeek = (dayOfWeek + 1) % 7;
        if (day < getLastDayOfMonth()) {
            day++;
        } else {
            day = 1;
            if (month == 12) {
                month = 1;
                year++;
            } else {
                month++;
            }
        }
    }

    public String toStringDayOfWeek() {
        return DAYS_OF_WEEK[dayOfWeek];
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isFirstDayOfWeek() {
        return dayOfWeek == 0;
    }

    public boolean isFirstDayOfMonth() {
        return day == 1;
    }

    public boolean isFirstDayOfYear() {
        return day == 1 && month == 1;
    }

    public static int calcDayOfWeek(BaseDate date) {
        return calcDayOfWeek(date.getDay(), date.getMonth(), date.getYear());
    }

    public static int calcDayOfWeek(int day, int month, int year) {
        //Zeller's Congruence
        if (month < 3) {
            month += 12;
            year -= 1;
        }
        int c = year / 100;
        year = year % 100;
        int h = (c / 4 - 2 * c + year + year / 4 + 13 * (month + 1) / 5 + day - 1) % 7;
        return ((h + 5) % 7); //Monday -> 0
        //return (h + 7) % 7; //Saturday -> 0
        //LocalDate localDate = LocalDate.of(year, month, day);
        //return localDate.getDayOfWeek().ordinal();
    }

    public BaseDate getBaseCopy() {
        return new BaseDate(day, month, year);
    }
}
