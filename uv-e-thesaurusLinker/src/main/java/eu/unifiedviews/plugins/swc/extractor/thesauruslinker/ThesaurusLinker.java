package eu.unifiedviews.plugins.swc.extractor.thesauruslinker;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.plugins.swc.api.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.api.PptApiConnector;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@DPU.AsExtractor
public class ThesaurusLinker extends AbstractDpu<ThesaurusLinkerConfig_V1> {

    private final Logger logger = LoggerFactory.getLogger(ThesaurusLinker.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    private RepositoryConnection dataUnitConnection;

    public ThesaurusLinker() {
        super(ThesaurusLinkerVaadinDialog.class, ConfigHistory.noHistory(ThesaurusLinkerConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        try {
            PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
            URLConnection pptConnection = establishPptConnection(poolPartyApiConfig);
            poolPartyApiConfig.getAuthentication().visit(pptConnection);

            establishDataUnitConnection();
            dataUnitConnection.add(pptConnection.getInputStream(), "", RDFFormat.RDFXML);
        }
        catch (Exception e) {
            throw new DPUException(e);
        }
        finally {
            closeDataUnitConnection();
        }
    }

    public URLConnection establishPptConnection(PoolPartyApiConfig poolPartyApiConfig) throws IOException
    {
        String extractionQuery = createExtractionQuery(config.getLinkProperty());
        URL url = PptApiConnector.getServiceUrl(poolPartyApiConfig.getServer(),
                "PoolParty/sparql/" + poolPartyApiConfig.getUriSupplement() +
                        "?format=application/rdf%2Bxml&query=" + URLEncoder.encode(extractionQuery, "UTF-8"));
        return url.openConnection();
    }

    private void establishDataUnitConnection() {
        try {
            dataUnitConnection = rdfOutput.getConnection();
        }
        catch (DataUnitException e) {
            logger.error("Error connecting to data unit", e);
        }
    }

    private void closeDataUnitConnection() {
        try {
            if (dataUnitConnection != null) dataUnitConnection.close();
        }
        catch (RepositoryException e) {
            logger.error("Error closing connection to data unit");
        }
    }

    private String createExtractionQuery(String linkProperty) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("PREFIX skos:<http://www.w3.org/2004/02/skos/core#>")
            .append("CONSTRUCT {?concept <").append(linkProperty).append("> ?link } WHERE { ?concept a skos:Concept. ")
            .append("?concept <").append(linkProperty).append("> ?link. }");
        return queryBuilder.toString();
    }

}
