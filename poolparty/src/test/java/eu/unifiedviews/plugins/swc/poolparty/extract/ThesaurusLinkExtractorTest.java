package eu.unifiedviews.plugins.swc.poolparty.extract;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.util.TestWritableRdfUnit;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.vocabulary.SKOS;

public class ThesaurusLinkExtractorTest {

    @Test
    public void extractFromSampleProject() throws Exception {
        ThesaurusLinkExtractor extractor = new ThesaurusLinkExtractor();
        extractor.configureDirectly(createExtractorConfig());
        extractor.rdfOutput = new TestWritableRdfUnit();
        extractor.execute(null);

        Assert.assertFalse(extractor.rdfOutput.getConnection().isEmpty());
    }

    private ThesaurusLinkConfig createExtractorConfig() {
        ThesaurusLinkConfig config = new ThesaurusLinkConfig();
        config.setApiConfig(createApiConfig());
        config.setLinkProperty(SKOS.EXACT_MATCH.stringValue());
        return config;
    }

    private PoolPartyApiConfig createApiConfig() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setServer("http://test-pp-linux.semantic-web.at/");
        config.setUriSupplement("test_unifiedviews");
        return config;
    }

}
