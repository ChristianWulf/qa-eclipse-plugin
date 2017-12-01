package pmd.eclipse.plugin.views;

import pmd.eclipse.plugin.markers.PmdViolationMarker;

class PmdPassAllFilter implements PmdViewFilter {

	@Override
	public boolean canPass(PmdViolationMarker marker) {
		return true;
	}

}
