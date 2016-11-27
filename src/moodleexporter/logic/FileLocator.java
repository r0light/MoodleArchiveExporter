package moodleexporter.logic;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class FileLocator extends SimpleFileVisitor<Path> {

	private final String hashedName;
	private Path foundFile = null;

	public FileLocator(String hashedName) {
		super();
		this.hashedName = hashedName;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (file.getFileName().toString().equals(hashedName)) {
			foundFile = file.toAbsolutePath();
			return FileVisitResult.TERMINATE;
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public Path getResult() {
		return foundFile;
	}
}
