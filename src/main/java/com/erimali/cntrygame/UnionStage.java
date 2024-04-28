package com.erimali.cntrygame;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;

public class UnionStage extends Stage {
    Label[] labels;


    public UnionStage() {
        labels = new Label[5];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new Label();
        }

        VBox vBox = new VBox(labels);
        Scene scene = new Scene(vBox);
        setScene(scene);
    }


    public void setFromUnion(Union u) {
        labels[0].setText(u.getName());
        labels[1].setText(u.toStringType());
        labels[2].setText("Stability: " + u.getStability());
        labels[3].setText(String.format("Centralization: %.1f", u.getCentralization()));
        labels[4].setText(String.format("Funds: $%.1f", u.getFunds()));

    }

    public static ListView<Union> makeListViewUnions(ObservableMap<String, Union> unions) {
        ObservableList<Union> ol = FXCollections.observableArrayList(unions.values());

        unions.addListener((MapChangeListener<String, Union>) change -> {
            if (change.wasAdded()) {
                ol.add(change.getValueAdded());
            } else if (change.wasRemoved()) {
                ol.remove(change.getValueRemoved());
            }
        });

        ListView<Union> lv = new ListView<>(ol);

        return lv;
    }

    public static ListView<String> makeListViewUnionsString(ObservableMap<String, Union> unions) {
        ObservableList<String> ol = FXCollections.observableArrayList(unions.keySet());

        unions.addListener((MapChangeListener<String, Union>) change -> {
            if (change.wasAdded()) {
                TESTING.print("ADDED");
                if (ol.contains(change.getKey()))
                    ol.add(change.getKey());
            } else if (change.wasRemoved()) {
                TESTING.print("REMOVED");
                ol.remove(change.getKey());
            }
        });

        return new ListView<>(ol);
    }

}