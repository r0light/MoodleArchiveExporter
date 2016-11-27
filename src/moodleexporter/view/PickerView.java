package moodleexporter.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import moodleexporter.logic.ArchiveCrawler;

public class PickerView extends Stage {

	private final ArchiveCrawler crawler;
	private BorderPane rootPane;
	private TextField archiveInput;
	private Button chooseArchive;
	private Button start;

	public PickerView(ArchiveCrawler crawler) {
		super();
		this.crawler = crawler;
		this.setTitle("Moodle Archive File Exporter");
		this.setWidth(600);
		this.setHeight(100);
		this.rootPane = new BorderPane();
		this.rootPane.setCenter(createActionPane());
		Scene scene = new Scene(this.rootPane);
		this.setScene(scene);
	}

	public void blockUI(boolean block) {
		chooseArchive.setDisable(block);
		start.setDisable(block);
	}

	public TextField getInputField() {
		return archiveInput;
	}

	private Pane createActionPane() {
		GridPane actionPane = new GridPane();
		actionPane.add(createInputField(), 0, 0);
		actionPane.add(createPickerButton(), 1, 0);
		actionPane.add(createStartButton(), 2, 0);

		actionPane.setVgap(10);
		actionPane.setHgap(10);
		actionPane.setPadding(new Insets(5));

		return actionPane;
	}

	private TextField createInputField() {
		archiveInput = new TextField();
		GridPane.setHgrow(archiveInput, Priority.ALWAYS);
		return archiveInput;
	}

	private Button createPickerButton() {
		chooseArchive = new Button("Select Archive");
		chooseArchive.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				chooseArchive();
			}
		});
		return chooseArchive;
	}

	private void chooseArchive() {
		FileChooser fileChooser = new FileChooser();
		crawler.setArchivePath(fileChooser.showOpenDialog(this));
	}

	private Button createStartButton() {
		start = new Button("Start Export");
		start.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				crawler.startExport();
			}
		});
		return start;
	}
}
