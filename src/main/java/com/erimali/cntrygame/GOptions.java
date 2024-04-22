package com.erimali.cntrygame;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

enum Settings {
    FULLSCREEN, VOLUME, CLI, TRANSLATEGEVENT, MODS;

    int defValue;
}

public class GOptions {
    private static final String DEF_SETTINGS_PATH = GLogic.RESOURCESPATH + "settings.ini";
    private static String modsPath = "mods/";

    private static boolean fullScreen = false;
    private static double volume = 0.5;
    private static boolean translateGEvent = true;
    private static boolean allowCLI = true;
    private static boolean allowMods = false;


    public static void loadGOptions() {
        try (BufferedReader br = new BufferedReader(new FileReader(DEF_SETTINGS_PATH))) {
            Map<String, Integer> settings = new HashMap<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank())
                    continue;
                try {
                    String[] setting = line.trim().split("\\s*:\\s*", 3);

                    if (setting.length == 3) {
                        if (setting[0].equalsIgnoreCase("MODS")) {
                            modsPath = readStringTrim(setting[2]);
                        }
                    }
                    if (setting.length >= 2) {
                        try {
                            String v = setting[1];
                            if (v.equals("DEFAULT")) {
                                continue;
                            }
                            int value = Integer.parseInt(v);
                            settings.put(setting[0].toUpperCase(), value);
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException(setting[0].toUpperCase() + " not Integer");
                        }

                    }
                } catch (IllegalArgumentException iae) {
                    ErrorLog.logError(iae);
                }
            }
            setSettingsFromMap(settings);
        } catch (Exception e) {
            saveToFile();
        }
    }

    private static void setSettingsFromMap(Map<String, Integer> settings) {
        //FULLSCREEN
        int fsval = settings.getOrDefault(Settings.FULLSCREEN.toString(), 0);
        fullScreen = fsval != 0;
        volume = settings.getOrDefault(Settings.VOLUME.toString(), 50);
        if (volume < 0)
            volume = 0;
        else if (volume > 100)
            volume = 100;
        volume /= 100;
        int enCLI = settings.getOrDefault(Settings.CLI.toString(), 1);
        allowCLI = enCLI != 0;
        int trGEvent = settings.getOrDefault(Settings.TRANSLATEGEVENT.toString(), 1);
        translateGEvent = trGEvent != 0;
        int enMods = settings.getOrDefault(Settings.MODS.toString(), 0);
        allowMods = enMods != 0;
    }

    public static void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DEF_SETTINGS_PATH))) {
            writer.write(Settings.FULLSCREEN + boolToIntString(fullScreen));
            writer.newLine();
            writer.write(Settings.VOLUME + ":" + (int) (volume * 100));
            writer.newLine();
            writer.write(Settings.CLI + boolToIntString(allowCLI));
            writer.newLine();
            writer.write(Settings.TRANSLATEGEVENT + boolToIntString(translateGEvent));
            writer.newLine();
            writer.write(Settings.MODS + boolToIntString(allowMods) + ":\"" + modsPath + "\"");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readStringTrim(String in) {
        int start = in.indexOf('\"') + 1;
        int end = in.lastIndexOf('\"') - 1;
        if (end > start) {
            while(start < end && Character.isWhitespace(in.charAt(start))){
                start++;
            }
            while(end > start && Character.isWhitespace(in.charAt(end))){
                end--;
            }
            return in.substring(start, end + 1);
        } else
            return in;
    }
    public static String boolToIntString(boolean b) {
        return b ? ":1" : ":0";
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

    public static double getVolume() {
        return volume;
    }

    public static int getVolumeInt() {
        return (int) (volume * 100);
    }

    public static void setVolume(int volume) {
        GOptions.volume = (double) volume / 100;
    }

    public static boolean isAllowCLI() {
        return allowCLI;
    }

    public static void setAllowCLI(boolean allowCLI) {
        GOptions.allowCLI = allowCLI;
    }


    public static void changeDirectoryPath(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Mods Directory");
        directoryChooser.setInitialDirectory(Paths.get(modsPath).toFile());
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            modsPath = selectedDirectory.getAbsolutePath();
        } else {

        }
    }

    public static boolean hasMods() {
        //check filesystem
        return false;
    }

    public static boolean isAllowMods() {
        return allowMods;
    }

    public static void setAllowMods(boolean allowMods) {
        GOptions.allowMods = allowMods;
    }

}
