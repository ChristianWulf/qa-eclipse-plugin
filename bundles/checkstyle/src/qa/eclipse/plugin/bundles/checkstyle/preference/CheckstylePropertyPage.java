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
package qa.eclipse.plugin.bundles.checkstyle.preference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.osgi.service.prefs.BackingStoreException;

import qa.eclipse.plugin.bundles.common.PropertyPageUtils;

/**
 *
 * @author Christian Wulf
 *
 */
public class CheckstylePropertyPage extends PropertyPage {

	public static final String CONFIG_FILE_PATH_DEFAULT_TEXT = "Example: conf/quality-config/cs-conf.xml";
	public static final String CUSTOM_MODULES_DEFAULT_TEXT = "Example: config/checkstyle/custom-modules-0.jar, config/checkstyle/custom-modules-1.jar";

	private Text configFilePathText;
	private Text customModulesJarPathsText;
	private Button enabledButton;
	private Label exampleConfigurationFilePathLabel;
	private Label exampleCustomModulesLabel;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public CheckstylePropertyPage() {
		super();
	}

	private void addFirstSection(final Composite parent) {
		final Composite composite = this.createDefaultComposite(parent, 1);

		final IResource resource = this.getElement().getAdapter(IResource.class);
		final IProject project = resource.getProject();

		// ensure that the properties displayed are in sync with the corresponding prefs
		// file
		try {
			project.getFolder(".settings").refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (final CoreException e) { // NOPMD ignore empty block
		}

		final IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(project);

		this.createCheckstyleEnableCheckbox(composite, preferences);
		this.createHintLabel(parent, composite, "Hint: Disabling Checkstyle clears all violations.");

		this.addSeparator(composite);

		this.createConfigurationFilePathField(composite, project, preferences);

		this.exampleConfigurationFilePathLabel = this.createFullWidthHintLabel(parent, composite, CheckstylePropertyPage.CONFIG_FILE_PATH_DEFAULT_TEXT);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Zero or more jar file paths with custom modules (comma separated):");

		this.createDefaultComposite(composite, 2);

		this.customModulesJarPathsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		final GridData gd = new GridData();
		gd.widthHint = this.convertWidthInCharsToPixels(60);
		this.customModulesJarPathsText.setLayoutData(gd);
		this.customModulesJarPathsText
				.setText(preferences.get(CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS, ""));
		this.customModulesJarPathsText.addKeyListener(new CustomModulesKeyListener(this));

		this.exampleCustomModulesLabel = this.createFullWidthHintLabel(parent, composite, CheckstylePropertyPage.CUSTOM_MODULES_DEFAULT_TEXT);

		new Label(composite, SWT.NONE); // serves as newline

		final Label hideFlagsHintLabel = new Label(composite, SWT.NONE);
		hideFlagsHintLabel.setText("To hide the violation flags in the (Package/Project) Explorer, "
				+ System.lineSeparator() + "open Eclipse's global preferences and search for 'Label Decorations'.");
		// exampleLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		new Label(composite, SWT.NONE); // serves as newline

		this.addSeparator(composite);

		this.createHintLabel(parent, composite, "Hint: Relative paths are resolved relative to the project's path.");
	}

	/** create the filed and button for the configuration file. */
	private void createConfigurationFilePathField(final Composite composite, final IProject project, final IEclipsePreferences preferences) {
		// Label for path field
		final Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("&Configuration file path:");

		final Composite lineComposite = this.createDefaultComposite(composite, 2);

		final String configFilePath = CheckstylePreferences.INSTANCE.loadConfigFilePath(preferences);
		// Path text field
		this.configFilePathText = new Text(lineComposite, SWT.SINGLE | SWT.BORDER);
		final GridData gd = new GridData();
		gd.widthHint = this.convertWidthInCharsToPixels(60);
		this.configFilePathText.setLayoutData(gd);
		this.configFilePathText.setText(configFilePath);
		this.configFilePathText.addKeyListener(new ConfigFilePathTextListener(this));

		final Button browseButton = new Button(lineComposite, SWT.NONE);
		browseButton.setText("...");
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(final MouseEvent e) {
				final String message = "Select your configuration file for this project...";
				final ResourceSelectionDialog dialog = new ResourceSelectionDialog(CheckstylePropertyPage.this.getShell(),
						project.getParent(), message);

				final int returnCode = dialog.open();
				if (returnCode == Window.OK) {
					final Object[] selectedObjects = dialog.getResult();
					if (selectedObjects.length > 0) {
						final IFile selectedFilePath = (IFile) selectedObjects[0];
						final IPath projectRelativePath = PropertyPageUtils.computeRelativePath(project.getLocation(), selectedFilePath.getLocation());
						Display.getCurrent().asyncExec(new Runnable() {
							@Override
							public void run() {
								CheckstylePropertyPage.this.configFilePathText.setText(projectRelativePath.toString()); // NOPMD
								// Ignoring access to parent class properties, as this is necessary here for the strange
								// Eclipse API
							}
						});
					}
				}
			}
		});

	}

	/** Checkstyle enabled button. */
	private void createCheckstyleEnableCheckbox(final Composite composite, final IEclipsePreferences preferences) {
		this.enabledButton = new Button(composite, SWT.CHECK);
		this.enabledButton.setText("Checkstyle &enabled");
		final boolean selected = preferences.getBoolean(CheckstylePreferences.PROP_KEY_ENABLED, false);
		this.enabledButton.setSelection(selected);
	}

	private void createHintLabel(final Composite parent, final Composite composite, final String message) {
		final Label hintLabel = new Label(composite, SWT.NONE);
		hintLabel.setText(message);
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	private Label createFullWidthHintLabel(final Composite parent, final Composite composite, final String message) {
		final Label hintLabel = new Label(composite, SWT.NONE);
		hintLabel.setText(message);
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		hintLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		return hintLabel;
	}

	private void addSeparator(final Composite parent) {
		final Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	Text getConfigFilePathText() { // NOPMD (CommentDefaultAccessModifier) access only necessary by listener
		return this.configFilePathText;
	}

	Label getExampleConfigurationFilePathLabel() { // NOPMD (CommentDefaultAccessModifier) access only necessary by listener
		return this.exampleConfigurationFilePathLabel;
	}

	Text getCustomModulesJarPathsText() { // NOPMD (CommentDefaultAccessModifier) access only necessary by listener
		return this.customModulesJarPathsText;
	}

	Label getExampleCustomModulesLabel() { // NOPMD (CommentDefaultAccessModifier) access only necessary by listener
		return this.exampleCustomModulesLabel;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		final GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		this.addFirstSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(final Composite parent, final int numColumns) {
		final Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		final GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		// Populate the owner text field with the default value
		this.configFilePathText.setText(CheckstylePreferences.INVALID_CONFIG_FILE_PATH);
		this.customModulesJarPathsText.setText("");
		this.enabledButton.setSelection(false);
	}

	@Override
	public boolean performOk() {
		final IResource resource = this.getElement().getAdapter(IResource.class);
		final IEclipsePreferences preferences = CheckstylePreferences.INSTANCE
				.getProjectScopedPreferences(resource.getProject());

		preferences.put(CheckstylePreferences.PROP_KEY_CONFIG_FILE_PATH, this.configFilePathText.getText());
		preferences.put(CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS,
				this.customModulesJarPathsText.getText());
		preferences.putBoolean(CheckstylePreferences.PROP_KEY_ENABLED, this.enabledButton.getSelection());

		try {
			preferences.flush();
		} catch (final BackingStoreException e) {
			return false;
		}

		return true;
	}

}
