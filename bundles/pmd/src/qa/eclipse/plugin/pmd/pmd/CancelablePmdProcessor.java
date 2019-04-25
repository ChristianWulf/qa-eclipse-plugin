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
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.SourceCodeProcessor;
import net.sourceforge.pmd.processor.PmdRunnable;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.datasource.DataSource;

/**
 * Represents a PMD processor which can be canceled before each file.
 * <p>
 * This implementation uses parts of
 * {@link net.sourceforge.pmd.processor.AbstractPMDProcessor} and
 * {@link net.sourceforge.pmd.processor.MonoThreadProcessor}.
 *
 * @author Christian Wulf (chw)
 *
 */
class CancelablePmdProcessor {

	private final PMDConfiguration configuration;
	private final List<Report> reports = new ArrayList<>();
	private final RuleSetFactory ruleSetFactory;
	private final SourceCodeProcessor processor;
	private final List<Renderer> renderers;

	public CancelablePmdProcessor(final PMDConfiguration configuration, final RuleSetFactory ruleSetFactory,
			final List<Renderer> renderers) {
		this.configuration = configuration;
		this.ruleSetFactory = ruleSetFactory;
		this.renderers = renderers;

		this.processor = new SourceCodeProcessor(configuration);
	}

	public void onStarted() {
		final RuleSets rs = this.createRuleSets(this.ruleSetFactory);
		this.configuration.getAnalysisCache().checkValidity(rs, this.configuration.getClassLoader());
	}

	public void processFile(final DataSource dataSource, final RuleContext context) {
		final String niceFileName = this.filenameFrom(dataSource);

		final RuleSets rs = this.createRuleSets(this.ruleSetFactory);

		final PmdRunnable pmdRunnable = new PmdRunnable(dataSource, niceFileName, this.renderers, context, rs, this.processor);

		final Report resultReport = pmdRunnable.call();

		this.reports.add(resultReport);
	}

	public void onFinished() {
		this.collectReports(this.renderers);
	}

	private String filenameFrom(final DataSource dataSource) {
		return dataSource.getNiceFileName(this.configuration.isReportShortNames(), this.configuration.getInputPaths());
	}

	/**
	 * Create instances for each rule defined in the ruleset(s) in the
	 * configuration. Please note, that the returned instances <strong>must
	 * not</strong> be used by different threads. Each thread must create its own
	 * copy of the rules.
	 *
	 * @param factory
	 * @return the rules within a rulesets
	 */
	private RuleSets createRuleSets(final RuleSetFactory factory) {
		return RulesetsFactoryUtils.getRuleSets(this.configuration.getRuleSets(), factory);
	}

	private void collectReports(final List<Renderer> localRenderers) {
		for (final Report report : this.reports) {
			for (final Renderer r : localRenderers) {
				try {
					r.renderFileReport(report);
				} catch (final IOException e) { // NOPMD we want to ignore errors here
					// on exception: ignore specific renderer
				}
			}
		}

		// Since this thread may run PMD again, clean up the runnable
		PmdRunnable.reset();
	}
}
