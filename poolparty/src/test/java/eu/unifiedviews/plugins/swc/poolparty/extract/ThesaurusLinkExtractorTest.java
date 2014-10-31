package eu.unifiedviews.plugins.swc.poolparty.extract;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import org.junit.Test;

import java.net.URLConnection;

public class ThesaurusLinkExtractorTest {

    @Test
    public void extractFromSampleProject() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setServer();
        config.setAuthentication();
        config.setProjectId();
        URLConnection pptConnection = new ThesaurusLinkExtractor().establishPptConnection()

        pptConnection.getInputStream()

    }

}
