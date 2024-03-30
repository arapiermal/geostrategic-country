package com.erimali.cntrygame;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

//Build vs Demolish button and cancel while in progress
public class BuildBuildings extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create a TreeTableView
        TreeTableView<BuildBuilding> treeTableView = new TreeTableView<>();
        // Define columns
        TreeTableColumn<BuildBuilding, String> nameColumn = new TreeTableColumn<>("Task Name");
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
        TreeTableColumn<BuildBuilding, Double> progressColumn = new TreeTableColumn<>("Progress");
        progressColumn.setCellValueFactory(param -> param.getValue().getValue().progressProperty());
        //
        progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());
        //
        TreeTableColumn<BuildBuilding, Integer> buttonsColumn = new TreeTableColumn<>("Status");
        buttonsColumn.setCellValueFactory(param -> param.getValue().getValue().statusProperty());
        buttonsColumn.setCellFactory(column -> {
            return new TreeTableCell<>() {
                @Override
                protected void updateItem(Integer status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        // Customize the cell content (e.g., add buttons, icons, etc.)
                        //setText(status);

                        Button button = new Button(status == 0 ? "Build" : status == 1 ? "Demolish" : "Cancel");
                        button.setOnAction(event -> {
                            BuildBuilding rowData = getTreeTableRow().getItem();
                            // Perform actions with rowData (e.g., open a dialog, update data, etc.)
                        });

                        setGraphic(button);
                    }
                }
            };
        });

        treeTableView.getColumns().addAll(nameColumn, progressColumn, buttonsColumn);
        // Create root item and populate data

        BuildBuilding a = new BuildBuilding(Building._BUILDING, 0);

        TreeItem<BuildBuilding> root = new TreeItem<>(a);
        initTreeTableViewFromEnum(root);
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
        // Display the TreeTableView
        Scene scene = new Scene(treeTableView, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Progress Data in TreeTableView");
        primaryStage.show();
        EnumMap<Building, Byte> buildings = new EnumMap<>(Building.class);
        buildings.put(Building.MIL_AIRPORT, (byte) 50);
        setValuesFromEnumMap(buildings, root);
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Task class representing each task
    public static class BuildBuilding {
        private final Building building;
        private final SimpleDoubleProperty progress;
        //if add progress the moment you click button, below becomes irrelevant
        private SimpleIntegerProperty buildingStatus;

        public BuildBuilding(Building building, double progress) {
            this.building = building;
            this.progress = new SimpleDoubleProperty(progress);
            this.buildingStatus = new SimpleIntegerProperty(progress == 0 ? 0 : progress == 1 ? 1 : -1);
        }
        public Building getBuilding(){
            return building;
        }
        public String getName() {
            return building.toString();
        }

        public double getProgress() {
            return progress.get();
        }

        public boolean addProgress(double amount) {
            progress.set(progress.get() + amount);
            if(progress.get() >= 1.0) {
                progress.set(1.0);
                return true;
            }
            return false;
        }
        public void setStatus(int status){
            buildingStatus.set(status);
        }
        public void setProgress(byte b){
            double val = (double) b / 100;
            progress.set(val);
            buildingStatus.set(val == 0 ? 0 : val == 1 ? 1 : -1);
        }
        public ObservableValue<String> nameProperty() {
            return new SimpleStringProperty(getName());
        }

        public ObservableValue<Double> progressProperty() {
            return progress.asObject();
        }

        public ObservableValue<Integer> statusProperty() {
            return buildingStatus.asObject();
        }
    }


    public static void initTreeTableViewFromEnum(TreeItem<BuildBuilding> root) {
        TreeItem<BuildBuilding> mil = new TreeItem<>(new BuildBuilding(Building._MIL, 0.0));
        TreeItem<BuildBuilding> dip = new TreeItem<>(new BuildBuilding(Building._DIP, 0.0));
        TreeItem<BuildBuilding> others = new TreeItem<>(new BuildBuilding(Building._OTHERS, 0.0));
        root.getChildren().addAll(mil, dip, others);

        for (Building b : Building.values()) {
            if (b.isMilitary())
                mil.getChildren().add(new TreeItem<>(new BuildBuilding(b, 0.0)));
            else if (b.isDiplomatic())
                dip.getChildren().add(new TreeItem<>(new BuildBuilding(b, 0.0)));
            else
                others.getChildren().add(new TreeItem<>(new BuildBuilding(b, 0.0)));
        }
    }

    public static void setValuesFromEnumMap(EnumMap<Building, Byte> buildings, TreeItem<BuildBuilding> root) {
        TreeItem<BuildBuilding> mil = root.getChildren().get(0);
        TreeItem<BuildBuilding> dip = root.getChildren().get(1);
        TreeItem<BuildBuilding> others = root.getChildren().get(2);
        int m = 0;
        int d = 0;
        int o = 0;
        for (Map.Entry<Building,Byte> b : buildings.entrySet()) {
           if(b.getKey().isMilitary()){
               mil.getChildren().get(m++).getValue().setProgress(b.getValue());
           } else if(b.getKey().isDiplomatic()){
               dip.getChildren().get(d++).getValue().setProgress(b.getValue());
           } else{
               others.getChildren().get(o++).getValue().setProgress(b.getValue());
           }
        }
    }
}
