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

	private Text ruleSetFilePathText;
	private Text jarFilePathsText;
	private Button enabledButton;

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

		Label exampleLabel;

		// Label for path field
//		Label pathLabel = new Label(composite, SWT.NONE);
//		pathLabel.setText("&Ruleset file path:");
//
//		// Path text field
//		ruleSetFilePathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
//		GridData gd = new GridData();
//		gd.widthHint = convertWidthInCharsToPixels(60);
//		ruleSetFilePathText.setLayoutData(gd);
//		ruleSetFilePathText.setText(
//				preferences.get(CheckstylePreferences.PROP_KEY_RULE_SET_FILE_PATH, CheckstylePreferences.INVALID_RULESET_FILE_PATH));
//
//		Label exampleLabel = new Label(composite, SWT.NONE);
//		exampleLabel.setText("Example: conf/quality-config/pmd-ruleset.xml");
//		exampleLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

//		Label label = new Label(composite, SWT.NONE);
//		label.setText("Zero or more jar file paths with custom rule sets (comma separated):");
//
//		jarFilePathsText = new Text(composite, SWT.SINGLE | SWT.BORDER);
//		gd = new GridData();
//		gd.widthHint = convertWidthInCharsToPixels(60);
//		jarFilePathsText.setLayoutData(gd);
//		jarFilePathsText.setText(preferences.get(CheckstylePreferences.PROP_KEY_CUSTOM_RULES_JARS, ""));
//
//		exampleLabel = new Label(composite, SWT.NONE);
//		exampleLabel.setText("Example: config/pmd/custom-ruleset-0.jar, config/pmd/custom-ruleset-1.jar");
//		exampleLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));

		new Label(composite, SWT.NONE); // serves as newline

		exampleLabel = new Label(composite, SWT.NONE);
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
//		ruleSetFilePathText.setText(CheckstylePreferences.INVALID_RULESET_FILE_PATH);
		jarFilePathsText.setText("");
		enabledButton.setSelection(false);
	}

	@Override
	public boolean performOk() {
		IResource resource = getElement().getAdapter(IResource.class);
		IEclipsePreferences preferences = CheckstylePreferences.INSTANCE.getProjectScopedPreferences(resource.getProject());


//		preferences.put(CheckstylePreferences.PROP_KEY_RULE_SET_FILE_PATH, ruleSetFilePathText.getText());
//		preferences.put(CheckstylePreferences.PROP_KEY_CUSTOM_RULES_JARS, jarFilePathsText.getText());
		preferences.putBoolean(CheckstylePreferences.PROP_KEY_ENABLED, enabledButton.getSelection());

		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			return false;
		}

		return true;
	}

}