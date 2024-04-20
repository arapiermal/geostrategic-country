package com.erimali.cntrygame;

import java.io.Serializable;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Currencies implements Serializable {
	private static final String DEFAULT_CURR_LOC = GLogic.RESOURCESPATH + "data/";
	private Map<String, Double> unitsPerXAU;
	private String mainCurrency = "USD";
	private String playerCurrency;

	public Currencies() {
		unitsPerXAU = new HashMap<>();
		readCsvFile(DEFAULT_CURR_LOC + "currencies1JAN2024.csv");
	}

	public Currencies(String subpath) {
		unitsPerXAU = new HashMap<>();
		readCsvFile(DEFAULT_CURR_LOC + subpath);
	}

	// Units per XAU
	public double convertCurr(String from, String to, double amount) throws IllegalArgumentException {
		if (unitsPerXAU.containsKey(from) && unitsPerXAU.containsKey(to))
			return amount * (unitsPerXAU.get(to) / unitsPerXAU.get(from));
		else
			throw new IllegalArgumentException("INVALID CURRENCY");
	}

	public double convertCurr(String from, String to) throws IllegalArgumentException {
		return convertCurr(from, to, 1d);
	}

	public double convertCurrRounded(String from, String to, double amount) throws IllegalArgumentException {
		if (unitsPerXAU.containsKey(from) && unitsPerXAU.containsKey(to)) {
			double res = amount * (unitsPerXAU.get(to) / unitsPerXAU.get(from));
			return Math.round(res * 100.0) / 100.0; // Maybe Math.pow(10,round);
		} else
			throw new IllegalArgumentException("INVALID CURRENCY");
	}

	public double convertCurrRounded(String from, String to) throws IllegalArgumentException {
		return convertCurrRounded(from, to, 1d);
	}

	public double convertCurrRounded(String from, String to, double amount, int round) throws IllegalArgumentException {
		if (unitsPerXAU.containsKey(from) && unitsPerXAU.containsKey(to)) {
			double res = amount * (unitsPerXAU.get(to) / unitsPerXAU.get(from));
			double p = Math.pow(10, round);
			return Math.round(res * p) / p;
		} else
			throw new IllegalArgumentException("INVALID CURRENCY");
	}

	public double convertCurrRounded(String from, String to, int round) throws IllegalArgumentException {
		return convertCurrRounded(from, to, 1d, round);
	}

	// Add/Remove currencies
	public void addNewCurr(String name, Double perXAU) {
		if (!this.unitsPerXAU.containsKey(name))
			this.unitsPerXAU.put(name, perXAU);
	}

	public void updateCurr(String name, Double perXAU) {
		if (this.unitsPerXAU.containsKey(name))
			this.unitsPerXAU.put(name, perXAU);
	}

	public void removeCurr(String name) {
		if (this.unitsPerXAU.containsKey(name))
			this.unitsPerXAU.remove(name);
	}

	// CSV
	private void readCsvFile(String path) {
		if (unitsPerXAU != null && !unitsPerXAU.isEmpty()) {
			unitsPerXAU.clear();
		}
		// Just in case
		else {
			unitsPerXAU = new HashMap<>();
		}
		if (!path.endsWith(".csv")) {
			path += ".csv";
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line = reader.readLine(); // skip first row
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length == 2 || tokens.length == 3) {
					String currency = tokens[0].trim();
					double units = Double.parseDouble(tokens[1].trim());
					unitsPerXAU.put(currency, units);
				}
			}
		} catch (IOException | NumberFormatException e) {
			ErrorLog.logError(e);
		}
    }

	public static String getCurrencySign(String s) {
        return switch (s) {
            case "USD" -> "$";
            case "GBP" -> "£";
            case "EUR" -> "€";
            default -> s + " ";
        };

	}
	/*
	 * changeValue(int newValue){ 
	 * // game.setValue(newValue); value = newValue;
	 * changeText();
	 * }
	 */

	public String getMainCurrency() {
		return mainCurrency;
	}

	public void setMainCurrency(String mainCurrency) {
		this.mainCurrency = mainCurrency;
	}

	public String getPlayerCurrency() {
		return playerCurrency;
	}

	public void setPlayerCurrency(String playerCurrency) {
		this.playerCurrency = playerCurrency;
	}
	// or separate label?
	public String mainCurrencyDouble(double d) {
		return mainCurrency + GUtils.doubleToString(d);
	}
	public String playerCurrencyDouble(double d) {
		return playerCurrency + GUtils.doubleToString(d);
	}
}
