package eu.unifiedviews.plugins.swc.poolparty;


import eu.unifiedviews.plugins.swc.poolparty.api.Authentication;
import eu.unifiedviews.plugins.swc.poolparty.api.NoAuthentication;

/**
 *
 * @author kreisera
 */
public class PoolPartyApiConfig {

    private String server = "";
    private String projectId = "";
    private String uriSupplement = "";
    private Authentication authentication = new NoAuthentication();

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public String getUriSupplement() {
        return uriSupplement;
    }

    public void setUriSupplement(String uriSupplement) {
        this.uriSupplement = uriSupplement;
    }
}
