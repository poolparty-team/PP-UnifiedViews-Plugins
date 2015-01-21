package eu.unifiedviews.plugins.swc.poolparty.extract;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.util.TestWritableRdfUnit;
import org.junit.Test;

public class ThesaurusLinkExtractorTest {

    @Test
    public void extractFromSampleProject() throws Exception {
        ThesaurusLinkExtractor extractor = new ThesaurusLinkExtractor();
        extractor.configureDirectly(createExtractorConfig());
        extractor.rdfOutput = new TestWritableRdfUnit();
        extractor.execute(null);

        System.out.println(extractor.rdfOutput.getConnection().getStatements(null,null,null,false).asList().toString());
    }

    private ThesaurusLinkConfig createExtractorConfig() {
        ThesaurusLinkConfig config = new ThesaurusLinkConfig();
        config.setApiConfig(createApiConfig());
        return config;
    }

    private PoolPartyApiConfig createApiConfig() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        //config.setServer("http://test-pp-linux.semantic-web.at/");
        config.setServer("http://localhost:8080");
        config.setUriSupplement("test_unifiedviews");
        return config;
    }

}
