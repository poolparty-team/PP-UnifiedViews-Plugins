package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for ConceptExtractor.
 *
 * @author Yang Yuanzhe
 */
public class ConceptExtractorConfig_V1 {
    private String host = "";
    private String port = "80";
    private String projectId = "";
    private String language = "en";
    private String extractorApi = "/extractor/api/annotate";
    private String extractionModelApi = "/PoolParty/api/indexbuilder";
    private String username = "";
    private String password = "";
    private String numberOfConcepts = "50";
    private String numberOfTerms = "0";
    private String corpusScoring = "";
    private int maxRetry = 3;
    private List<String> booleanParams = new ArrayList<>();

    public ConceptExtractorConfig_V1() {
        booleanParams.add("filterNestedConcepts");
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

    public String getNumberOfTerms() {
        return numberOfTerms;
    }

    public void setNumberOfTerms(String numberOfTerms) {
        this.numberOfTerms = numberOfTerms;
    }

    public String getNumberOfConcepts() {
        return numberOfConcepts;
    }

    public void setNumberOfConcepts(String numberOfConcepts) {
        this.numberOfConcepts = numberOfConcepts;
    }

    public String getCorpusScoring() {
        return corpusScoring;
    }

    public void setCorpusScoring(String corpusScoring) {
        this.corpusScoring = corpusScoring;
    }

    public List<String> getBooleanParams() {
        return booleanParams;
    }

    public void setBooleanParams(List<String> booleanParams) {
        this.booleanParams = booleanParams;
    }

    public String getExtractionServiceUrl() {
        return "http://" + host + ":" + port + extractorApi;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public void setMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
    }

    public String getExtractionModelServiceUrl() {
        return "http://" + host + ":" + port + extractionModelApi;
    }

}
