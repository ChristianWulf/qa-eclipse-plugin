/***************************************************************************
 * Copyright (C) 2019
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
package qa.eclipse.plugin.pmd.preference;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.util.ResourceLoader;

import qa.eclipse.plugin.pmd.PmdUIPlugin;
import qa.eclipse.plugin.pmd.markers.PmdMarkersUtils;

/**
 *
 * @author Christian Wulf
 *
 */
public class RuleSetFileLoader {

	// TODO load only once at the start, i.e., make this field static
	private final RuleSets defaultRuleSets;

	/**
	 * Load a rule set from file.
	 */
	public RuleSetFileLoader() {
		final ResourceLoader resourceLoader = new ResourceLoader(this.getClass().getClassLoader());
		final RuleSetFactory factory = new RuleSetFactory(resourceLoader, RulePriority.LOW, false, true);

		final Iterator<RuleSet> registeredRuleSets;

		final ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		try {
			registeredRuleSets = factory.getRegisteredRuleSets();
		} catch (RuleSetNotFoundException | RuntimeException e) { // NOPMD, NOCS RuntimeException: if rule class was not found
			throw new IllegalStateException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(savedContextClassLoader);
		}

		this.defaultRuleSets = new RuleSets();
		while (registeredRuleSets.hasNext()) {
			final RuleSet ruleSet = registeredRuleSets.next();
			this.defaultRuleSets.addRuleSet(ruleSet);
		}
	}

	/**
	 * @param ruleSetFilePath
	 *            path to the ruleset
	 * @param project
	 *            associated project
	 * @param classLoaderWithCustomRules
	 *            class loader with custom rules
	 * @return the rule set declared in the given <code>ruleSetFilePath</code>, or
	 *         the built-in default rule set.
	 */
	public RuleSets load(final String ruleSetFilePath, final IProject project,
			final ClassLoader classLoaderWithCustomRules) {
		final ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();
		final ResourceLoader resourceLoader = new ResourceLoader(this.getClass().getClassLoader());

		final RuleSetFactory factory = new RuleSetFactory(resourceLoader, RulePriority.LOW, false, true);
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
			final RuleSet ruleSet = factory.createRuleSet(ruleSetFilePath);
			return new RuleSets(ruleSet);
		} catch (RuleSetNotFoundException | RuntimeException e) { // NOPMD, NOCS RuntimeException: if rule class was not found
			final String message;
			if (!new File(ruleSetFilePath).exists()) {
				// RuleSetNotFoundException at this place means: file not found.
				// Since PMD does not work without any ruleset file,
				// we use as default all of the rule sets which PMD provides.
				final String messageFormat = "Ruleset file not found on file path '%s'. Defaulting to all of the rule sets which PMD provides.";
				message = String.format(messageFormat, ruleSetFilePath);
			} else {
				final String messageFormat = "Ruleset file references rules "
						+ "which are not in the (custom rules) classpath: %s. Defaulting to all of the rule sets which PMD provides.";
				message = String.format(messageFormat, e.getLocalizedMessage());
			}
			try {
				PmdMarkersUtils.appendViolationMarker(project, message);
			} catch (final CoreException e1) {
				PmdUIPlugin.getDefault().logThrowable("Cannot set marker error, while reporting: " + message, e);
			}
			return this.defaultRuleSets;
		} finally {
			Thread.currentThread().setContextClassLoader(savedContextClassLoader);
		}
	}
}
