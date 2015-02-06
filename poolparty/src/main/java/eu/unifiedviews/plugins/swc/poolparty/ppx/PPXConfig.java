/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.plugins.swc.poolparty.ppx;

/**
 *
 * @author Kata
 */
public class PPXConfig {

    private int numberOfConcepts = 10;
    private String text="";
    private String language;
    private String projectId="";
    private String server = "";

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getNumberOfConcepts() {
        return numberOfConcepts;
    }

    public void setNumberOfConcepts(int numberOfConcepts) {
        this.numberOfConcepts = numberOfConcepts;
    }
}
