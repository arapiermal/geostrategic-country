package com.erimali.cntrygame;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.EnumMap;
import java.util.EnumSet;

public class BuildBuildings {
    //Store gamestage reference ?
    private final GameStage gameStage;

    public BuildBuildings(GameStage gameStage){
        this.gameStage = gameStage;
    }

    private EnumMap<Building, Byte> currBuildingBuildings;
    private EnumSet<Building> buildings;

    public void setFromProv(AdmDiv admDiv) {
        currBuildingBuildings = admDiv.getBuildingBuildings();
        buildings = admDiv.getBuildings();
    }

    public static class ProgressButtonTableCell<S extends GTask> extends TableCell<S, Double> {
        public static <S extends GTask> Callback<TableColumn<S,Double>, TableCell<S,Double>> forTableColumn() {
            return param -> new ProgressButtonTableCell<>();
        }
//Progress indicator or bar (?) indicator -> less space
        private final ProgressIndicator progressBar;
        private final Button button;
        private final HBox hBox;
        private ObservableValue<Double> observable;

        public ProgressButtonTableCell() {
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

    public TableView<BuildBuildingTask> makeTableView() {
        TableView<BuildBuildingTask> tableView = new TableView<>();
        // Columns
        TableColumn<BuildBuildingTask, String> nameColumn = new TableColumn<>("Building Name");
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        nameColumn.setMinWidth(140);
        TableColumn<BuildBuildingTask, Double> progressColumn = new TableColumn<>("Progress");
        progressColumn.setCellValueFactory(param -> param.getValue().progressProperty());
        progressColumn.setMinWidth(120);
        //
        progressColumn.setCellFactory(ProgressButtonTableCell.forTableColumn());

        tableView.getColumns().add(nameColumn);
        tableView.getColumns().add(progressColumn);

        for (Building build : Building.values()) {
            tableView.getItems().add(new BuildBuildingTask(this, build, 0.0));
        }

        return tableView;
    }

    public abstract static class GTask {
        abstract void changeStatus();

    }

    public static class BuildBuildingTask extends GTask {
        private final BuildBuildings buildBuildings;
        private final Building building;
        private final SimpleDoubleProperty progress;

        public BuildBuildingTask(BuildBuildings buildBuildings, Building building, double progress) {
            this.buildBuildings = buildBuildings;
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
            double val = (double) b / building.getStepsToBuild();
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
            buildBuildings.popupBuilding(this);
        }

    }

    public void popupBuilding(BuildBuildingTask bb) {
        Dialog<ButtonType> dialog = new Dialog<>();
        Building b = bb.getBuilding();
        byte byteVal = buildings.contains(b) ? b.getStepsToBuild() : currBuildingBuildings.getOrDefault(b, (byte) 0);
        if (byteVal == 0) {
            dialog.setTitle("Build Building");
        }
        //Demolish
        else if (byteVal >= b.getStepsToBuild()) {
            dialog.setTitle("Demolish Building");
        }
        //Cancel
        else {
            dialog.setTitle("Cancel Building");
        }
        VBox vBox = new VBox(new Label("Name: " + b.toString()), new Label("Cost: $" + b.getPrice()), new Label("Time (Months): " + b.getStepsToBuild()));
        dialog.getDialogPane().setContent(vBox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    byte bVal = buildings.contains(b) ? b.getStepsToBuild() : currBuildingBuildings.getOrDefault(b, (byte) 0);
                    //Build
                    if (bVal == 0) {
                        if(gameStage.getGame().canPurchase(b.getPrice())) {
                            gameStage.getGame().spendTreasury(b.getPrice());
                            bVal++;
                            currBuildingBuildings.put(b, bVal);
                        } else{
                            gameStage.showAlert(Alert.AlertType.WARNING, "Insufficient treasury", "You don't have the money $" + b.getPrice());
                        }
                    }
                    //Demolish
                    else if (bVal == b.getStepsToBuild()) {
                        //upon confirm
                        bVal = 0;
                        buildings.remove(b);
                    }
                    //Cancel
                    else {
                        double moneyBack = b.getPrice() *  ((double) (b.getStepsToBuild() - bVal) / b.getStepsToBuild());
                        gameStage.getGame().addTreasury(moneyBack);
                        gameStage.showAlert(Alert.AlertType.INFORMATION, "Cancelled building", "You got back $" + moneyBack);
                        bVal = 0;
                        currBuildingBuildings.remove(b);
                    }
                    bb.setProgress(bVal);
                }
        );
        dialog.showAndWait();
    }


}
