package com.erimali.cntrygame;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;

import java.io.*;

public class SaveGame {
    //does every class/object inside need serializable!??!?!
    protected static String saveGamePath = "saveGames/";
    protected static final String saveExtension = ".save";
    protected static final ObservableList<String> saves = FXCollections.observableArrayList();

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
            ioException.printStackTrace();
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
            ioException.printStackTrace();
            return null;
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
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
