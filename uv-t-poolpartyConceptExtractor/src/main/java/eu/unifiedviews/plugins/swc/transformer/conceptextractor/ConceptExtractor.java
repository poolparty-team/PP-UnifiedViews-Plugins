package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;
import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

import java.util.List;
import java.util.UUID;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsExtractor
public class ConceptExtractor extends AbstractDpu<ConceptExtractorConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptExtractor.class);

    public RepositoryResult<Statement> graphStatements = null;
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit input;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit output;

	public ConceptExtractor() {
		super(ConceptExtractorVaadinDialog.class, ConfigHistory.noHistory(ConceptExtractorConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        String server = config.getServer();
        String language = config.getLanguage();
        String projectId = config.getProjectId();


        ContextUtils.sendShortInfo(ctx, "Tabular.message");

        faultTolerance.execute();
    }


    private Model getGraphModel(final List<RDFDataUnit.Entry> sourceEntries) throws DPUException {
        for (final RDFDataUnit.Entry entry : sourceEntries) {
            FaultToleranceUtils.asGraph(faultTolerance, entry).;
        }
    }

    private String getQueryString() throws DPUException {
        return faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {
            @Override
            public String action() throws Exception {
                final List<RDFDataUnit.Entry> sources = DataUnitUtils.getEntries(input, RDFDataUnit.Entry.class);
                return QUERY.replaceFirst("(?i)WHERE",
                        SparqlUtils.prepareClause("FROM", sources) + "WHERE ");
            }
        });
    }

    private void loadGraphStatements() throws DPUException {
        faultTolerance.execute(input, new FaultTolerance.ConnectionAction() {
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                graphStatements = connection.getStatements(null, null, null, false);
            }
        });
    }

    private void executeConceptExtraction() {
        try {
            if (graphStatements != null && graphStatements.hasNext()) {
                while (graphStatements.hasNext()) {
                    Statement statement = graphStatements.next();
                    Resource subject = statement.getSubject();
                    String predicateLocalName = statement.getPredicate().getLocalName();
                    String text = statement.getObject().stringValue();
                    URI tagPredicate = new URIImpl("http://schema.semantic-web.at/ppx/taggedResourceFor"
                            + predicateLocalName.charAt(0) + predicateLocalName.substring(1));
                    URI taggedResource = new URIImpl("http://schema.semantic-web.at/ppx/" + predicateLocalName + "/"
                            + UUID.randomUUID().toString() + "#id");

                    
                }
            }
        } catch (RepositoryException e) {

        }

    }

    String uri = "http://poolparty.capsenta.com:8080/extractor/api/annotate?projectId=1DCE358B-0316-0001-D178-1C371D0019B0&language=en&text=politics&documentUri=SWC:1";
}
