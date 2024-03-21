package com.erimali.cntrygame;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Disclaimer {

    private final static String disclaimer = "This game does not promote or condone activities that are considered illegal under national or international law, such as:\n" +
            " - Committing war crimes\n" +
            " - Declaring wars for no (valid) reason\n" +
            " - Engaging in cyber warfare against random countries or infrastructure (without justification)\n" +
            "Etc.";
    public static Alert generateDisclaimer(){
        Alert a = new Alert(Alert.AlertType.WARNING, disclaimer, ButtonType.OK);
        a.setTitle("Disclaimer");
        a.setWidth(600);
        a.setHeight(400);
        return a;
    }
}
