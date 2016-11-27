package moodleexporter.filesmodel;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "files")
public class MoodleFiles {

	private List<MoodleFile> files = new LinkedList<MoodleFile>();

	@XmlElement(name = "file")
	public List<MoodleFile> getFiles() {
		return files;
	}

}
