package eu.unifiedviews.plugins.swc.poolparty.util;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;

public class TestPPLinuxApiConfig extends PoolPartyApiConfig {

    public TestPPLinuxApiConfig() {
        setServer("http://test-pp-linux.semantic-web.at/");
        setUriSupplement("test_unifiedviews");
    }

}
