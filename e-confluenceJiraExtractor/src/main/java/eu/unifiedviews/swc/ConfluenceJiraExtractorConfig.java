package eu.unifiedviews.swc;

import java.util.Arrays;
import java.util.Collection;

public class ConfluenceJiraExtractorConfig {

    private String confluenceApiBaseUri = "https://grips.semantic-web.at/rest/api/",
            jiraApiBaseUri = "http://jira-dev.semantic-web.at:8080/rest/api/latest/";
    private Collection<String> jiraProjectKeys = Arrays.asList("POOL, PPS");
    private String username = "username", password = "password";

    public String getConfluenceApiBaseUri() {
        return confluenceApiBaseUri;
    }

    public void setConfluenceApiBaseUri(String confluenceApiBaseUri) {
        this.confluenceApiBaseUri = confluenceApiBaseUri;
    }

    public String getJiraApiBaseUri() {
        return jiraApiBaseUri;
    }

    public void setJiraApiBaseUri(String jiraApiBaseUri) {
        this.jiraApiBaseUri = jiraApiBaseUri;
    }

    public Collection<String> getJiraProjectKeys() {
        return jiraProjectKeys;
    }

    public void setJiraProjectKeys(Collection<String> jiraProjectKeys) {
        this.jiraProjectKeys = jiraProjectKeys;
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
