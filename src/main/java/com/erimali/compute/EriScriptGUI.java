package com.erimali.compute;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import com.erimali.cntrygame.GOptions;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class EriScriptGUI extends Stage {
	public static EriScriptGUI mainEriGUI;
	private TextArea inputTextArea;
	private TextArea outputTextArea;
	private EriScript2 e;

	public EriScriptGUI() {
		EriScriptGUI.mainEriGUI = this;
		setTitle("Eri Script GUI");
		setOnCloseRequest(e -> close());

		BorderPane layout = createLayout();
		setWidth(800);
		setHeight(500);

		Scene scene = new Scene(layout);
		setScene(scene);
		scene.getStylesheets().add(getClass().getResource("eriScript.css").toExternalForm());

		this.setFullScreen(GOptions.isFullScreen());

	}

	private BorderPane createLayout() {
		BorderPane borderPane = new BorderPane();

		Button runButton = new Button("Compile & Run");
		runButton.setOnAction(e -> runScript());
		Button runLastCompButton = new Button("Run already compiled");
		runLastCompButton.setOnAction(e -> runLastCompiledScript());
		inputTextArea = new TextArea();
		inputTextArea.setPromptText("Enter your script here...");

		outputTextArea = new TextArea();
		outputTextArea.setEditable(false);

		MenuBar menuBar = new MenuBar();
		Menu fileMenu = new Menu("File");
		MenuItem openMenuItem = new MenuItem("Open");
		MenuItem saveMenuItem = new MenuItem("Save");

		openMenuItem.setOnAction(event -> openFileToString());
		saveMenuItem.setOnAction(event -> saveFile());

		Menu helpMenu = new Menu("Help");
		MenuItem keywordsItem = new MenuItem("Key words");
		keywordsItem.setOnAction(event -> showHelpKeywords());

		fileMenu.getItems().addAll(openMenuItem, saveMenuItem);
		helpMenu.getItems().addAll(keywordsItem);
		menuBar.getMenus().addAll(fileMenu,helpMenu);

		// Toolbar...

		borderPane.setTop(menuBar);

		BorderPane.setMargin(inputTextArea, new Insets(10));
		BorderPane.setMargin(outputTextArea, new Insets(10));
		borderPane.setCenter(new VBox(inputTextArea, outputTextArea));
		ToolBar toolBar = new ToolBar(runButton,runLastCompButton);

		borderPane.setBottom(toolBar);

		return borderPane;
	}

	private void showHelpKeywords() {
		Popup popupHelpKeywords = new Popup();
		Button closeButton = new Button("Close");
		closeButton.setOnAction(event -> popupHelpKeywords.hide());
		VBox vBox = new VBox(new Text("EriScript keywords"),new TextArea("print:[print new line]"),closeButton);

		popupHelpKeywords.getContent().add(vBox);
		popupHelpKeywords.show(this);
	}

	private void saveFile() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Save file");
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EriScript files","*.erisc"));
		File saveFile = fc.showSaveDialog(this);
		if(saveFile != null){
			try(FileWriter wr = new FileWriter(saveFile)){
				wr.write(inputTextArea.getText());
			} catch (IOException ioException) {
				//
                throw new RuntimeException(ioException);
            }
        }
	}

	private void openFileToString() {
		FileChooser fc = new FileChooser();
		fc.setTitle("Open EriScript file");
		File selFile = fc.showOpenDialog(this);
		if(selFile != null){
			try{
				String content = Files.readString(selFile.toPath());
				inputTextArea.setText(content);
			} catch(IOException ioException){
				inputTextArea.setText("print:Error while loading file");
			}
		}
	}

	public static double showDoubleInputDialog(Stage parentStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(parentStage);
        dialog.setTitle("Double Input Dialog");
        dialog.setHeaderText("Enter a double value:");
        dialog.setContentText("Value:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                double doubleValue = Double.parseDouble(result.get());
                return doubleValue;
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(parentStage);
                alert.setTitle("Invalid Input");
                alert.setHeaderText("Invalid input format");
                alert.setContentText("Please enter a valid double value.");
                alert.showAndWait();
            }
        }

        return 0.0;//Double.NaN; //in order to figure out it was wrong input?
    }

	public static double[] showDoubleArrInputDialog(Stage parentStage) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.initOwner(parentStage);
        dialog.setTitle("Double Array Input Dialog");
        dialog.setHeaderText("Enter double values separated by comma ',':");
        dialog.setContentText("Values:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
            	String strVals[] = result.get().trim().split("\\s*,\\s*"); 
            	double values[] = new double[strVals.length];
            	for(int i = 0; i < values.length; i++) {
                    double doubleValue = Double.parseDouble(strVals[i]);
                    values[i] = doubleValue;
            	}
                return values;
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initOwner(parentStage);
                alert.setTitle("Invalid Input");
                alert.setHeaderText("Invalid input format");
                alert.setContentText("Please enter a valid double array.");
                alert.showAndWait();
            }
        }
        return null;
    }
	public static void showPopupStage(Stage stage) {

        stage.initModality(Modality.APPLICATION_MODAL);

        stage.show(); //showAndWait()
    }
	
	private void runScript() {
		e = new EriScript2(inputTextArea.getText());
		e.execute();
		outputTextArea.setText(e.toPrint());
	}
	private void runLastCompiledScript(){
		if(e != null) {
			e.execute();
			outputTextArea.setText(e.toPrint());
		}
	}
}
