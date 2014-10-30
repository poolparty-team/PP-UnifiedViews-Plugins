package eu.unifiedviews.plugins.swc.poolparty.extract;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
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
    private final static String query = "CONSTRUCT {}";

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfData;


    public ThesaurusLinkExtractor() {
        super(ThesaurusLinkConfig.class);
    }

    @Override
    public void execute(DPUContext dpuContext) throws DPUException, InterruptedException {
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("PREFIX skos:<http://www.w3.org/2004/02/skos/core#> CONSTRUCT {?concept <").append(config.getLinkProperty()).append("> ?link } WHERE { ?concept a skos:Concept. ");
            queryBuilder.append("?concept <").append(config.getLinkProperty()).append("> ?link. }");
            logger.info(queryBuilder.toString());
            URL url = PPTApi.getServiceUrl(config.getApiConfig().getServer(), "PoolParty/sparql/" + config.getApiConfig().getUriSupplement() + "?format=application/rdf%2Bxml&query=" + URLEncoder.encode(queryBuilder.toString(), "UTF-8"));
            URLConnection connection = url.openConnection();
            config.getApiConfig().getAuthentication().visit(connection);

            RepositoryConnection repCon = rdfData.getConnection();
            repCon.add(connection.getInputStream(), "", RDFFormat.RDFXML);
        }
        catch (Exception e) {
            throw new DPUException(e);
        }
    }

    @Override
    public AbstractConfigDialog<ThesaurusLinkConfig> getConfigurationDialog() {
        return new ThesaurusLinkDialog();
    }

}
