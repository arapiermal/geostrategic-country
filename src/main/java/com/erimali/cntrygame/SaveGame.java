package com.erimali.cntrygame;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class SaveGame {
    protected static String saveGamePath = "saveGames/";
    protected static final String saveExtension = ".save";
    protected static final ObservableList<String> saves = FXCollections.observableArrayList();

    public static boolean deleteSaveGame(String fileName) {
        try {
            File file = new File(saveGamePath + fileName);
            if (file.exists()) {
                if (alertConfirmation("Delete", "Delete save-game: " + fileName + "?")) {
                    if (file.delete()) {
                        return true;
                    } else {
                        ErrorLog.logError("Failed to delete file");
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                ErrorLog.logError("File does not exist");
                return true;
            }
        } catch (SecurityException e) {
            ErrorLog.logError("Security exception occurred while deleting file - " + fileName);
            return false;
        }
    }

    public static List<String> deleteSaveGames(List<? extends String> list) {
        List<String> failed = new LinkedList<>();
        for (String fileName : list) {

        }
        return failed;
    }

    public static boolean alertConfirmation(String title, String desc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(desc);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        ButtonType result = alert.showAndWait().orElse(ButtonType.NO);
        return result == ButtonType.YES;
    }

    public static void saveGame(String name, GLogic g) {
        try {
            if (name.indexOf('.') < 0) {
                name += saveExtension;
            }
            FileOutputStream fileOut = new FileOutputStream(saveGamePath + name);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(g);
            out.close();
            fileOut.close();
            if (!saves.contains(name)) {
                saves.add(name);
            }
        } catch (IOException ioException) {
            ErrorLog.logError(ioException);
        }
    }

    public static GLogic loadGame(String name) {
        try {
            if (name.indexOf('.') < 0)
                name += saveExtension;
            FileInputStream fileIn = new FileInputStream(saveGamePath + name);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            GLogic g = (GLogic) in.readObject();
            in.close();
            fileIn.close();
            return g;
        } catch (IOException ioException) {
            ErrorLog.logError(ioException);
            return null;
        } catch (ClassNotFoundException c) {
            ErrorLog.logError(c);
            return null;
        }
    }

    public static void loadSaveGamePaths() {
        try {
            File directory = new File(saveGamePath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(saveExtension)) {
                        saves.add(f.getName());
                    }
                }
            }
        } catch (Exception e) {

        }
    }

}
