package pmd.eclipse.plugin.preference;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import pmd.eclipse.plugin.PmdUIPlugin;
import pmd.eclipse.plugin.markers.PmdMarkers;
import qa.eclipse.plugin.bundles.common.ClassLoaderUtil;

public class RuleSetFileLoader {

	// TODO load only once at the start, i.e., make this field static
	private final RuleSets defaultRuleSets;

	public RuleSetFileLoader() {
		ClassLoader classLoader = getClass().getClassLoader();

		final RuleSetFactory factory = new RuleSetFactory(classLoader, RulePriority.LOW, false, true);

		Iterator<RuleSet> registeredRuleSets = ClassLoaderUtil.executeWithContextClassLoader(classLoader, () -> {
			try {
				return factory.getRegisteredRuleSets();
			} catch (RuleSetNotFoundException | RuntimeException e) { // RuntimeException: if rule class was not found
				throw new IllegalStateException(e);
			}
		});

		// final ClassLoader savedContextClassLoader =
		// Thread.currentThread().getContextClassLoader();
		// Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		// try {
		// registeredRuleSets = factory.getRegisteredRuleSets();
		// } catch (RuleSetNotFoundException | RuntimeException e) { //
		// RuntimeException: if rule class was not found
		// throw new IllegalStateException(e);
		// } finally {
		// Thread.currentThread().setContextClassLoader(savedContextClassLoader);
		// }

		defaultRuleSets = new RuleSets();
		while (registeredRuleSets.hasNext()) {
			RuleSet ruleSet = registeredRuleSets.next();
			defaultRuleSets.addRuleSet(ruleSet);
		}
	}

	/**
	 * @param ruleSetFilePath
	 * @return the rule set declared in the given <code>ruleSetFilePath</code>, or
	 *         the built-in default rule set.
	 */
	public RuleSets load(String ruleSetFilePath, IProject project, ClassLoader classLoaderWithCustomRules) {
		final ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();

		final RuleSetFactory factory = new RuleSetFactory(classLoaderWithCustomRules, RulePriority.LOW, false, true);
		Thread.currentThread().setContextClassLoader(classLoaderWithCustomRules);
		try {
			// Explanation for overwriting the context class loader:
			// The call factory.createRuleSet(..) internally calls ServiceLoader.load(..)
			// which uses the context class loader to find an implementation for the
			// interface Language.
			// Look at: net.sourceforge.pmd.util.ResourceLoader.loadResourceAsStream(String)
			// However, the context class loader in an equinox environment does not find the
			// correct implementation.
			// Hence, we overwrite the context class loader with an equinox class loader.
			// Finally, we rollback the context class loader to its original one.
			RuleSet ruleSet = factory.createRuleSet(ruleSetFilePath);
			return new RuleSets(ruleSet);
		} catch (RuleSetNotFoundException | RuntimeException e) { // RuntimeException: if rule class was not found
			final String message;
			if (!new File(ruleSetFilePath).exists()) {
				// RuleSetNotFoundException at this place means: file not found.
				// Since PMD does not work without any ruleset file,
				// we use as default all of the rule sets which PMD provides.
				String messageFormat = "Ruleset file not found on file path '%s'. Defaulting to all of the rule sets which PMD provides.";
				message = String.format(messageFormat, ruleSetFilePath);
			} else {
				String messageFormat = "Ruleset file references rules which are not in the (custom rules) classpath: %s. Defaulting to all of the rule sets which PMD provides.";
				message = String.format(messageFormat, e.getLocalizedMessage());
			}
			try {
				PmdMarkers.appendViolationMarker(project, message);
			} catch (CoreException e1) {
				PmdUIPlugin.getDefault().logThrowable("Cannot set marker error, while reporting: " + message, e);
			}
			return defaultRuleSets;
		} finally {
			Thread.currentThread().setContextClassLoader(savedContextClassLoader);
		}
	}
}
