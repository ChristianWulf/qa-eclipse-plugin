package pmdeclipseplugin.pmd;

import java.io.IOException;
import java.util.Iterator;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.Report.RuleConfigurationError;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractRenderer;
import net.sourceforge.pmd.util.datasource.DataSource;

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
	public void startFileAnalysis(DataSource dataSource) {
		// do nothing
	}

	@Override
	public void renderFileReport(Report report) throws IOException {
		for (RuleViolation v : report) {
			problemReport.addRuleViolation(v);
		}
		for (Iterator<ProcessingError> it = report.errors(); it.hasNext();) {
			problemReport.addError(it.next());
		}
		for (Iterator<RuleConfigurationError> it = report.configErrors(); it.hasNext();) {
			problemReport.addConfigError(it.next());
		}
	}

	@Override
	public void end() throws IOException {
		// do nothing
	}

	public Report getProblemReport() {
		return problemReport;
	}

}
