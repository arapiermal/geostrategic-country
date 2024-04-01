package com.erimali.cntrygame;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.ProgressBarTreeTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
//TreeView redundant if no opening Military vs Civilian subtrees...
public class BuildBuildings extends Application {
    public static void setFromProv(AdmDiv admDiv) {
        BuildBuildings.currProvBuildings = admDiv.currProvBuildings;
        BuildBuildings.buildings = admDiv.buildings;
    }

    public static class ProgressBarButtonTableCell<S extends GTask> extends TableCell<S, Double> {
        public static <S extends GTask> Callback<TableColumn<S,Double>, TableCell<S,Double>> forTableColumn() {
            return param -> new ProgressBarButtonTableCell<>();
        }
//Progress indicator or bar (?) indicator -> less space
        private final ProgressIndicator progressBar;
        private final Button button;
        private final HBox hBox;
        private ObservableValue<Double> observable;

        public ProgressBarButtonTableCell() {
            this.getStyleClass().add("progress-button-table-cell");

            this.progressBar = new ProgressIndicator();
            this.progressBar.setMaxWidth(Double.MAX_VALUE);

            this.button = new Button();
            this.button.setOnAction(event -> {
                S rowData = getTableRow().getItem();
                rowData.changeStatus();

            });
            this.hBox = new HBox(4, progressBar, button);
        }

        @Override
        public void updateItem(Double item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                progressBar.progressProperty().unbind();

                final TableColumn<S, Double> column = getTableColumn();
                observable = column == null ? null : column.getCellObservableValue(getIndex());

                if (observable != null) {
                    progressBar.progressProperty().bind(observable);
                    String txt = observable.getValue() == 0 ? "Build" : observable.getValue() == 1 ? "Demolish" : "Cancel";
                    button.setText(txt);
                    button.getStyleClass().removeAll("Build","Demolish","Cancel");
                    button.getStyleClass().add(txt);
                } else if (item != null) {
                    progressBar.setProgress(item);
                    String txt = item == 0 ? "Build" : item == 1 ? "Demolish" : "Cancel";
                    button.setText(txt);
                    button.getStyleClass().removeAll("Build","Demolish","Cancel");
                    button.getStyleClass().add(txt);
                }
                setGraphic(hBox);

            }


        }
    }
//how to make the stuff on top to be 0/4 buildings built...
    //utilize negative values from -1 to -n...
    //provId, ownerId, subjects ...

    public static TableView<BuildBuilding> makeTableView() {
        TableView<BuildBuilding> tableView = new TableView<>();
        // Columns
        TableColumn<BuildBuilding, String> nameColumn = new TableColumn<>("Building Name");
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        nameColumn.setMinWidth(140);
        TableColumn<BuildBuilding, Double> progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellValueFactory(param -> param.getValue().progressProperty());
        progressColumn.setMinWidth(140);
        //

        //progressColumn.setCellFactory(ProgressBarTreeTableCell.forTreeTableColumn());
        progressColumn.setCellFactory(ProgressBarButtonTableCell.forTableColumn());

        tableView.getColumns().addAll(nameColumn, progressColumn);

        for (Building build : Building.values()) {
            tableView.getItems().add(new BuildBuilding(build, 0.0));
        }

        return tableView;
    }

    @Override
    public void start(Stage primaryStage) {
        TableView<BuildBuilding> tableView = makeTableView();
        // Display the TreeTableView
        Scene scene = new Scene(tableView, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Progress Data in TreeTableView");

        primaryStage.show();
        buildings = EnumSet.noneOf(Building.class);
        currProvBuildings = new EnumMap<>(Building.class);
        //setValuesFromEnumMapSet(tableView);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public abstract static class GTask {
        abstract void changeStatus();

    }

    // Task class representing each task
    public static class BuildBuilding extends GTask {
        private final Building building;
        private final SimpleDoubleProperty progress;

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
    public static EnumSet<Building> buildings;

    public static void popupBuilding(BuildBuilding bb) {
        Dialog<ButtonType> dialog = new Dialog<>();
        Building b = bb.getBuilding();
        byte byteVal = buildings.contains(b) ? b.stepsToBuild : currProvBuildings.getOrDefault(b, (byte) 0);
        if (byteVal == 0) {
            dialog.setTitle("Build Building");
        }
        //Demolish
        else if (byteVal >= b.stepsToBuild) {
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
                    byte bVal = buildings.contains(b) ? b.stepsToBuild : currProvBuildings.getOrDefault(b, (byte) 0);

                    //Build
                    if (bVal == 0) {

                        bVal++;
                        currProvBuildings.put(b, bVal);

                    }
                    //Demolish
                    else if (bVal == b.stepsToBuild) {
                        //upon confirm
                        bVal = 0;
                        buildings.remove(b);
                    }
                    //Cancel
                    else {
                        bVal = 0;
                        currProvBuildings.remove(b);
                    }
                    bb.setProgress(bVal);
                }
        );

        dialog.showAndWait();

    }


}
