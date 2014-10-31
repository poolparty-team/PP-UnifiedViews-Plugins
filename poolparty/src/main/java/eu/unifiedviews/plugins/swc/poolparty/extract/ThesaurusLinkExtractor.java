package eu.unifiedviews.plugins.swc.poolparty.extract;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.api.PptApiConnector;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

@DPU.AsExtractor
public class ThesaurusLinkExtractor extends ConfigurableBase<ThesaurusLinkConfig> implements ConfigDialogProvider<ThesaurusLinkConfig> {

    private final Logger logger = LoggerFactory.getLogger(ThesaurusLinkExtractor.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    private RepositoryConnection dataUnitConnection;

    public ThesaurusLinkExtractor() {
        super(ThesaurusLinkConfig.class);
    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException, InterruptedException {
        try {
            String extractionQuery = createExtractionQuery(config.getLinkProperty());
            PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
            URL url = PptApiConnector.getServiceUrl(poolPartyApiConfig.getServer(),
                    "PoolParty/sparql/" + poolPartyApiConfig.getUriSupplement() +
                            "?format=application/rdf%2Bxml&query=" + URLEncoder.encode(extractionQuery, "UTF-8"));
            logger.debug(extractionQuery);

            URLConnection pptConnection = url.openConnection();
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
            dataUnitConnection.close();
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

    @Override
    public AbstractConfigDialog<ThesaurusLinkConfig> getConfigurationDialog() {
        return new ThesaurusLinkDialog();
    }

}
