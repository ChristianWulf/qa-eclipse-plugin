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

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import qa.eclipse.plugin.bundles.common.ProjectUtils;

/**
 *
 *
 * @author Christian Wulf
 *
 */
class ConfigFilePathTextListener extends KeyAdapter {

	private static final String NON_EXISTING_FILE_TEXT = "Attention: the file path does not point to an existing file.";
	private static final String ABSOLUTE_FILE_PATH_TEXT = "Attention: an absolute file path is not recommended if you work in a team.";

	private final PmdPropertyPage propertyPage;

	public ConfigFilePathTextListener(final PmdPropertyPage propertyPage) {
		this.propertyPage = propertyPage;
	}

	@Override
	public void keyReleased(final KeyEvent event) {
		final Object source = event.getSource();
		final Text textField = propertyPage.getRuleSetFilePathText();
		if (source != textField) {
			return;
		}

		final String text = textField.getText();

		final Label label = propertyPage.getRuleSetFilePathLabel();
		final Path path;
		try {
			path = Paths.get(text);
		} catch (final InvalidPathException e) {
			// for example, on Windows, ck:/ instead of c:/
			label.setText(ConfigFilePathTextListener.NON_EXISTING_FILE_TEXT);
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		if (path.isAbsolute()) {
			label.setText(ConfigFilePathTextListener.ABSOLUTE_FILE_PATH_TEXT);
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		final Path absoluteProjectPath = ProjectUtils.getAbsoluteProjectPath(propertyPage);
		final Path absoluteConfigFilePath = absoluteProjectPath.resolve(path);

		if (!Files.exists(absoluteConfigFilePath)) {
			label.setText(ConfigFilePathTextListener.NON_EXISTING_FILE_TEXT);
			label.setForeground(label.getDisplay().getSystemColor(SWT.COLOR_RED));
			return;
		}

		label.setText(PmdPropertyPage.RULE_SET_FILE_EXAMPLE_TEXT);
		label.setForeground(textField.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
	}

}
