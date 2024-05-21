package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilUnitData;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;

public class Technology {
    public static class GeneralTech {

    }

    //
    public static class MilTech {

    }

    //make class for visibility changes (?)
    public static SplitPane makeMilResearchUnitSplitPane(List<MilUnitData>[] dataTypes) {
        int n = dataTypes.length; //MilUnitData.getMaxTypes();
        VBox[] vBoxes = new VBox[n];
        for (int i = 0; i < n; i++) {
            vBoxes[i] = makeMilResearchUnitVBox(i, dataTypes[i]);
        }
        SplitPane splitPane = new SplitPane(vBoxes);
        return splitPane;
    }

    public static VBox makeMilResearchUnitVBox(int type, List<MilUnitData> list) {
        Label labelType = new Label(MilUnitData.getUnitTypeName(type));
        VBox vBox = new VBox(16, labelType);
        int[] minTechs = calcMinTechLevels(list);
        for (int i = 0; i < minTechs.length; i++) {
            vBox.getChildren().addAll(new Label("Level " + i), new HBox(8));
        }
        for (MilUnitData ud : list) {
            Node node = vBox.getChildren().get(2 * ud.getMinMilTech() + 2);
            if (node instanceof HBox hBox) {
                hBox.getChildren().add(new Label(ud.getName()));
            }
        }
        ToggleButton research = new ToggleButton("Research");
        vBox.getChildren().add(research);
        return vBox;
    }

    //In MILITARY!!!!
    public static int[] calcMinTechLevels(List<MilUnitData> list) {
        int maxLvl = 0;
        for (MilUnitData ud : list) {
            maxLvl = Math.max(maxLvl, ud.getMinMilTech());
        }
        if (maxLvl == 0)
            return new int[]{list.size()};
        int[] arr = new int[maxLvl + 1];
        Arrays.fill(arr, 0);
        for (MilUnitData ud : list) {
            int minMilTech = ud.getMinMilTech();
            if (minMilTech >= 0 && minMilTech <= maxLvl) {
                arr[minMilTech]++;
            }
        }
        return arr;
    }
}
