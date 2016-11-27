package moodleexporter.filesmodel;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class MoodleFile {

	@XmlAttribute
	public String id;

	@XmlElement
	public String contenthash;

	@XmlElement
	public String filename;

	// @XmlElement
	// public String contextid;
	//
	// @XmlElement
	// public String component;
	//
	// @XmlElement
	// public String filearea;
	//
	// @XmlElement
	// public String itemid;
	//
	// @XmlElement
	// public String filepath;

	// @XmlElement
	// public String userid;
	//
	// @XmlElement
	// public String filesize;
	//
	// @XmlElement
	// public String mimetype;
	//
	// @XmlElement
	// public String status;
	//
	// @XmlElement
	// public String timecreated;
	//
	// @XmlElement
	// public String timemodified;
	//
	// @XmlElement
	// public String source;
	//
	// @XmlElement
	// public String author;
	//
	// @XmlElement
	// public String license;
	//
	// @XmlElement
	// public String sortorder;
	//
	// @XmlElement
	// public String repositorytype;
	//
	// @XmlElement
	// public String repositoryid;
	//
	// @XmlElement
	// public String reference;

}
