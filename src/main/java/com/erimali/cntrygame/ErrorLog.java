package com.erimali.cntrygame;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ErrorLog {
    private static final ObservableList<Exception> errors = FXCollections.observableArrayList();
    private static final Dialog<Void> alertErrors = new Dialog<>();
    private static final ListView<Exception> listViewErrors = new ListView<>(errors);
    private static final Alert detailedViewAlert = new Alert(Alert.AlertType.INFORMATION);
    private static final TextArea textArea = new TextArea();

    static {
        textArea.setEditable(false);
        detailedViewAlert.setGraphic(textArea);
        alertErrors.setTitle("Error");
        alertErrors.setOnCloseRequest(e -> alertErrors.hide());
        alertErrors.getDialogPane().setContent(listViewErrors);

        ButtonType saveButtonType = new ButtonType("Save");
        ButtonType viewButtonType = new ButtonType("View Selected");//all other errors are gone
        alertErrors.getDialogPane().getButtonTypes().addAll(saveButtonType, viewButtonType, ButtonType.CLOSE);

        alertErrors.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                saveErrorLogs();
            } else if (dialogButton == viewButtonType) {
                Exception selectedError = listViewErrors.getSelectionModel().getSelectedItem();
                if (selectedError != null) {
                    textArea.setText(getStackTraceAsString(selectedError));
                    detailedViewAlert.show();
                }
            }
            errors.clear();
            return null;
        });


    }

    public static void logError(String errorMessage) {
        //errors.add(errorMessage);
        Exception exception = new IllegalArgumentException(errorMessage);
        Platform.runLater(() -> {
            errors.add(exception);
            showAlertErrors();
        }); //thread safety!
    }

    private static void updateListViewHeight() {
        double prefHeight = Math.min(Math.max(errors.size() * 24, 100), 600);
        listViewErrors.setPrefHeight(prefHeight);
    }

    public static void logError(Exception e) {
        // errors.add(e.getMessage());
        Platform.runLater(() -> {
            errors.add(e);
            e.printStackTrace(); //!!!
            showAlertErrors();
        }); //thread safety!
    }

    private static void showAlertErrors() {
        updateListViewHeight();
        alertErrors.show();
        TESTING.print("HELLO");
    }

    public static String getStackTraceAsString(Throwable throwable) {
        if (throwable == null)
            return "none";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    public static void saveErrorLogs() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String timestamp = now.format(formatter);
        String currentDir = System.getProperty("user.dir"); // Should be game (userdata) directory
        String logsDirPath = currentDir + File.separator + "logs"; // !!!
        File logsDir = new File(logsDirPath);
        if (!logsDir.exists()) {
            if (!logsDir.mkdirs()) {
                System.err.println("Error: Could not create directory to save file - " + logsDirPath);
                return;
            }
        }
        // Show where to save(?)
        String filename = logsDirPath + File.separator + "log_error_" + timestamp + ".txt";
        try (FileWriter writer = new FileWriter(filename)) {
            for (Exception e : errors) {
                writer.write(getStackTraceAsString(e) + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
