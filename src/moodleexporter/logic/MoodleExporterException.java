package moodleexporter.logic;

public class MoodleExporterException extends Exception {

	private static final long serialVersionUID = 2305994364133678102L;

	public MoodleExporterException() {
		super();
	}

	public MoodleExporterException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public MoodleExporterException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public MoodleExporterException(String arg0) {
		super(arg0);
	}

	public MoodleExporterException(Throwable arg0) {
		super(arg0);
	}

}
