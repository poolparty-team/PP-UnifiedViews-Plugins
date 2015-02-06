package eu.unifiedviews.plugins.swc.poolparty.ppx;

import org.junit.Assert;
import org.junit.Test;

public class PPXTransformerTest {

    @Test
    public void test() throws Exception{
        PPXTransformer transformer = new PPXTransformer();
        transformer.configureDirectly(createConfig());

        Assert.fail();
    }

    private PPXConfig createConfig() {
        PPXConfig config = new PPXConfig();
        config.setServer("http://localhost:8080/extractor");
        return config;
    }
}
