package qa.eclipse.plugin.bundles.checkstyle.preference;

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

public class CheckstylePropertyPage extends PropertyPage {

	static final String CONFIG_FILE_PATH_DEFAULT_TEXT = "Example: conf/quality-config/cs-conf.xml";
	static final String CUSTOM_MODULES_DEFAULT_TEXT = "Example: config/checkstyle/custom-modules-0.jar, config/checkstyle/custom-modules-1.jar";

	private Text configFilePathText;
	private Text customModulesJarPathsText;
	private Button enabledButton;
	private Label exampleLabel;
	private Label exampleCustomModulesLabel;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public CheckstylePropertyPage() {
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

		IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(project);

		enabledButton = new Button(composite, SWT.CHECK);
		enabledButton.setText("Checkstyle &enabled");
		boolean selected = preferences.getBoolean(CheckstylePreferences.PROP_KEY_ENABLED, false);
		enabledButton.setSelection(selected);

		Label hintLabel = new Label(composite, SWT.NONE);
		hintLabel.setText("Hint: Disabling Checkstyle clears all violations.");
		hintLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		addSeparator(composite);

		// Label for path field
		Label pathLabel = new Label(composite, SWT.NONE);
		pathLabel.setText("&Configuration file path:");

		// Path text field
		configFilePathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(60);
		configFilePathText.setLayoutData(gd);
		configFilePathText.setText(CheckstylePreferences.INSTANCE.loadConfigFilePath(preferences));
		configFilePathText.addKeyListener(new ConfigFilePathTextListener(this));

		exampleLabel = new Label(composite, SWT.NONE);
		exampleLabel.setText(CONFIG_FILE_PATH_DEFAULT_TEXT);
		exampleLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		exampleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Zero or more jar file paths with custom modules (comma separated):");

		customModulesJarPathsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(60);
		customModulesJarPathsText.setLayoutData(gd);
		customModulesJarPathsText.setText(preferences.get(CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS, ""));
		customModulesJarPathsText.addKeyListener(new CustomModulesKeyListener(this));

		exampleCustomModulesLabel = new Label(composite, SWT.NONE);
		exampleCustomModulesLabel.setText(CUSTOM_MODULES_DEFAULT_TEXT);
		exampleCustomModulesLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		exampleCustomModulesLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		new Label(composite, SWT.NONE); // serves as newline

		Label hideFlagsHintLabel = new Label(composite, SWT.NONE);
		hideFlagsHintLabel.setText("To hide the violation flags in the (Package/Project) Explorer, "
				+ System.lineSeparator() + "open Eclipse's global preferences and search for 'Label Decorations'.");
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

	Text getConfigFilePathText() {
		return configFilePathText;
	}

	Label getExampleLabel() {
		return exampleLabel;
	}

	Text getCustomModulesJarPathsText() {
		return customModulesJarPathsText;
	}

	Label getExampleCustomModulesLabel() {
		return exampleCustomModulesLabel;
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
		configFilePathText.setText(CheckstylePreferences.INVALID_CONFIG_FILE_PATH);
		customModulesJarPathsText.setText("");
		enabledButton.setSelection(false);
	}

	@Override
	public boolean performOk() {
		IResource resource = getElement().getAdapter(IResource.class);
		IEclipsePreferences preferences = CheckstylePreferences.INSTANCE
				.getProjectScopedPreferences(resource.getProject());

		preferences.put(CheckstylePreferences.PROP_KEY_CONFIG_FILE_PATH, configFilePathText.getText());
		preferences.put(CheckstylePreferences.PROP_KEY_CUSTOM_MODULES_JAR_PATHS, customModulesJarPathsText.getText());
		preferences.putBoolean(CheckstylePreferences.PROP_KEY_ENABLED, enabledButton.getSelection());

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			return false;
		}

		return true;
	}

}