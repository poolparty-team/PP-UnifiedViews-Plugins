/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.plugins.swc.poolparty.ppx;

import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;

/**
 *
 * @author Kata
 */
public class PPXConfig {

    private int numberOfConcepts = 10;
    private String text="";
    private String language;
    private PoolPartyApiConfig apiConfig;

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

    public PoolPartyApiConfig getApiConfig() {
        return apiConfig;
    }

    public void setApiConfig(PoolPartyApiConfig apiConfig) {
        this.apiConfig = apiConfig;
    }

}
