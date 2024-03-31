package com.erimali.cntrygame;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

//Build vs Demolish button and cancel while in progress
public class BuildBuildings extends Application {
    public static class ProgressBarButtonTreeTableCell<S extends Task> extends TreeTableCell<S, Double> {
        public static <S extends Task> Callback<TreeTableColumn<S, Double>, TreeTableCell<S, Double>> forTreeTableColumn() {
            return param -> new ProgressBarButtonTreeTableCell<>();
        }

        private final ProgressBar progressBar;
        private final Button button;
        private final HBox hBox;
        private ObservableValue<Double> observable;

        public ProgressBarButtonTreeTableCell() {
            this.getStyleClass().add("progress-bar-tree-table-cell");

            this.progressBar = new ProgressBar();
            this.progressBar.setMaxWidth(Double.MAX_VALUE);

            this.button = new Button();
            this.button.setOnAction(event -> {
                S rowData = getTableRow().getItem();
                rowData.changeStatus();

            });
            this.hBox = new HBox(8, progressBar, button);
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
                    button.setText(observable.getValue() == 0 ? "Build" : observable.getValue() == 1 ? "Demolish" : "Cancel");

                } else if (item != null) {
                    progressBar.setProgress(item);
                    button.setText(item == 0 ? "Build" : item == 1 ? "Demolish" : "Cancel");

                }
                setGraphic(hBox);

            }


        }
    }
//how to make the stuff on top to be 0/4 buildings built...
    //utilize negative values from -1 to -n...
    //provId, ownerId, subjects ...

    public static TreeTableView<BuildBuilding> makeTreeTableView() {
        // Create a TreeTableView
        TreeTableView<BuildBuilding> treeTableView = new TreeTableView<>();
        // Define columns
        TreeTableColumn<BuildBuilding, String> nameColumn = new TreeTableColumn<>("Task Name");
        nameColumn.setCellValueFactory(param -> param.getValue().getValue().nameProperty());
        nameColumn.setMinWidth(160);
        TreeTableColumn<BuildBuilding, Double> progressColumn = new TreeTableColumn<>("Progress");
        progressColumn.setCellValueFactory(param -> param.getValue().getValue().isRootLike() ? null : param.getValue().getValue().progressProperty());
        progressColumn.setMinWidth(160);
        //

        //progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());
        progressColumn.setCellFactory(ProgressBarButtonTreeTableCell.forTreeTableColumn());

        treeTableView.getColumns().addAll(nameColumn, progressColumn);
        // Create root item and populate data

        BuildBuilding a = new BuildBuilding(Building._BUILDING, -2);

        TreeItem<BuildBuilding> root = new TreeItem<>(a);
        initTreeTableViewFromEnum(root);
        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
        //treeTableView.setMinWidth(200);

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
        buildings = EnumSet.noneOf(Building.class);
        currProvBuildings = new EnumMap<>(Building.class);

        currProvBuildings.put(Building.MIL_AIRPORT, (byte) 2);
        setValuesFromEnumMapSet(treeTableView.getRoot());
    }

    public static void main(String[] args) {
        launch(args);
    }

    public abstract static class Task {
        abstract void changeStatus();

        abstract boolean isRootLike();
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

        @Override
        public boolean isRootLike() {
            return building.isRootLike();
        }
    }

    public static EnumMap<Building, Byte> currProvBuildings;
    public static EnumSet<Building> buildings;

    //SelectedProvince -> EnumMap<Building,Byte> currBuilding; EnumSet<Building> finishedBuildings;
    //
    public static void popupBuilding(BuildBuilding bb) {
        Dialog<ButtonType> dialog = new Dialog<>();
        Building b = bb.getBuilding();
        byte byteVal = currProvBuildings.getOrDefault(b, (byte) 0);
        if (byteVal == 0) {
            dialog.setTitle("Build Building");
        }
        //Demolish
        else if (byteVal == b.stepsToBuild) {
            dialog.setTitle("Demolish Building");
        }
        //Cancel
        else {
            dialog.setTitle("Cancel Building");
        }
        VBox vBox = new VBox(new Label("Name: " + b.toString()), new Label("Cost: $" + b.price), new Label("Time (Months): " + b.stepsToBuild));
        dialog.getDialogPane().setContent(vBox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    byte bVal = currProvBuildings.getOrDefault(b, (byte) 0);

                    //Build
                    if (bVal == 0) {

                        bVal++;

                    }
                    //Demolish
                    else if (bVal == b.stepsToBuild) {
                        //upon confirm
                        bVal = 0;
                    }
                    //Cancel
                    else {
                        bVal = 0;
                    }
                    currProvBuildings.put(b, bVal);
                    bb.setProgress(bVal);
                }
        );

        dialog.showAndWait();

    }

    public static void initTreeTableViewFromEnum(TreeItem<BuildBuilding> root) {
        Building[] builds = Building.values();
        for (int i = 1; i < builds.length; i++) {
            root.getChildren().add(new TreeItem<>(new BuildBuilding(builds[i], 0.0)));
        }

    }

    public static void setValuesFromEnumMapSet(TreeItem<BuildBuilding> root) {
        /*for(Building b : Building.values()){
            if(currProvBuildings.containsKey(b)){
                byte val = currProvBuildings.get(b);
                if (b.isMilitary()) {
                    mil.getChildren().get(m++).getValue().setProgress(val);
                } else if (b.isDiplomatic()) {
                    dip.getChildren().get(d++).getValue().setProgress(val);
                } else if (b.isOther()) {
                    others.getChildren().get(o++).getValue().setProgress(val);
                }
            }
        }*/
/*
        for (Map.Entry<Building, Byte> b : currProvBuildings.entrySet()) {
            if (b.getKey().isMilitary()) {
                mil.getChildren().get(m++).getValue().setProgress(b.getValue());
            } else if (b.getKey().isDiplomatic()) {
                dip.getChildren().get(d++).getValue().setProgress(b.getValue());
            } else if (b.getKey().isOther()) {
                others.getChildren().get(o++).getValue().setProgress(b.getValue());
            }
        }
*/
        Building[] builds = Building.values();
        for (int i = 1; i < builds.length; i++) {
            BuildBuilding bb = root.getChildren().get(i++).getValue();
            Building b = builds[i];
            if (buildings.contains(b)) {
                if (bb != null) bb.setProgress(1.0);
            } else if (currProvBuildings.containsKey(b)) {
                if (bb != null) bb.setProgress(currProvBuildings.get(b));
            } else {
                if (bb != null) bb.setProgress(0.0);
            }
        }
    }

}
