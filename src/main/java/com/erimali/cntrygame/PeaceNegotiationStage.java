package com.erimali.cntrygame;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.EnumSet;


public class PeaceNegotiationStage extends Stage {
    private final GameStage gameStage;
    private War war;
    private EnumSet<WarObjective> warObjectives; //can be done with a CheckListView
    private DoubleProperty warScoreRequired;
    private ListSelectionView<AdmDiv> listSelectionView;
    private ObservableList<AdmDiv> selectedProvinces;
    private Button dealButton;
    private Label totalWarScoreLabel;
    //fix when moving back and front the stuff in list... OBSERVABLE LIST INSIDE WAR IS TOUCHED!!!
    public PeaceNegotiationStage(GameStage gameStage) {
        this.gameStage = gameStage;
        this.selectedProvinces = FXCollections.observableArrayList();
        this.warScoreRequired = new SimpleDoubleProperty(0);

        totalWarScoreLabel = new Label();
        Label warScoreRequiredLabel = new Label();
        warScoreRequiredLabel.textProperty().bind(warScoreRequired.asString());
        Region reg = new Region();
        HBox hBox = new HBox(4, new Label("Total score: "), totalWarScoreLabel, reg, new Label("Required score: "), warScoreRequiredLabel);
        HBox.setHgrow(reg, Priority.ALWAYS);
        makeProvinceListSelectionView();
        Tab provinceLSV = new Tab("Annex", listSelectionView);
        Tab warObjectives = new Tab("War Objectives");

        TabPane tabPane = new TabPane(provinceLSV, warObjectives);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        dealButton = new Button("Deal"); //deal button makes other button useless !!!!!
        dealButton.setOnAction(e -> dealPeace());
        VBox vBox = new VBox(hBox, tabPane, dealButton);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        Scene scene = new Scene(vBox);
        setScene(scene);
    }

    private void dealPeace() {
        if(warScoreRequired.get() == 0){
            //white peace
        } else {
            gameStage.getGame().forcePeace(war, selectedProvinces);
        }
        listSelectionView.setSourceItems(null);
        this.war = null;
        this.selectedProvinces.clear();
    }

    public void setDataFromWar(War war, Country c) {
        if (this.war != war) {
            this.war = war;
            DoubleProperty totalWarScore = war.warStateProperty(c);
            totalWarScoreLabel.textProperty().bind(totalWarScore.asString());
            dealButton.disableProperty().bind(Bindings.lessThan(totalWarScore, warScoreRequired));
            setTitle(war.toString());
            listSelectionView.setSourceItems(war.getOccupiedProvinces(gameStage.getGame().getPlayer()));
            this.selectedProvinces.clear();
        }
    }

    //occupation -> either by rebels or in war...
    public void makeProvinceListSelectionView() {
        listSelectionView = new ListSelectionView<>();
        listSelectionView.setTargetItems(selectedProvinces);
        listSelectionView.setPrefWidth(700);

        listSelectionView.setCellFactory(param -> new ListCell<>() {
            protected void updateItem(AdmDiv item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.calcWarCost());
                }
            }
        });

        selectedProvinces.addListener((ListChangeListener<AdmDiv>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (AdmDiv a : change.getAddedSubList()) {
                        double val = warScoreRequired.get() + a.calcWarCost();
                        warScoreRequired.set(val);
                    }
                } else if (change.wasRemoved()) {
                    for (AdmDiv a : change.getRemoved()) {
                        double val = warScoreRequired.get() - a.calcWarCost();
                        warScoreRequired.set(val);
                    }
                }
            }
        });
        listSelectionView.setSourceHeader(new Label("Occupied provinces"));
        listSelectionView.setTargetHeader(new Label("Selected Provinces"));
    }

    public void annexProvinces() {
        //popup...
        for (AdmDiv a : selectedProvinces) {
            System.out.println(a);
        }
    }

}