package eu.unifiedviews.plugins.swc.poolparty.extract;


import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;

/**
 *
 * @author kreisera
 */
public class ThesaurusLinkConfig {

    private PoolPartyApiConfig apiConfig = new PoolPartyApiConfig();
    private String linkProperty = "http://www.w3.org/2004/02/skos/core#exactMatch";

    public PoolPartyApiConfig getApiConfig() {
        return apiConfig;
    }

    public void setApiConfig(PoolPartyApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

    public String getLinkProperty() {
        return linkProperty;
    }

    public void setLinkProperty(String linkProperty) {
        this.linkProperty = linkProperty;
    }

    @Override
    public String toString() {
        return "apiconfig: " +apiConfig.toString()+ ", linkProperty: " +linkProperty;
    }
}
