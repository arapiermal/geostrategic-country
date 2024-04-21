package com.erimali.cntrygame;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.util.*;

//POPUP??
public class ErrorLog {
    private static final ObservableList<String> errors = FXCollections.observableArrayList();

    static {
        errors.addListener((InvalidationListener) observable -> showAlertErrors());
    }

    public static void logError(String errorMessage) {
        errors.add(errorMessage);
    }

    public static void logError(Exception e) {
        errors.add(e.getMessage());
        e.printStackTrace();
    }

    public static String retrieveErrors() {
        StringBuilder sb = new StringBuilder();
        for (String error : errors) {
            sb.append(error).append("\n");
        }
        errors.clear();
        return sb.toString();
    }

    private static void showAlertErrors() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(retrieveErrors());
        alert.show();
    }
}
