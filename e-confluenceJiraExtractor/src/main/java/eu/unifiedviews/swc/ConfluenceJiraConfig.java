package eu.unifiedviews.swc;

import java.util.Collection;

public class ConfluenceJiraConfig {

    private String confluenceApiBaseUri, jiraApiBaseUri;
    private Collection<String> jiraProjectKeys;
    private String username, password;

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
