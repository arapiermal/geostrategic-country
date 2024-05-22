package com.erimali.cntrygame;

import com.erimali.cntrymilitary.MilUnitData;
import com.erimali.cntrymilitary.Military;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Text;

import java.util.Arrays;
import java.util.List;

public class Technology {
    public static class GeneralTech {

    }

    //
    public static class MilTech {

    }

    public static class MilResearchUnitsStage extends Stage {
        //bind...
        private final Label[] labelTypeCurrLvl;
        private final Label[] labelTypeProgress;
        private final ProgressBar[] progressBars;
        private final ToggleButton[] researchButtons;
        private final SplitPane splitPane;

        public MilResearchUnitsStage(List<MilUnitData>[] dataTypes) {
            int n = dataTypes.length; //MilUnitData.getMaxTypes();
            this.labelTypeCurrLvl = new Label[n];
            this.labelTypeProgress = new Label[n];
            this.progressBars = new ProgressBar[n];
            this.researchButtons = new ToggleButton[n];
            VBox[] vBoxes = new VBox[n];
            for (int i = 0; i < n; i++) {
                vBoxes[i] = makeMilResearchUnitsVBox(i, dataTypes[i]);
            }
            splitPane = new SplitPane(vBoxes);
            double[] dividerPositions = new double[n - 1]; // 8 VBox -> 7 dividers
            for (int i = 1; i <= n - 1; i++) {
                dividerPositions[i - 1] = i / (double) n;
            }
            splitPane.setDividerPositions(dividerPositions);
            Scene scene = new Scene(splitPane);
            setScene(scene);
        }

        public VBox makeMilResearchUnitsVBox(int type, List<MilUnitData> list) {
            //or level...
            Text typeText = new Text(MilUnitData.getUnitTypeNameUpper(type));
            labelTypeCurrLvl[type] = new Label("0");
            labelTypeProgress[type] = new Label("0/100");
            progressBars[type] = new ProgressBar(0.0);
            HBox firstRowHBox1 = new HBox(8, new Text("Level"), labelTypeCurrLvl[type]);
            HBox firstRowHBox2 = new HBox(8, new Text("Progress"), labelTypeProgress[type]);
            VBox firstRowVBox = new VBox(typeText, firstRowHBox1, firstRowHBox2, progressBars[type]);
            VBox vBox = new VBox(16, firstRowVBox);
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
            Region reg = new Region();
            vBox.getChildren().add(reg);
            VBox.setVgrow(reg, Priority.ALWAYS);
            researchButtons[type] = new ToggleButton("Research");
            vBox.getChildren().add(researchButtons[type]);
            vBox.setPadding(new Insets(4));
            return vBox;
        }

        public void updatePlayer(Military mil) {
            boolean[] researchingMilTech = mil.getResearchingMilTech();
            for (int i = 0; i < researchButtons.length; i++) {
                int finalI = i;
                researchButtons[i].setOnAction(e -> researchingMilTech[finalI] = !researchingMilTech[finalI]);
            }
        }

        //monthly update (?)
        public void updateScene(Military mil) {
            short[] milTechProgress = mil.getMilTechProgress();
            short[] milTechLevel = mil.getMilTechLevel();
            boolean[] researchingMilTech = mil.getResearchingMilTech();
            for (int i = 0; i < labelTypeCurrLvl.length; i++) {
                short lvl = milTechLevel[i];
                labelTypeCurrLvl[i].setText(Short.toString(lvl));
                short progress = milTechProgress[i];
                short lvlCap = mil.getMilTechLevelCap();
                labelTypeProgress[i].setText(progress + "/" + lvlCap);
                progressBars[i].setProgress((double) progress / lvlCap);
                researchButtons[i].setSelected(researchingMilTech[i]);
            }
        }


    }

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
