package com.erimali.cntrygame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

enum Settings {
	FULLSCREEN,VOLUME,TRANSLATEGEVENT;
}

public class GOptions {
	private static final String DEF_SETTINGSPATH = GLogic.RESOURCESPATH+"settings.ini";
	private static boolean fullScreen = false;
	private static int volume = 50;
	private static boolean translateGEvent = true;
	public static void loadGOptions() {
		try (BufferedReader br = new BufferedReader(new FileReader(DEF_SETTINGSPATH))) {
			Map<String, Integer> settings = new HashMap<>();
			String line;
			while ((line = br.readLine()) != null) {
				if (line.isBlank())
					continue;
				try {
					String[] setting = line.split(":");
					if (setting.length == 2) {
						try {
							String v = setting[1].trim();
							if(v.equals("DEFAULT")) {
								continue;
							}
							int value = Integer.parseInt(v);
							settings.put(setting[0].trim().toUpperCase(), value);
						} catch (NumberFormatException nfe) {
							throw new IllegalArgumentException(setting[0].trim().toUpperCase()+ " not Integer");
						}

					} else {
						throw new IllegalArgumentException("Not of type -> SETTING:VALUE");
					}
				} catch (IllegalArgumentException iae) {
					ErrorLog.logError(iae);
				}
			}
			setSettingsFromMap(settings);

		} catch (Exception e) {
			defaultSettings();
		}
	}

	private static void setSettingsFromMap(Map<String, Integer> settings) {
		//FULLSCREEN
		int fsval = settings.getOrDefault(Settings.FULLSCREEN.toString(), 0);
		fullScreen = fsval != 0;
		int trGEvent = settings.getOrDefault(Settings.TRANSLATEGEVENT.toString(), 1);
		translateGEvent = trGEvent != 0;
		volume = settings.getOrDefault(Settings.VOLUME.toString(), 50);
		if(volume < 0)
			volume = 0;
		else if(volume > 100)
			volume = 100;

		
	}
	//Unnecessary if we have .getOrDefault() and have initialized them with default values (instead of 0 use default?!?)
	public static void defaultSettings() {
		//empty settings.ini so the default values can be retrieved on restart (?)
	}

	public static void saveToFile() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEF_SETTINGSPATH))) {
			writer.write(Settings.FULLSCREEN.toString() + (fullScreen ? ":1" : ":0"));
			writer.newLine();
			writer.write(Settings.TRANSLATEGEVENT.toString() + (translateGEvent ? ":1" : ":0"));
			writer.newLine();
            writer.write(Settings.VOLUME.toString() +":" + String.valueOf(volume));
            writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isFullScreen() {
		return fullScreen;
	}

	public static void setFullScreen(boolean fullScreen) {
		GOptions.fullScreen = fullScreen;
	}

	public static boolean isTranslateGEvent() {
		return translateGEvent;
	}

	public static void setTranslateGEvent(boolean translateGEvent) {
		GOptions.translateGEvent = translateGEvent;
	}

	public static int getVolume() {
		return volume;
	}

	public static void setVolume(int volume) {
		GOptions.volume = volume;
	}

}
