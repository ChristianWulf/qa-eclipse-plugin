/***************************************************************************
 * Copyright (C) 2019 Christian Wulf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/
package qa.eclipse.plugin.bundles.checkstyle.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 *
 * @author Christian Wulf
 *
 */
class ResourceCollector implements IResourceVisitor {

    private static final Collection<String> ACCEPTED_FILE_EXTENSIONS = Arrays.asList(new String[] { "java" });

    private final Map<IProject, List<IFile>> projectResources = new ConcurrentHashMap<>();

    /**
     * Default constructor.
     */
    public ResourceCollector() {
        // nothing to be done here
    }

    @Override
    public boolean visit(final IResource resource) throws CoreException {
        final int resourceType = resource.getType();
        switch (resourceType) {
        case IResource.FILE: {
            final IProject project = resource.getProject();
            List<IFile> files = this.projectResources.get(project);
            if (files == null) {
                files = new ArrayList<>();
                this.projectResources.put(project, files);
            }

            final IFile file = (IFile) resource;
            if (ResourceCollector.ACCEPTED_FILE_EXTENSIONS.contains(file.getFileExtension())) {
                files.add(file);
            }
            return false;
        }
        case IResource.FOLDER:
            return true;
        case IResource.PROJECT:
            return true;
        case IResource.ROOT:
            return true;
        default:
            return false;
        }
    }

    public Map<IProject, List<IFile>> getProjectResources() {
        return this.projectResources;
    }

}
