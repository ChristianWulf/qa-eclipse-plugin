package pmd.eclipse.plugin.views;

import pmd.eclipse.plugin.markers.PmdViolationMarker;

public class PmdPriorityFilter implements PmdViewFilter {

	private final PmdViewFilter filter;
	private final int lowestPriority;

	public PmdPriorityFilter(PmdViewFilter filter, int lowestPriority) {
		this.filter = filter;
		this.lowestPriority = lowestPriority;
	}

	@Override
	public boolean canPass(PmdViolationMarker marker) {
		if (marker.getPriority() <= lowestPriority) {
			return filter.canPass(marker);
		} else {
			return false;
		}
	}

}
