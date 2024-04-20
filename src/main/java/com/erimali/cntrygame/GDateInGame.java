package com.erimali.cntrygame;

public class GDateInGame extends GDate {
	public GLogic game;
	public GDateInGame(int day, int month, int year, GLogic game) throws IllegalArgumentException {
		super(day, month, year);
		this.game = game;
	}
	public GDateInGame(int day, int month, int year, int dayOfWeek, GLogic game) throws IllegalArgumentException {
		super(day, month, year, dayOfWeek);
		this.game = game;

	}
	public GDateInGame(int day, int month, int year, boolean incorrect, GLogic game) {
		super(day, month, year, incorrect);
		this.game = game;

	}

	public GDateInGame(String date, GLogic game) throws IllegalArgumentException {
		super(date);
		this.game = game;
	}

	@Override
	public void nextDay() {
		dayOfWeek = (dayOfWeek + 1) % 7;
		if(dayOfWeek == 0){
			game.weeklyTick();
		}
		if (day < getLastDayOfMonth()) {
			day++;
		} else {
			day = 1;
			if (month == 12) {
				month = 1;
				year++;
				game.yearlyTick();
			} else {
				month++;
			}
			game.monthlyTick();
		}
	}
}
