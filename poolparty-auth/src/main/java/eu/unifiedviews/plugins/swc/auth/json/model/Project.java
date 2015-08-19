package eu.unifiedviews.plugins.swc.poolparty.api.json.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kreisera
 */
public class Project {

    private String id;
    private String uri;
    private String title;
    private String subject = "";
    private List<String> availableLanguages = new ArrayList<String>();
    private String defaultLanguage;
    private String description = "";
    private String uriSupplement = "";

    public Project() {
    }
    
    public Project(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    public void setAvailableLanguages(List<String> availableLanguages) {
        this.availableLanguages = availableLanguages;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUriSupplement() {
        return uriSupplement;
    }

    public void setUriSupplement(String uriSupplement) {
        this.uriSupplement = uriSupplement;
    }
}
