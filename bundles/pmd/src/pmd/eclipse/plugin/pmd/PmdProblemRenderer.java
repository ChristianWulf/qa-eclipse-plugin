package pmd.eclipse.plugin.pmd;

import java.io.IOException;

import net.sourceforge.pmd.Report;
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
	public void renderFileReport(Report workerThreadReport) throws IOException {
		problemReport.merge(workerThreadReport);
	}

	@Override
	public void end() throws IOException {
		// do nothing
	}

	public Report getProblemReport() {
		return problemReport;
	}

}
