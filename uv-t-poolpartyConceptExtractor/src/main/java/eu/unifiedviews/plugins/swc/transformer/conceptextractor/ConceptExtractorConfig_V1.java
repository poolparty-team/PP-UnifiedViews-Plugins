package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

/**
 * Configuration class for ConceptExtractor.
 *
 * @author Yang Yuanzhe
 */
public class ConceptExtractorConfig_V1 {
    private String server = "";
    private String projectId = "";
    private String uriSupplement = "";
    private String language = "";

    public ConceptExtractorConfig_V1() {

    }

    public String getUriSupplement() {
        return uriSupplement;
    }

    public void setUriSupplement(String uriSupplement) {
        this.uriSupplement = uriSupplement;
    }

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
