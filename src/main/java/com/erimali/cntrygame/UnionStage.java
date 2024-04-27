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
    Label nameLabel;
    Label typeLabel;

    public UnionStage() {
        nameLabel = new Label();
        typeLabel = new Label();

        VBox vBox = new VBox(nameLabel, typeLabel);
        Scene scene = new Scene(vBox);
        setScene(scene);
    }


    public void setFromUnion(Union u) {
        nameLabel.setText(u.getName());
        typeLabel.setText(u.toStringType());

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
                ol.add(change.getKey());
            } else if (change.wasRemoved()) {
                TESTING.print("REMOVED");
                ol.remove(change.getKey());
            }
        });

        return new ListView<>(ol);
    }

}