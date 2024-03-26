package com.erimali.cntrygame;

public class GDate extends BaseDate {
	protected int dayOfWeek;
	protected static final String[] DAYS_OF_WEEK = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
			"Sunday" };

	public GDate(int day, int month, int year) throws IllegalArgumentException {
		super(day, month, year);
	}
	public GDate(int day, int month, int year, int dayOfWeek) throws IllegalArgumentException {
		super(day, month, year);
		if(dayOfWeek < 0 || dayOfWeek > 6)
			throw new IllegalArgumentException("INVALID DAY OF WEEK");
		this.dayOfWeek = dayOfWeek;
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
	public boolean equals(GDate o) {
        return this.day == o.getDay() && this.month == o.getMonth() && this.year == o.getYear();
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
}
