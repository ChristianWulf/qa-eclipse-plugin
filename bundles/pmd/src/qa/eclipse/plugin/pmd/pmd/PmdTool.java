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
package qa.eclipse.plugin.pmd.pmd;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;

/**
 *
 * @author Christian Wulf
 *
 */
public class PmdTool {

	/**
	 * All passed files must belong to the same project.
	 *
	 * @param eclipseFiles
	 */
	public void startAsyncAnalysis(final List<IFile> eclipseFiles) {
		if (eclipseFiles.isEmpty()) {
			return;
		}

		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (final IFile eclipseFile : eclipseFiles) {
			final ISchedulingRule fileRule = ruleFactory.markerRule(eclipseFile);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		final Job job = new PmdWorkspaceJob("Analysis by PMD", eclipseFiles);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}

}
