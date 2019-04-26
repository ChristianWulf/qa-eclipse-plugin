/***************************************************************************
 * Copyright (C) 2019 SE research group, Kiel University
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
package qa.eclipse.plugin.bundles.common;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Reiner Jung
 *
 */
public final class PropertyPageUtils {

	private PropertyPageUtils() {
		// make instantiation impossible
	}

	/**
	 * Compute a project relative path for a file which may or may not be in the same project.
	 *
	 * @param project
	 *            project to which the path must be relative to
	 * @param file
	 *            a full qualified location for a file
	 *
	 * @return returns the relative path
	 */
	public static IPath computeRelativePath(final IPath project, final IPath file) {
		IPath result = new Path("");
		int i = 0;
		for (final String segment : project.segments()) {
			final String fileSegment = file.segment(i);
			if (!segment.equals(fileSegment)) {
				break;
			} else {
				i++;
			}
		}
		for (int down = 0; down < (project.segmentCount() - i); down++) {
			result = result.append(".." + IPath.SEPARATOR);
		}
		for (int up = i; up < file.segmentCount(); up++) {
			result = result.append(file.segment(up));
		}
		return result;
	}
}
