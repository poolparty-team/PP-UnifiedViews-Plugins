package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

/**
 * Configuration class for ConceptExtractor.
 *
 * @author Yang Yuanzhe
 */
public class ConceptExtractorConfig_V1 {
    private String host = "";
    private String port = "80";
    private String projectId = "";
    private String uriSupplement = "";
    private String language = "en";
    private String extractorApi = "/extractor/api/annotate";
    private String username = "";
    private String password = "";

    public ConceptExtractorConfig_V1() {

    }

    public String getUriSupplement() {
        return uriSupplement;
    }

    public void setUriSupplement(String uriSupplement) {
        this.uriSupplement = uriSupplement;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public String getExtractorApi() {
        return extractorApi;
    }

    public void setExtractorApi(String extractorApi) {
        this.extractorApi = extractorApi;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getServiceRequestUrl() {
        return "http://" + host + ":" + port + extractorApi;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
