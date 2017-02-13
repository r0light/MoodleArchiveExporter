package moodleexporter.logic;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import moodleexporter.filesmodel.MoodleFiles;
import moodleexporter.view.PickerView;

public class CrawlerController {

	private final ExecutorService executorService = Executors.newFixedThreadPool(1);
	private final String DIRECTORY_NAME = "Dateien";
	private JAXBContext context;
	private PickerView view;
	private SimpleStringProperty archivePath = new SimpleStringProperty();

	public CrawlerController() {
		try {
			this.context = JAXBContext.newInstance(MoodleFiles.class);
			this.view = new PickerView(this);
			this.view.getInputField().textProperty().bindBidirectional(archivePath);
			this.view.show();
		} catch (JAXBException e) {
			System.err.println("Error: JAXB context could not be initialized");
		}
	}

	public void setArchivePath(File archive) {
		if (archive != null) {
			this.archivePath.set(archive.getPath());
		}
	}

	public void startExport() {
		view.blockUI(true);

		// create Task for the export to be executed in a seperate thread
		Task<Boolean> crawlerTask = new CrawlerTask(getArchivePath(), DIRECTORY_NAME, context);

		crawlerTask.stateProperty().addListener(new ChangeListener<Worker.State>() {
			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, Worker.State newState) {
				if (newState == Worker.State.SUCCEEDED) {
					Alert alert = new Alert(AlertType.INFORMATION, "The export finished successfully");
					alert.setHeaderText("Success");
					alert.showAndWait();
					view.blockUI(false);
				} else if (newState == Worker.State.FAILED) {
					Alert alert = new Alert(AlertType.ERROR, crawlerTask.exceptionProperty().get().getMessage());
					alert.setHeaderText("Error");
					alert.showAndWait();
					view.blockUI(false);
				}
			}
		});
		executorService.submit(crawlerTask);
	}

	private String getArchivePath() {
		return archivePath.get();
	}

}
