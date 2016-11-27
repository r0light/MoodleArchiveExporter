package moodleexporter.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import moodleexporter.filesmodel.MoodleFile;
import moodleexporter.filesmodel.MoodleFiles;
import moodleexporter.view.PickerView;

public class ArchiveCrawler {

	private final String DIRECTORY_NAME = "Dateien";
	private JAXBContext context;
	private PickerView view;
	private SimpleStringProperty archivePath = new SimpleStringProperty();

	public ArchiveCrawler() {
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
		try {
			this.view.blockUI(true);
			exportFiles();
			Alert alert = new Alert(AlertType.INFORMATION, "The export finished successfully");
			alert.setHeaderText("Success");
			alert.showAndWait();
		} catch (MoodleExporterException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage());
			alert.setHeaderText("Error");
			alert.showAndWait();
		} finally {
			this.view.blockUI(false);
		}
	}

	public void exportFiles() throws MoodleExporterException {
		Path archive = checkArchivePath(getArchivePath());
		Path outputDir = createDirectoryForOutput(archive.getParent());
		try (FileSystem archiveFileSystem = FileSystems.newFileSystem(archive, null)) {
			// look for file "files.xml" which is a mapping file for all files
			Path filesData = archiveFileSystem.getPath("files.xml");

			List<MoodleFile> filesToExport = readFileEntries(filesData);
			for (MoodleFile file : filesToExport) {
				// ignore files with filename .
				if (!".".equals(file.filename)) {
					Path foundFile = locateFile(archiveFileSystem.getPath("files/"), file.contenthash);
					String outputFilename = file.filename;
					int counter = 1;
					// try to copy file but only if it does not override an
					// existing file
					while (Files.exists(outputDir.resolve(outputFilename))) {
						int pointPosition = file.filename.lastIndexOf(".");
						outputFilename = new StringBuffer(file.filename).insert(pointPosition, "_" + ++counter)
								.toString();
					}
					Files.copy(foundFile, outputDir.resolve(outputFilename));
				}
			}
		} catch (IOException e) {
			throw new MoodleExporterException("Error while accessing the archive file");
		}
	}

	private String getArchivePath() {
		return archivePath.get();
	}

	private Path checkArchivePath(String archivePath) throws MoodleExporterException {
		if (archivePath.endsWith(".mbz") || archivePath.endsWith(".zip")) {
			Path archive = Paths.get(archivePath);
			if (Files.exists(archive)) {
				return archive;
			}
			throw new MoodleExporterException("The specified archive does not exist.");
		}
		throw new MoodleExporterException("The specified file is not a valid archive file.");
	}

	private Path createDirectoryForOutput(Path parent) {
		Path outputDir = parent.resolve(DIRECTORY_NAME);
		try {
			Files.createDirectories(outputDir);
		} catch (IOException e) {
			System.out.println("Directory " + outputDir.toString() + " already exists ?");
			return outputDir;
		}
		return outputDir;
	}

	private List<MoodleFile> readFileEntries(Path filesData) throws MoodleExporterException {
		if (Files.exists(filesData) && Files.isRegularFile(filesData)) {
			try (BufferedReader filesDataReader = Files.newBufferedReader(filesData)) {
				Unmarshaller um = context.createUnmarshaller();
				MoodleFiles filesWrapper = (MoodleFiles) um.unmarshal(filesDataReader);
				return filesWrapper.getFiles();
			} catch (IOException e) {
				throw new MoodleExporterException("Error while reading the archive ", e);
			} catch (JAXBException e) {
				throw new MoodleExporterException("Error while unmarshalling the xml file ", e);
			}
		}
		throw new MoodleExporterException("The archive does not have a 'files.xml'-file.");

	}

	private Path locateFile(Path filesRoot, String hashedName) throws MoodleExporterException {
		FileLocator fileLocator = new FileLocator(hashedName);
		try {
			Files.walkFileTree(filesRoot, fileLocator);
			return fileLocator.getResult();
		} catch (IOException e) {
			throw new MoodleExporterException("Error while searching the file " + hashedName, e);
		}

	}

}
