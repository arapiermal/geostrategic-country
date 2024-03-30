package com.erimali.cntrygame;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.EnumMap;
import java.util.Map;

//Build vs Demolish button and cancel while in progress
public class BuildBuildings extends Application {
    public static class ProgressBarButtonTreeTableCell<S extends Task> extends TreeTableCell<S, Double> {
        public static <S extends Task> Callback<TreeTableColumn<S, Double>, TreeTableCell<S, Double>> forTreeTableColumn() {
            return param -> new ProgressBarButtonTreeTableCell<>();
        }

        private final ProgressBar progressBar;
        private final Button button;

        private ObservableValue<Double> observable;

        public ProgressBarButtonTreeTableCell() {
            this.getStyleClass().add("progress-bar-tree-table-cell");

            this.progressBar = new ProgressBar();
            this.progressBar.setMaxWidth(Double.MAX_VALUE);
            this.button = new Button();
            this.button.setOnAction(event -> {
                S rowData = getTreeTableRow().getItem();
                rowData.changeStatus();

            });
        }

        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                progressBar.progressProperty().unbind();

                final TreeTableColumn<S, Double> column = getTableColumn();
                observable = column == null ? null : column.getCellObservableValue(getIndex());

                if (observable != null) {
                    progressBar.progressProperty().bind(observable);
                    button.setText(item == 0 ? "Build" : item == 1 ? "Demolish" : "Cancel");
                } else if (item != null) {
                    progressBar.setProgress(item);
                    button.setText(item == 0 ? "Build" : item == 1 ? "Demolish" : "Cancel");
                }

                setGraphic(new HBox(progressBar, button));
            }
        }
    }
//how to make the stuff on top to be 0/4 buildings built...
    //utilize negative values from -1 to -n...

    public static TreeTableView<BuildBuilding> makeTreeTableView() {
        // Create a TreeTableView
        TreeTableView<BuildBuilding> treeTableView = new TreeTableView<>();
        // Define columns
        TreeTableColumn<BuildBuilding, String> nameColumn = new TreeTableColumn<>("Task Name");
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
        TreeTableColumn<BuildBuilding, Double> progressColumn = new TreeTableColumn<>("Progress");
        progressColumn.setCellValueFactory(param -> param.getValue().getValue().progressProperty());
        //

        //progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());
        progressColumn.setCellFactory(ProgressBarButtonTreeTableCell.forTreeTableColumn());

        treeTableView.getColumns().addAll(nameColumn, progressColumn);
        // Create root item and populate data

        BuildBuilding a = new BuildBuilding(Building._BUILDING, 0);

        TreeItem<BuildBuilding> root = new TreeItem<>(a);
        initTreeTableViewFromEnum(root);
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
        return treeTableView;
    }

    @Override
    public void start(Stage primaryStage) {
        TreeTableView<BuildBuilding> treeTableView = makeTreeTableView();
        // Display the TreeTableView
        Scene scene = new Scene(treeTableView, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Progress Data in TreeTableView");
        primaryStage.show();
        currProvBuildings = new EnumMap<>(Building.class);
        currProvBuildings.put(Building.MIL_AIRPORT, (byte) 50);
        setValuesFromEnumMap(currProvBuildings, treeTableView.getRoot());
    }

    public static void main(String[] args) {
        launch(args);
    }

    public abstract static class Task {
        abstract void changeStatus();
    }

    // Task class representing each task
    public static class BuildBuilding extends Task {
        private final Building building;
        private final SimpleDoubleProperty progress;
        //if add progress the moment you click button, below becomes irrelevant

        public BuildBuilding(Building building, double progress) {
            this.building = building;
            this.progress = new SimpleDoubleProperty(progress);
        }

        public Building getBuilding() {
            return building;
        }

        public String getName() {
            return building.toString();
        }

        public double getProgress() {
            return progress.get();
        }

        public void setProgress(double d) {
            progress.set(d);
        }

        public void setProgress(byte b) {
            double val = (double) b / building.stepsToBuild;
            progress.set(val);
        }

        public ObservableValue<String> nameProperty() {
            return new SimpleStringProperty(getName());
        }

        public ObservableValue<Double> progressProperty() {
            return progress.asObject();
        }

        @Override
        public void changeStatus() {
            popupBuilding(this);
        }
    }

    public static EnumMap<Building, Byte> currProvBuildings;

    //SelectedProvince -> EnumMap<Building,Byte> currBuilding; EnumSet<Building> finishedBuildings;
    //
    public static void popupBuilding(BuildBuilding bb) {
        double val = bb.getProgress();
        Building b = bb.getBuilding();
        byte byteVal = currProvBuildings.getOrDefault(b, (byte) 0);
        //Build
        if (val == 0) {
            byteVal++;

            val += (double) 1 / b.stepsToBuild;
        }
        //Demolish
        else if (val == 1) {
            //upon confirm
            byteVal = 0;
        }
        //Cancel
        else {
            byteVal = 0;
        }
        currProvBuildings.put(b, byteVal);
        bb.setProgress(byteVal);
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
            else if (b.isOther())
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
        for (Map.Entry<Building, Byte> b : buildings.entrySet()) {
            if (b.getKey().isMilitary()) {
                mil.getChildren().get(m++).getValue().setProgress(b.getValue());
            } else if (b.getKey().isDiplomatic()) {
                dip.getChildren().get(d++).getValue().setProgress(b.getValue());
            } else if (b.getKey().isOther()) {
                others.getChildren().get(o++).getValue().setProgress(b.getValue());
            }
        }
    }
}
