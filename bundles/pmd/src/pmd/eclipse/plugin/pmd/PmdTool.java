package pmd.eclipse.plugin.pmd;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;

public class PmdTool {

	/**
	 * All passed files must belong to the same project.
	 * 
	 * @param eclipseFiles
	 */
	public void startAsyncAnalysis(List<IFile> eclipseFiles) {
		if (eclipseFiles.isEmpty()) {
			return;
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResourceRuleFactory ruleFactory = workspace.getRuleFactory();

		ISchedulingRule jobRule = null;
		for (IFile eclipseFile : eclipseFiles) {
			ISchedulingRule fileRule = ruleFactory.markerRule(eclipseFile);
			jobRule = MultiRule.combine(jobRule, fileRule);
		}

		Job job = new PmdWorkspaceJob("Analysis by PMD", eclipseFiles);
		job.setRule(jobRule);
		job.setUser(true);
		job.schedule();
	}

}
