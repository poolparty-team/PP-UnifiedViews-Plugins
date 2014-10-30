package eu.unifiedviews.plugins.swc.poolparty.extract;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.api.PPTApi;
import org.openrdf.repository.RepositoryConnection;
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

    public ThesaurusLinkExtractor() {
        super(ThesaurusLinkConfig.class);
    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException, InterruptedException {
        try {
            PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
            String extractionQuery = createExtractionQuery(config.getLinkProperty());
            logger.debug(extractionQuery);

            URL url = PPTApi.getServiceUrl(poolPartyApiConfig.getServer(),
                    "PoolParty/sparql/" + poolPartyApiConfig.getUriSupplement() +
                    "?format=application/rdf%2Bxml&query=" + URLEncoder.encode(extractionQuery, "UTF-8"));

            URLConnection pptConnection = url.openConnection();
            poolPartyApiConfig.getAuthentication().visit(pptConnection);

            RepositoryConnection rdfOutputConnection = rdfOutput.getConnection();
            rdfOutputConnection.add(pptConnection.getInputStream(), "", RDFFormat.RDFXML);
        }
        catch (Exception e) {
            throw new DPUException(e);
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
