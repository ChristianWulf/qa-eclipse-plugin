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
package qa.eclipse.plugin.pmd.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

public class PmdPropertyPage extends PropertyPage {

	static final String RULE_SET_FILE_EXAMPLE_TEXT = "Example: conf/quality-config/pmd-ruleset.xml";
	static final String CUSTOM_JAR_PATHS_EXAMPLE_TEXT = "Example: config/pmd/custom-ruleset-0.jar, config/pmd/custom-ruleset-1.jar";

	private Text ruleSetFilePathText;
	private Text customJarFilePathsText;
	private Button enabledButton;
	private Label ruleSetFilePathLabel;
	private Label customJarFilePathsLabel;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public PmdPropertyPage() {
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
		} catch (final CoreException e) {
			// ignore
		}

		final IEclipsePreferences preferences = PmdPreferences.INSTANCE.getProjectScopedPreferences(project);

		this.enabledButton = new Button(composite, SWT.CHECK);
		this.enabledButton.setText("PMD &enabled");
		final boolean selected = preferences.getBoolean(PmdPreferences.PROP_KEY_ENABLED, false);
		this.enabledButton.setSelection(selected);

		Label hintLabel = new Label(composite, SWT.NONE);
		hintLabel.setText("Hint: Disabling PMD clears all violations.");
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		this.addSeparator(composite);

		// Label for path field
		final Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("&Ruleset file path:");

		Composite lineComposite = this.createDefaultComposite(composite, 2);

		final String ruleSetFilePath = preferences.get(PmdPreferences.PROP_KEY_RULE_SET_FILE_PATH,
				PmdPreferences.INVALID_RULESET_FILE_PATH);
		// Path text field
		this.ruleSetFilePathText = new Text(lineComposite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = this.convertWidthInCharsToPixels(60);
		this.ruleSetFilePathText.setLayoutData(gd);
		this.ruleSetFilePathText.setText(ruleSetFilePath);
		this.ruleSetFilePathText.addKeyListener(new ConfigFilePathTextListener(this));

		final Button browseButton = new Button(lineComposite, SWT.NONE);
		browseButton.setText("...");
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(final MouseEvent e) {
				final String message = "Select your rule set file for this project...";
				final ResourceSelectionDialog dialog = new ResourceSelectionDialog(PmdPropertyPage.this.getShell(), project, message);
				final int returnCode = dialog.open();
				if (returnCode == Window.OK) {
					final Object[] selectedObjects = dialog.getResult();
					if (selectedObjects.length > 0) {
						final IFile selectedFilePath = (IFile) selectedObjects[0];
						final IPath projectRelativePath = selectedFilePath.getProjectRelativePath();
						Display.getCurrent().asyncExec(new Runnable() {
							@Override
							public void run() {
								PmdPropertyPage.this.ruleSetFilePathText.setText(projectRelativePath.toString());
							}
						});
					}
				}
			}
		});

		this.ruleSetFilePathLabel = new Label(composite, SWT.NONE);
		this.ruleSetFilePathLabel.setText(PmdPropertyPage.RULE_SET_FILE_EXAMPLE_TEXT);
		this.ruleSetFilePathLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		this.ruleSetFilePathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Zero or more jar file paths with custom rule sets (comma separated):");

		lineComposite = this.createDefaultComposite(composite, 2);

		this.customJarFilePathsText = new Text(lineComposite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = this.convertWidthInCharsToPixels(60);
		this.customJarFilePathsText.setLayoutData(gd);
		this.customJarFilePathsText.setText(preferences.get(PmdPreferences.PROP_KEY_CUSTOM_RULES_JARS, ""));
		this.customJarFilePathsText.addKeyListener(new CustomModulesKeyListener(this));

		// browseButton = new Button(lineComposite, SWT.NONE);
		browseButton.setText("...");
		browseButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(final MouseEvent e) {
				final String message = "Select your custom rule jar file(s) for this project...";
				final ResourceSelectionDialog dialog = new ResourceSelectionDialog(PmdPropertyPage.this.getShell(), project, message);
				final int returnCode = dialog.open();
				if (returnCode == Window.OK) {
					final Object[] selectedObjects = dialog.getResult();
					if (selectedObjects.length == 0) {
						return;
					}

					final List<String> filePathNames = new ArrayList<>();
					for (final Object selectedObject : selectedObjects) {
						final IFile selectedFilePath = (IFile) selectedObject;
						final IPath projectRelativePath = selectedFilePath.getProjectRelativePath();
						final String filePathName = projectRelativePath.toString();
						filePathNames.add(filePathName);
					}

					Display.getCurrent().asyncExec(new Runnable() {
						@Override
						public void run() {
							PmdPropertyPage.this.customJarFilePathsText.setText(StringUtils.join(filePathNames, ','));
						}
					});
				}
			}
		});

		this.customJarFilePathsLabel = new Label(composite, SWT.NONE);
		this.customJarFilePathsLabel.setText(PmdPropertyPage.CUSTOM_JAR_PATHS_EXAMPLE_TEXT);
		this.customJarFilePathsLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		this.customJarFilePathsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		new Label(composite, SWT.NONE); // serves as newline

		final Label exampleLabel = new Label(composite, SWT.NONE);
		exampleLabel.setText("To hide the violation flags in the (Package/Project) Explorer, " + System.lineSeparator()
		+ "open Eclipse's global preferences and search for 'Label Decorations'.");
		// exampleLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		new Label(composite, SWT.NONE); // serves as newline

		this.addSeparator(composite);

		hintLabel = new Label(composite, SWT.NONE);
		// Image infoImage =
		// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		// hintLabel.setImage(infoImage);
		hintLabel.setText("Hint: Relative paths are resolved relative to the project's path.");
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	private void addSeparator(final Composite parent) {
		final Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		final GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	Text getRuleSetFilePathText() {
		return this.ruleSetFilePathText;
	}

	Label getRuleSetFilePathLabel() {
		return this.ruleSetFilePathLabel;
	}

	Text getCustomJarFilePathsText() {
		return this.customJarFilePathsText;
	}

	Label getCustomJarFilePathsLabel() {
		return this.customJarFilePathsLabel;
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
		layout.marginWidth = 0;
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
		this.ruleSetFilePathText.setText(PmdPreferences.INVALID_RULESET_FILE_PATH);
		this.customJarFilePathsText.setText("");
		this.enabledButton.setSelection(false);
	}

	@Override
	public boolean performOk() {
		final IResource resource = this.getElement().getAdapter(IResource.class);
		final IEclipsePreferences preferences = PmdPreferences.INSTANCE
				.getProjectScopedPreferences(resource.getProject());

		// try {
		// preferences.sync();
		// } catch (BackingStoreException e) {
		// // ignore
		// }

		preferences.put(PmdPreferences.PROP_KEY_RULE_SET_FILE_PATH, this.ruleSetFilePathText.getText());
		preferences.put(PmdPreferences.PROP_KEY_CUSTOM_RULES_JARS, this.customJarFilePathsText.getText());
		preferences.putBoolean(PmdPreferences.PROP_KEY_ENABLED, this.enabledButton.getSelection());

		try {
			preferences.flush();
		} catch (final BackingStoreException e) {
			return false;
		}

		return true;
	}

}