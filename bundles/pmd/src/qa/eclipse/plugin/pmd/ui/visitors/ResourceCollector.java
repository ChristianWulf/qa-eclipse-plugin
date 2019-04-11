package qa.eclipse.plugin.pmd.ui.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class ResourceCollector implements IResourceVisitor {

	private final Map<IProject, List<IFile>> projectResources = new HashMap<>();

	@Override
	public boolean visit(IResource resource) throws CoreException {
		int resourceType = resource.getType();
		switch (resourceType) {
		case IResource.FILE: {
			IProject project = resource.getProject();
			List<IFile> files = projectResources.get(project);
			if (files == null) {
				files = new ArrayList<>();
				projectResources.put(project, files);
			}

			IFile file = (IFile) resource;
			files.add(file);
			return false;
		}
		case IResource.FOLDER: {
			return true;
		}
		case IResource.PROJECT: {
			return true;
		}
		case IResource.ROOT: {
			return true;
		}
		default: {
			return false;
		}
		}
	}

	public Map<IProject, List<IFile>> getProjectResources() {
		return projectResources;
	}

}
