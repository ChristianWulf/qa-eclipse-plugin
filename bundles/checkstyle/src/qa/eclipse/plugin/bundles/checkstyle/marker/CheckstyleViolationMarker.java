package qa.eclipse.plugin.bundles.checkstyle.marker;

import org.eclipse.core.resources.IMarker;

public class CheckstyleViolationMarker {

	private final IMarker marker;

	public CheckstyleViolationMarker(IMarker marker) {
		super();
		this.marker = marker;
	}

	public int getPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

}
