package com.erimali.minigames;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomNumberInputDialogExample extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button inputButton = new Button("Open Number Input Popup");
        inputButton.setOnAction(e -> {
            Double result = showNumberInputDialog();
            if (result != null) {
                displayNumberInputResult(result);
            }
        });

        StackPane root = new StackPane(inputButton);
        Scene scene = new Scene(root, 300, 250);

        primaryStage.setTitle("Custom Number Input Popup Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Double showNumberInputDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        Label label = new Label("Enter a number:");
        TextField inputField = new TextField();

        Button okButton = new Button("OK");
        okButton.setOnAction(e -> dialogStage.close());

        VBox vbox = new VBox(10, label, inputField, okButton);
        vbox.setStyle("-fx-padding: 10px;");
        vbox.setPrefSize(200, 150);

        dialogStage.setScene(new Scene(vbox));
        dialogStage.setTitle("Number Input");
        dialogStage.showAndWait();

        try {
            return Double.parseDouble(inputField.getText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void displayNumberInputResult(double number) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Number Input Result");
        alert.setHeaderText(null);
        alert.setContentText("You entered: " + number);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

	private void showInputPopup() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Input Popup");
		dialog.setHeaderText("Please enter amount:");
		dialog.setContentText("Amount:");

		// Show the popup and wait for the user's response
		dialog.showAndWait().ifPresent(name -> {
			// Handle the input data
			try {
				double amount = Double.parseDouble(name);
				displayInputResult(amount);
			} catch (NumberFormatException e) {
				//showError("Amount cannot be a string!");
			}

		});
		
	}

	private void displayInputResult(double amount) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Input Result");
		alert.setHeaderText(null);
		alert.setContentText("Amount is " + amount);
		alert.showAndWait();
	}
}
