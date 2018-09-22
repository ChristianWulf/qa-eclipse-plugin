package pmd.eclipse.plugin.pmd;

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

	public CancelablePmdProcessor(PMDConfiguration configuration, RuleSetFactory ruleSetFactory,
			List<Renderer> renderers) {
		this.configuration = configuration;
		this.ruleSetFactory = ruleSetFactory;
		this.renderers = renderers;

		this.processor = new SourceCodeProcessor(configuration);
	}

	public void onStarted() {
		RuleSets rs = createRuleSets(ruleSetFactory);
		configuration.getAnalysisCache().checkValidity(rs, configuration.getClassLoader());
	}

	public void processFile(DataSource dataSource, RuleContext context) {
		String niceFileName = filenameFrom(dataSource);
		RuleSets rs = createRuleSets(ruleSetFactory);

		PmdRunnable pmdRunnable = new PmdRunnable(dataSource, niceFileName, renderers, context, rs, processor);

		Report resultReport = pmdRunnable.call();

		reports.add(resultReport);
	}

	public void onFinished() {
		collectReports(renderers);
	}

	private String filenameFrom(DataSource dataSource) {
		return dataSource.getNiceFileName(configuration.isReportShortNames(), configuration.getInputPaths());
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
	private RuleSets createRuleSets(RuleSetFactory factory) {
		return RulesetsFactoryUtils.getRuleSets(configuration.getRuleSets(), factory);
	}

	private void collectReports(List<Renderer> renderers) {
		for (Report report : reports) {
			for (Renderer r : renderers) {
				try {
					r.renderFileReport(report);
				} catch (IOException e) {
					// on exception: ignore specific renderer
				}
			}
		}

		// Since this thread may run PMD again, clean up the runnable
		PmdRunnable.reset();
	}
}
