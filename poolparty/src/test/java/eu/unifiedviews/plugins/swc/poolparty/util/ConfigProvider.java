package eu.unifiedviews.plugins.swc.poolparty.util;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.api.BasicAuthentication;
import eu.unifiedviews.plugins.swc.poolparty.ppx.PPXConfig;

public class ConfigProvider {

    private String ppLinuxProjectId = "1DCDF746-5C36-0001-5CB9-18A013776E20";

    public PoolPartyApiConfig createTestPPLinuxApiConfig() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setServer("http://test-pp-linux.semantic-web.at/");
        config.setUriSupplement("test2");
        config.setProjectId(ppLinuxProjectId);
        config.setAuthentication(new BasicAuthentication("unifiedviews", "UnifiedViews"));
        return config;
    }

    public PPXConfig createTestPPLinuxPPXConfig() {
        PPXConfig config = new PPXConfig();
        config.setLanguage("en");
        PoolPartyApiConfig apiConfig = createTestPPLinuxApiConfig();
        apiConfig.setServer(apiConfig.getServer() + "/extractor");
        config.setApiConfig(apiConfig);
        return config;
    }

    public PoolPartyApiConfig createLocalApiConfig() {
        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setServer("http://localhost:8080/");
        config.setUriSupplement("test_unifiedviews");
        config.setProjectId("1DCDE7BF-C388-0001-C44B-ABE086BB1CF6");
        config.setAuthentication(new BasicAuthentication("unifiedviews", "UnifiedViews123"));
        return config;
    }

}
