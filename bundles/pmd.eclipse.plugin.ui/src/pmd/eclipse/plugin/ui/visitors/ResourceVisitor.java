package pmd.eclipse.plugin.ui.visitors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import pmdeclipseplugin.pmd.PmdTool;

public class ResourceVisitor implements IResourceVisitor {

	private final PmdTool pmdTool;

	public ResourceVisitor(PmdTool pmdTool) {
		this.pmdTool = pmdTool;
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		int resourceType = resource.getType();
		// System.out.println("resource: " + resource + ", " + resourceType);
		switch (resourceType) {
		case IResource.FILE: {
			IFile eclipseFile = (IFile) resource;
			pmdTool.startAsyncAnalysis(eclipseFile);
			return false;
		}
		case IResource.FOLDER: {
			return true;
		}
		default: {
			return false;
		}
		}
	}

}
