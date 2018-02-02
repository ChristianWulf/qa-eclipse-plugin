package pmd.eclipse.plugin.pmd;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;

class CustomPMDConfiguration extends PMDConfiguration {

	private static final Language PMD_JAVA = LanguageRegistry.getLanguage(JavaLanguageModule.NAME);

	public CustomPMDConfiguration(String compilerCompliance) {
		// default language version
		LanguageVersion defaultLanguageVersion = PMD_JAVA.getVersion(compilerCompliance);
		if (defaultLanguageVersion != null) {
			this.setDefaultLanguageVersion(defaultLanguageVersion);
		}

		// IProject project = null;
		// Java class loader
		// ClassLoader javaClassLoader = getJavaClassLoader(project);
		// if (javaClassLoader != null) {
		// this.setClassLoader(javaClassLoader);
		// }
	}

//	private LanguageVersion getDefaultLanguageVersion(File file) {
//		LanguageVersionDiscoverer languageDiscoverer = new LanguageVersionDiscoverer();
//		LanguageVersion languageVersion = languageDiscoverer.getDefaultLanguageVersionForFile(file.getName());
//
//		if (languageVersion == null) {
//			return null;
//		}
//
//		// in case it is java, select the correct java version
//		if (languageVersion.getLanguage() == PMD_JAVA) {
//			// languageVersion = this.javaVersionFor(file.getProject());
//		}
//
//		return languageVersion;
//	}

	/**
	 * Return the Java language version for the resources found within the specified
	 * project or null if it isn't a Java project or a Java version we don't support
	 * yet.
	 * 
	 * @param project
	 * @return
	 */
	// private LanguageVersion javaVersionFor(IProject project) {
	// IJavaProject jProject = JavaProjectsByIProject.get(project);
	// if (jProject == null) {
	// jProject = JavaCore.create(project);
	// JavaProjectsByIProject.put(project, jProject);
	// }
	//
	// if (jProject.exists()) {
	// String compilerCompliance = jProject.getOption(JavaCore.COMPILER_COMPLIANCE,
	// true);
	// return PMD_JAVA.getVersion(compilerCompliance);
	// }
	// return null;
	// }
	//
	// private ClassLoader getJavaClassLoader(IProject project) {
	// IPreferences preferences = getDefault().loadPreferences();
	// try {
	// if (preferences.isProjectBuildPathEnabled() &&
	// project.hasNature(JavaCore.NATURE_ID)) {
	// return new JavaProjectClassLoader(config.getClass().getClassLoader(),
	// project);
	// }
	// } catch (CoreException e) {
	// throw new RuntimeException(e);
	// }
	// }
}
