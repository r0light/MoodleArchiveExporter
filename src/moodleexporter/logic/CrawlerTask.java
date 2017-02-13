package moodleexporter.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import javafx.concurrent.Task;
import moodleexporter.filesmodel.MoodleFile;
import moodleexporter.filesmodel.MoodleFiles;

public class CrawlerTask extends Task<Boolean> {

	private final String archivePath;
	private final String directoryName;
	private final JAXBContext context;

	public CrawlerTask(String archivePath, String directoryName, JAXBContext context) {
		super();
		this.archivePath = archivePath;
		this.directoryName = directoryName;
		this.context = context;
	}

	@Override
	protected Boolean call() throws MoodleExporterException {
		exportFiles();
		return true;
	}

	public void exportFiles() throws MoodleExporterException {
		Path archive = checkArchivePath(archivePath);
		Path outputDir = createDirectoryForOutput(archive.getParent());
		List<String> errors = new LinkedList<String>();
		try (FileSystem archiveFileSystem = FileSystems.newFileSystem(archive, null)) {

			// check if a file is a correct file, there are entries with only a
			// dot in the files.xml which represent other stuff
			Predicate<MoodleFile> isCorrectFile = file -> {
				if (!".".equals(file.filename)) {
					return true;
				}
				return false;
			};

			// processes a MoodleFile and returns a String which is empty, if
			// the processing was successful or otherwise contains the error
			// message
			Function<MoodleFile, Boolean> exportFile = file -> {
				try {
					// ignore files with filename .

					Path foundFile = locateFile(archiveFileSystem.getPath("files/"), file.contenthash);
					String outputFilename = file.filename;
					int counter = 1;
					// try to copy file but only if it does not override an
					// existing file
					while (Files.exists(outputDir.resolve(outputFilename))) {
						int pointPosition = file.filename.lastIndexOf(".");
						if (pointPosition == -1) {
							pointPosition = file.filename.length();
						}
						outputFilename = new StringBuffer(file.filename).insert(pointPosition, "_" + ++counter)
								.toString();
					}
					Files.copy(foundFile, outputDir.resolve(outputFilename));
					return true;
				} catch (MoodleExporterException | IOException e) {
					// save error message and return false
					errors.add(e.getMessage());
					return false;
				}
			};

			// look for file "files.xml" which is a mapping file for all files
			// and read all file entries in a list
			Path filesData = archiveFileSystem.getPath("files.xml");
			List<MoodleFile> filesToExport = readFileEntries(filesData);

			// start a stream of this list and export the files
			Optional<Boolean> success = filesToExport.stream().parallel().filter(isCorrectFile).map(exportFile)
					.reduce((b1, b2) -> {
						return b1 && b2;
					});

			// if any export failed the Boolean in the Optional is false and the
			// error(s) should be reported
			if (success.isPresent() && !success.get()) {
				throw new MoodleExporterException("Error(s) while exporting the files: " + errors.toString());
			}

		} catch (InvalidPathException e) {
			throw new MoodleExporterException(
					"Error while reading a file; the filename was bad." + System.lineSeparator() + e.getMessage(), e);
		} catch (IOException e) {
			throw new MoodleExporterException("Error while accessing the archive file.", e);
		}
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
		Path outputDir = parent.resolve(directoryName);
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
