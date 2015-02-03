package eu.unifiedviews.plugins.swc.poolparty.util;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.api.BasicAuthentication;

public class ApiConfig {

    public static PoolPartyApiConfig createTestPPLinuxApiConfig() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setServer("http://test-pp-linux.semantic-web.at/");
        config.setUriSupplement("test_unifiedviews");
        config.setProjectId("1DCDE7C4-981B-0001-2E92-12059D5A1C38");
        config.setAuthentication(new BasicAuthentication("unifiedviews", "UnifiedViews"));
        return config;
    }

    public static PoolPartyApiConfig createLocalApiConfig() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setServer("http://localhost:8080/");
        config.setUriSupplement("test_unifiedviews");
        config.setProjectId("1DCDE7BF-C388-0001-C44B-ABE086BB1CF6");
        config.setAuthentication(new BasicAuthentication("unifiedviews", "UnifiedViews123"));
        return config;
    }

}
