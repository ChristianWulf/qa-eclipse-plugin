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

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;

/**
 *
 * @author Christian Wulf
 *
 */
class CustomPMDConfiguration extends PMDConfiguration {

	private static final Language PMD_JAVA = LanguageRegistry.getLanguage(JavaLanguageModule.NAME);

	public CustomPMDConfiguration(final String compilerCompliance) {
		// default language version
		final LanguageVersion defaultLanguageVersion = CustomPMDConfiguration.PMD_JAVA.getVersion(compilerCompliance);
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

	// private LanguageVersion getDefaultLanguageVersion(File file) {
	// LanguageVersionDiscoverer languageDiscoverer = new
	// LanguageVersionDiscoverer();
	// LanguageVersion languageVersion =
	// languageDiscoverer.getDefaultLanguageVersionForFile(file.getName());
	//
	// if (languageVersion == null) {
	// return null;
	// }
	//
	// // in case it is java, select the correct java version
	// if (languageVersion.getLanguage() == PMD_JAVA) {
	// // languageVersion = this.javaVersionFor(file.getProject());
	// }
	//
	// return languageVersion;
	// }

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
