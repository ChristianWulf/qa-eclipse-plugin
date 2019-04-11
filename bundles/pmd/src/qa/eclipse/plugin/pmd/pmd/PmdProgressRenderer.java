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

import java.io.IOException;

import org.eclipse.core.runtime.SubMonitor;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.renderers.AbstractRenderer;
import net.sourceforge.pmd.util.datasource.DataSource;

public class PmdProgressRenderer extends AbstractRenderer {

	private final SubMonitor subMonitor;

	public PmdProgressRenderer(final SubMonitor subMonitor) {
		super(PmdProgressRenderer.class.getName(), "Renderer that informs about the progress");
		this.subMonitor = subMonitor;
	}

	@Override
	public String defaultFileExtension() {
		return null;
	}

	@Override
	public void start() throws IOException {
		// do nothing
	}

	@Override
	public void startFileAnalysis(final DataSource dataSource) {
		subMonitor.split(1);
	}

	@Override
	public void renderFileReport(final Report report) throws IOException {
		// do nothing
	}

	@Override
	public void end() throws IOException {
		// do nothing
	}

}
