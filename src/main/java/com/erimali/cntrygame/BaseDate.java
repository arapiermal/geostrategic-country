package com.erimali.cntrygame;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BaseDate implements Comparable<BaseDate>, Serializable {

	protected int day, month, year;

	public BaseDate(int day, int month, int year) throws IllegalArgumentException {
		if (checkValidDate(day, month, year)) {
			this.year = year;
			this.month = month;
			this.day = day;
		} else {
			throw new IllegalArgumentException("Invalid date");
		}
	}

	public BaseDate(int day, int month, int year, boolean incorrect) {
		if (checkValidDate(day, month, year)) {
			this.year = year;
			this.month = month;
			this.day = day;
		} else if (incorrect) {
			this.year = year;
			this.month = month % 12;
			this.year += month / 12;
			this.day = day % LAST_DAY_OF_MONTH[this.month];
			addDays(day - this.day);
		}
	}

	public BaseDate(String date) throws IllegalArgumentException {
		String[] in = date.split("/");
		int day, month, year;
		if (in.length == 3) {
			try {
				day = Integer.parseInt(in[0]);
				month = Integer.parseInt(in[1]);
				year = Integer.parseInt(in[2]);
				if (checkValidDate(day, month, year)) {
					this.year = year;
					this.month = month;
					this.day = day;
				} else {
					throw new IllegalArgumentException("Invalid date");
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid date format");
			}
		} else {
			throw new IllegalArgumentException("Invalid date format length");
		}
	}

	public void addDays(int numDays) {
		int totalDays = day + numDays;
		if (totalDays > 0) {
			while (totalDays > getLastDayOfMonth()) {
				totalDays -= getLastDayOfMonth();
				if (month == 12) {
					month = 1;
					year++;
				} else {
					month++;
				}
			}
			day = totalDays;
		} else {
			while (totalDays <= 0) {
				if (month == 1) {
					month = 12;
					year--;
				} else {
					month--;
				}
				totalDays += getLastDayOfMonth();
			}
			day = totalDays;
		}
	}

	private static final String[] MONTH_NAMES = { "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" };
	private static final int[] LAST_DAY_OF_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	protected int getLastDayOfMonth() {
		if (month < 1 || month > 12) {
			return -1;
		}
		int lastDay = LAST_DAY_OF_MONTH[month - 1];
		if (month == 2 && isLeapYear()) {
			lastDay = 29;
		}
		return lastDay;
	}

	protected boolean isLeapYear() {
		return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
	}

	public boolean changeDate(int day, int month, int year) {
		if (checkValidDate(day, month, year)) {
			this.day = day;
			this.month = month;
			this.year = year;
			return true;
		} else {
			return false;
		}
	}

	// STATIC
	public static boolean checkValidDate(int day, int month, int year) {
		if (month < 1 || month > 12) {
			return false;
		}
		if (day < 1 || day > getLastDayOfMonth(month, year)) {
			return false;
		}
		return true;
	}

	public static int getLastDayOfMonth(int month, int year) {
		if (month < 1 || month > 12) {
			return -1;
		}
		int lastDay = LAST_DAY_OF_MONTH[month - 1];
		if (month == 2 && isLeapYear(year)) {
			lastDay = 29;
		}
		return lastDay;
	}

	public static boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
	}

	// toString

	public String toString() {
		return String.format("%02d/%02d/%d", day, month, year);
	}

	public String toStringLong() {
		String dayOrder;
		if (day == 1 || day == 21 || day == 31)
			dayOrder = "st";
		else if (day == 2 || day == 22)
			dayOrder = "nd";
		else if (day == 3 || day == 23)
			dayOrder = "rd";
		else
			dayOrder = "th"; // NOT MOST EFFICIENT (?)
		return String.format("%d%s %s %d", day, dayOrder, MONTH_NAMES[month - 1], year);
	}

	public String toString(char c) {
		return String.format("%02d%c%02d%c%d", day, c, month, c, year);
	}

	@Override
	public int compareTo(BaseDate o) {
		int compYear = Integer.compare(this.year, o.getYear());
		if (compYear != 0)
			return compYear;
		int compMonth = Integer.compare(this.month, o.getMonth());
		if (compMonth != 0)
			return compMonth;
		return Integer.compare(this.day, o.getDay());
	}

	public boolean equals(BaseDate o) {
        return this.day == o.day && this.month == o.month && this.year == o.year;
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	// CALCULATE DIF
	public int difInYears(BaseDate o) {
		return this.year - o.year;
	}

	public int difInMonths(BaseDate o) {
		int difYear = this.year - o.year;
		int difMonths = this.month - o.month;
		if (difMonths < 0) {
			difMonths += 12;
			difYear--;
		} else if (difMonths > 11) {
			difMonths -= 12;
			difYear++;
		}
		difMonths += difYear * 12;
		return difMonths;
	}

	public int difInDays(BaseDate o) {
		return (int) ChronoUnit.DAYS.between(toLocalDate(), o.toLocalDate());
	}

	public LocalDate toLocalDate() {
		return LocalDate.of(year, month, day);
	}

	public String getMonthName() {
		return MONTH_NAMES[month];
	}
}
