package pmd.eclipse.plugin.preference;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
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

	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent, 1);

		IResource resource = getElement().getAdapter(IResource.class);
		IProject project = resource.getProject();

		// ensure that the properties displayed are in sync with the corresponding prefs
		// file
		try {
			project.getFolder(".settings").refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// ignore
		}

		IEclipsePreferences preferences = PmdPreferences.INSTANCE.getProjectScopedPreferences(project);

		enabledButton = new Button(composite, SWT.CHECK);
		enabledButton.setText("PMD &enabled");
		boolean selected = preferences.getBoolean(PmdPreferences.PROP_KEY_ENABLED, false);
		enabledButton.setSelection(selected);

		Label hintLabel = new Label(composite, SWT.NONE);
		hintLabel.setText("Hint: Disabling PMD clears all violations.");
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		addSeparator(composite);

		// Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("&Ruleset file path:");

		String ruleSetFilePath = preferences.get(PmdPreferences.PROP_KEY_RULE_SET_FILE_PATH,
				PmdPreferences.INVALID_RULESET_FILE_PATH);
		// Path text field
		ruleSetFilePathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(60);
		ruleSetFilePathText.setLayoutData(gd);
		ruleSetFilePathText.setText(ruleSetFilePath);
		ruleSetFilePathText.addKeyListener(new ConfigFilePathTextListener(this));

		ruleSetFilePathLabel = new Label(composite, SWT.NONE);
		ruleSetFilePathLabel.setText(RULE_SET_FILE_EXAMPLE_TEXT);
		ruleSetFilePathLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		ruleSetFilePathLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Zero or more jar file paths with custom rule sets (comma separated):");

		customJarFilePathsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(60);
		customJarFilePathsText.setLayoutData(gd);
		customJarFilePathsText.setText(preferences.get(PmdPreferences.PROP_KEY_CUSTOM_RULES_JARS, ""));
		customJarFilePathsText.addKeyListener(new CustomModulesKeyListener(this));

		customJarFilePathsLabel = new Label(composite, SWT.NONE);
		customJarFilePathsLabel.setText(CUSTOM_JAR_PATHS_EXAMPLE_TEXT);
		customJarFilePathsLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		customJarFilePathsLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		new Label(composite, SWT.NONE); // serves as newline

		Label exampleLabel = new Label(composite, SWT.NONE);
		exampleLabel.setText("To hide the violation flags in the (Package/Project) Explorer, " + System.lineSeparator()
				+ "open Eclipse's global preferences and search for 'Label Decorations'.");
		// exampleLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		new Label(composite, SWT.NONE); // serves as newline

		addSeparator(composite);

		hintLabel = new Label(composite, SWT.NONE);
		// Image infoImage =
		// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		// hintLabel.setImage(infoImage);
		hintLabel.setText("Hint: Relative paths are resolved relative to the project's path.");
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	Text getRuleSetFilePathText() {
		return ruleSetFilePathText;
	}

	Label getRuleSetFilePathLabel() {
		return ruleSetFilePathLabel;
	}

	Text getCustomJarFilePathsText() {
		return customJarFilePathsText;
	}

	Label getCustomJarFilePathsLabel() {
		return customJarFilePathsLabel;
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		// Populate the owner text field with the default value
		ruleSetFilePathText.setText(PmdPreferences.INVALID_RULESET_FILE_PATH);
		customJarFilePathsText.setText("");
		enabledButton.setSelection(false);
	}

	@Override
	public boolean performOk() {
		IResource resource = getElement().getAdapter(IResource.class);
		IEclipsePreferences preferences = PmdPreferences.INSTANCE.getProjectScopedPreferences(resource.getProject());

		// try {
		// preferences.sync();
		// } catch (BackingStoreException e) {
		// // ignore
		// }

		preferences.put(PmdPreferences.PROP_KEY_RULE_SET_FILE_PATH, ruleSetFilePathText.getText());
		preferences.put(PmdPreferences.PROP_KEY_CUSTOM_RULES_JARS, customJarFilePathsText.getText());
		preferences.putBoolean(PmdPreferences.PROP_KEY_ENABLED, enabledButton.getSelection());

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			return false;
		}

		return true;
	}

}