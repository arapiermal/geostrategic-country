package com.erimali.cntryrandom;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainRandom extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        RandomGenStage randomGenStage = new RandomGenStage();
        randomGenStage.setTitle("Random Map Generator");
        randomGenStage.show();

        primaryStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
