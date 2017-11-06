package pmdeclipseplugin.settings;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;

public class RuleSetCache {

	// <IProject, RuleSet>
	private final Map<IProject, RuleSet> ruleSets = new HashMap<>();

	public RuleSet getCachedRuleSet(IProject eclipseProject) {
		return ruleSets.get(eclipseProject);
	}

	public void load(IProject eclipseProject, String ruleSetFilePath, ClassLoader classLoader)
			throws RuleSetNotFoundException {
		final RuleSet ruleSet;
		final ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();

		final RuleSetFactory factory = new RuleSetFactory(classLoader, RulePriority.LOW, false, true);
		Thread.currentThread().setContextClassLoader(classLoader);
		try {
			// Explanation for overwriting the context class loader:
			// The call factory.createRuleSet(..) internally calls ServiceLoader.load(..)
			// which uses the context class loader to find an implementation for the
			// interface Language.
			// However, the context class loader in an equinox environment does not find the
			// correct implementation.
			// Hence, we overwrite the context class loader with an equinox class loader.
			// Finally, we rollback the context class loader to its original one.
			ruleSet = factory.createRuleSet(ruleSetFilePath);
		} finally {
			Thread.currentThread().setContextClassLoader(savedContextClassLoader);
		}

		ruleSets.put(eclipseProject, ruleSet);
	}

}
