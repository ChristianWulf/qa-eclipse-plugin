package pmdeclipseplugin.settings;

import net.sourceforge.pmd.RulePriority;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;

class RuleSetFileLoader {

	private final ClassLoader osgiClassLoaderWithCustomRules;

	public RuleSetFileLoader(ClassLoader osgiClassLoaderWithCustomRules) {
		this.osgiClassLoaderWithCustomRules = osgiClassLoaderWithCustomRules;
	}

	/**
	 * @param ruleSetFilePath
	 * @return the rule set declared in the given <code>ruleSetFilePath</code>
	 * @throws RuleSetNotFoundException
	 *             if
	 *             <ul>
	 *             <li>the given <code>ruleSetFilePath</code> cannot be found,
	 *             or</li>
	 *             <li>one of the declared rules cannot be found in the
	 *             classpath</li>
	 *             </ul>
	 */
	public RuleSet load(String ruleSetFilePath) throws RuleSetNotFoundException {
		final ClassLoader savedContextClassLoader = Thread.currentThread().getContextClassLoader();

		ClassLoader factoryClassLoader = osgiClassLoaderWithCustomRules;
		final RuleSetFactory factory = new RuleSetFactory(factoryClassLoader, RulePriority.LOW, false, true);
		try {
			// Explanation for overwriting the context class loader:
			// The call factory.createRuleSet(..) internally calls ServiceLoader.load(..)
			// which uses the context class loader to find an implementation for the
			// interface Language.
			// However, the context class loader in an equinox environment does not find the
			// correct implementation.
			// Hence, we overwrite the context class loader with an equinox class loader.
			// Finally, we rollback the context class loader to its original one.
			Thread.currentThread().setContextClassLoader(osgiClassLoaderWithCustomRules);
			return factory.createRuleSet(ruleSetFilePath);
		} finally {
			Thread.currentThread().setContextClassLoader(savedContextClassLoader);
		}
	}
}
