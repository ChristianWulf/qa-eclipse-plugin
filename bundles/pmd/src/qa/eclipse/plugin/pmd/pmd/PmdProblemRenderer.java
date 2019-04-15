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

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.renderers.AbstractRenderer;
import net.sourceforge.pmd.util.datasource.DataSource;

/**
 *
 * @author Christian Wulf
 *
 */
class PmdProblemRenderer extends AbstractRenderer {

	private final Report problemReport = new Report();

	public PmdProblemRenderer() {
		super(PmdProblemRenderer.class.getName(),
				"Renderer that collects PMD problems, e.g., violations and processing errors");
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
		// do nothing
	}

	@Override
	public void renderFileReport(final Report workerThreadReport) throws IOException {
		this.problemReport.merge(workerThreadReport);
	}

	@Override
	public void end() throws IOException {
		// do nothing
	}

	public Report getProblemReport() {
		return this.problemReport;
	}

}
