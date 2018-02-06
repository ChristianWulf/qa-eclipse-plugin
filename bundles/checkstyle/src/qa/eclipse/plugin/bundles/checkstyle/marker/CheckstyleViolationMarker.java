package qa.eclipse.plugin.bundles.checkstyle.marker;

import org.eclipse.core.resources.IMarker;

public class CheckstyleViolationMarker {

	private final IMarker marker;

	public CheckstyleViolationMarker(IMarker marker) {
		super();
		this.marker = marker;
	}

	public IMarker getMarker() {
		return marker;
	}

	public int getSeverityLevelIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLineNumer() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getModuleName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getProjectName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDirectoryPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
