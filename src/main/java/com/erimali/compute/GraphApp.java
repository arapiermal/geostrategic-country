package com.erimali.compute;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

public class GraphApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public static Stage generateGraph(double[]... data) {
        int dimensions = data.length;

        // Create X and Y axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();

        if (dimensions == 1) {
            // Create a line chart for 1D data
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle("1D Line Chart");

            // Create a series for 1D data
            XYChart.Series<Number, Number> series1D = new XYChart.Series<>();
            series1D.setName("Data Series");

            // Add data to the series for 1D chart
            for (int i = 0; i < data[0].length; i++) {
                series1D.getData().add(new XYChart.Data<>(i + 1, data[0][i]));
            }

            // Add the series to the 1D chart
            lineChart.getData().add(series1D);
            return createStage(lineChart);
        } else if (dimensions == 2) {
            // Create a scatter chart for 2D data
            LineChart<Number, Number> scatterChart = new LineChart<>(xAxis, yAxis);
            scatterChart.setTitle("2D Scatter Chart");

            // Create a series for 2D data
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Data Series");

            // Add data to the series for 2D chart
            for (int i = 0; i < data[0].length; i++) {
                series.getData().add(new XYChart.Data<>(data[0][i], data[1][i]));
            }

            // Add the series to the 2D chart
            scatterChart.getData().add(series);
            return createStage(scatterChart);
        } else {
            throw new IllegalArgumentException("Unsupported number of dimensions. Only 1D and 2D data supported.");
        }
    }

    private static Stage createStage(LineChart<Number, Number> chart) {
        Stage stage = new Stage();
        Scene scene = new Scene(chart, 800, 600);
        stage.setScene(scene);
        return stage;
    }

    @Override
    public void start(Stage primaryStage) {
        // Example usage:
        double[] data1D = {1.0, 2.0, 3.0, 4.0, 5.0};
        Stage chartStage1D = generateGraph(data1D);
        chartStage1D.setTitle("1D Chart");
        chartStage1D.show();

        double[] dataX = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] dataY = {2.0, 4.0, 1.0, 5.0, 3.0};
        Stage chartStage2D = generateGraph(dataX, dataY);
        chartStage2D.setTitle("2D Chart");
        chartStage2D.show();
    }
}
