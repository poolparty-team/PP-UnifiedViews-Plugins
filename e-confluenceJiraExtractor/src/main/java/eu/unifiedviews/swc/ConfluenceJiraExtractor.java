package eu.unifiedviews.swc;

import biz.poolparty.aligned.unifiedgovernance.UnifiedGovernance;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFWriterBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsExtractor
public class ConfluenceJiraExtractor extends AbstractDpu<ConfluenceJiraExtractorConfig> {

    private static final Logger log = LoggerFactory.getLogger(ConfluenceJiraExtractor.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit out;

	public ConfluenceJiraExtractor() {
		super(ConfluenceJiraExtractorDialog.class, ConfigHistory.noHistory(ConfluenceJiraExtractorConfig.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "ConfluenceJira.message");

        try {
            ConnectionRdfWriter connectionRdfWriter =  new ConnectionRdfWriter(out.getConnection());
            UnifiedGovernance.extract(config.getConfluenceApiBaseUri(),
                    config.getJiraApiBaseUri(),
                    config.getJiraProjectKeys(),
                    config.getUsername(),
                    config.getPassword(),
                    connectionRdfWriter);
            log.info("Extracted " +connectionRdfWriter.statementCount+ " statements");
        }
        catch (Exception e) {
            log.error("Error extracting data", e);
        }
    }

    private class ConnectionRdfWriter extends RDFWriterBase {

        private RepositoryConnection repCon;
        private int statementCount = 0;

        ConnectionRdfWriter(RepositoryConnection repCon) {
            this.repCon = repCon;
        }

        @Override
        public RDFFormat getRDFFormat() {
            return null;
        }

        @Override
        public void startRDF() throws RDFHandlerException {
        }

        @Override
        public void endRDF() throws RDFHandlerException {
        }

        @Override
        public void handleStatement(Statement statement) throws RDFHandlerException {
            try {
                repCon.add(statement);
                statementCount++;
            }
            catch (RepositoryException e) {
                log.error("Error handling extracted statement", e);
            }
        }

        @Override
        public void handleComment(String s) throws RDFHandlerException {
        }
    }
}