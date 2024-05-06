package com.erimali.cntrygame;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;


public class PeaceNegotiation {
    private static Glyph getGlyph(FontAwesome.Glyph angleDoubleDown) {
        return new FontAwesome().create(angleDoubleDown);
    }

    public static TabPane makeTabPaneNegotiation(ObservableList<AdmDiv> occupied) {
        DoubleProperty totalWarScore = new SimpleDoubleProperty(0.5); // as arg DoubleProperty totalWarScore,
        Tab provinceLSV = new Tab("Annex", makeProvinceListSelectionView(totalWarScore, occupied));
        Tab warObjectives = new Tab("War Objectives");
        TabPane tabPane = new TabPane(provinceLSV, warObjectives);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }

    //occupation -> either by rebels or in war...
    public static ListSelectionView<AdmDiv> makeProvinceListSelectionView(DoubleProperty totalWarScore, ObservableList<AdmDiv> occupied) {
        ObservableList<AdmDiv> selectedItems = FXCollections.observableArrayList();
        ListSelectionView<AdmDiv> listSelectionView = new ListSelectionView<>();
        listSelectionView.setSourceItems(occupied);
        listSelectionView.setTargetItems(selectedItems);
        listSelectionView.setPrefWidth(700);

        Label totalWarScoreLabel = new Label();
        totalWarScoreLabel.textProperty().bind(totalWarScore.asString());

        DoubleProperty warScoreRequired = new SimpleDoubleProperty(0);

        listSelectionView.getActions().add(new ListSelectionView.ListSelectionAction<AdmDiv>(getGlyph(FontAwesome.Glyph.BANK)) {
            @Override
            public void initialize(ListView<AdmDiv> sourceListView, ListView<AdmDiv> targetListView) {
                disabledProperty().bind(Bindings.lessThan(totalWarScore, warScoreRequired));
                setEventHandler(ae -> annexProvinces(selectedItems));
            }
        });
        Label warScoreRequiredLabel = new Label();
        warScoreRequiredLabel.textProperty().bind(warScoreRequired.asString());

        HBox hBoxTarget = new HBox(12, new Label("Selected Provinces"), new Label("Required score:"), warScoreRequiredLabel);
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
        HBox hBoxSource = new HBox(12, new Label("Occupied provinces"), new Label("Total score:"), totalWarScoreLabel);
        listSelectionView.setSourceHeader(hBoxSource);
        listSelectionView.setTargetHeader(hBoxTarget);
        return listSelectionView;
    }

    public static void annexProvinces(ObservableList<AdmDiv> occupied) {
        //popup...
        for (AdmDiv a : occupied) {
            System.out.println(a);
        }
    }
}