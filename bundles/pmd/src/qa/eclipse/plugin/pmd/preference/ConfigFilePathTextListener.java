package qa.eclipse.plugin.pmd.preference;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import qa.eclipse.plugin.bundles.common.ProjectUtil;

class ConfigFilePathTextListener extends KeyAdapter {

	private static final String NON_EXISTING_FILE_TEXT = "Attention: the file path does not point to an existing file.";
	private static final String ABSOLUTE_FILE_PATH_TEXT = "Attention: an absolute file path is not recommended if you work in a team.";

	private final PmdPropertyPage propertyPage;

	public ConfigFilePathTextListener(PmdPropertyPage propertyPage) {
		this.propertyPage = propertyPage;
	}

	@Override
	public void keyReleased(KeyEvent event) {
		Object source = event.getSource();
		Text textField = propertyPage.getRuleSetFilePathText();
		if (source != textField) {
			return;
		}

		String text = textField.getText();

		Label label = propertyPage.getRuleSetFilePathLabel();
		Path path;
		try {
			path = Paths.get(text);
		} catch (InvalidPathException e) {
			// for example, on Windows, ck:/ instead of c:/
			label.setText(NON_EXISTING_FILE_TEXT);
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		if (path.isAbsolute()) {
			label.setText(ABSOLUTE_FILE_PATH_TEXT);
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		Path absoluteProjectPath = ProjectUtil.getAbsoluteProjectPath(propertyPage);
		Path absoluteConfigFilePath = absoluteProjectPath.resolve(path);

		if (!Files.exists(absoluteConfigFilePath)) {
			label.setText(NON_EXISTING_FILE_TEXT);
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		label.setText(PmdPropertyPage.RULE_SET_FILE_EXAMPLE_TEXT);
		label.setForeground(textField.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

}
