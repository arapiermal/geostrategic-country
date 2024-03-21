package com.erimali.minigames;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class GraphApp extends Application {
    @Override
    public void start(Stage stage) {
        // Create a single array of data
        double[] data1D = {1.0, 2.0, 3.0, 4.0, 5.0};

        // Create two arrays of data for 2D graph
        double[] dataX = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] dataY = {2.0, 4.0, 1.0, 5.0, 3.0};

        // Create X and Y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        // Create a scatter chart for 2D data
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        scatterChart.setTitle("2D Scatter Chart");

        // Create a series for 2D data
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Data Series");

        // Add data to the series
        for (int i = 0; i < dataX.length; i++) {
            series.getData().add(new XYChart.Data<>(dataX[i], dataY[i]));
        }

        // Create a line chart for 1D data
        ScatterChart<Number, Number> lineChart = new ScatterChart<>(xAxis, yAxis);
        lineChart.setTitle("1D Line Chart");

        // Create a series for 1D data
        XYChart.Series<Number, Number> series1D = new XYChart.Series<>();
        series1D.setName("Data Series");

        // Add data to the series for 1D chart
        for (int i = 0; i < data1D.length; i++) {
            series1D.getData().add(new XYChart.Data<>(i + 1, data1D[i]));
        }

        // Add the series to the charts
        scatterChart.getData().add(series);
        lineChart.getData().add(series1D);

        // Create a scene with both charts
        Scene scene = new Scene(scatterChart, 800, 600);

        stage.setTitle("JavaFX Graphs");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
