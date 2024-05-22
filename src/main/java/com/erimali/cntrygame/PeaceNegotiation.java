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
import javafx.util.Callback;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;


public class PeaceNegotiation {
    //redundant.
    private War war;

    public PeaceNegotiation(War war) {
        this.war = war;
    }

    public static Stage makePeaceNegotiationStage(ObservableList<AdmDiv> occupied) {
        DoubleProperty totalWarScore = new SimpleDoubleProperty(0); // as arg DoubleProperty totalWarScore,
        DoubleProperty warScoreRequired = new SimpleDoubleProperty(0);

        Label totalWarScoreLabel = new Label();
        totalWarScoreLabel.textProperty().bind(totalWarScore.asString());
        Label warScoreRequiredLabel = new Label();
        warScoreRequiredLabel.textProperty().bind(warScoreRequired.asString());
        Region reg = new Region();
        HBox hBox = new HBox(4, new Label("Total score: "), totalWarScoreLabel, reg, new Label("Required score: "), warScoreRequiredLabel);
        HBox.setHgrow(reg, Priority.ALWAYS);
        Tab provinceLSV = new Tab("Annex", makeProvinceListSelectionView(totalWarScore, warScoreRequired, occupied));
        Tab warObjectives = new Tab("War Objectives");
        TabPane tabPane = new TabPane(provinceLSV, warObjectives);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Button dealButton = new Button("Deal");

        VBox vBox = new VBox(hBox, tabPane, dealButton);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        Stage stage = new Stage();
        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        return stage;
    }

    //occupation -> either by rebels or in war...
    public static ListSelectionView<AdmDiv> makeProvinceListSelectionView(DoubleProperty totalWarScore, DoubleProperty warScoreRequired, ObservableList<AdmDiv> occupied) {
        ObservableList<AdmDiv> selectedItems = FXCollections.observableArrayList();
        ListSelectionView<AdmDiv> listSelectionView = new ListSelectionView<>();
        listSelectionView.setSourceItems(occupied);
        listSelectionView.setTargetItems(selectedItems);
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

        listSelectionView.getActions().add(new ListSelectionView.ListSelectionAction<AdmDiv>(GameStage.getGlyph(FontAwesome.Glyph.BANK)) {
            @Override
            public void initialize(ListView<AdmDiv> sourceListView, ListView<AdmDiv> targetListView) {
                disabledProperty().bind(Bindings.lessThan(totalWarScore, warScoreRequired));
                setEventHandler(ae -> annexProvinces(selectedItems));
            }
        });

        selectedItems.addListener((ListChangeListener<AdmDiv>) change -> {
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
        return listSelectionView;
    }

    public static void annexProvinces(ObservableList<AdmDiv> occupied) {
        //popup...
        for (AdmDiv a : occupied) {
            System.out.println(a);
        }
    }

}