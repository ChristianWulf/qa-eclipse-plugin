package pmd.eclipse.plugin.views;

import pmd.eclipse.plugin.markers.PmdViolationMarker;

interface PmdViewFilter {

	public boolean canPass(PmdViolationMarker marker);

}
