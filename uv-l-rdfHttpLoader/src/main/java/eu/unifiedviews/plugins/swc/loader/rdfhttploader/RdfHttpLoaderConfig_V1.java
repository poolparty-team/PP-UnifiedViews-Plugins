package eu.unifiedviews.plugins.swc.loader.rdfhttploader;

/**
 * Configuration class for RdfHttpLoader.
 *
 * @author Yang Yuanzhe
 */
public class RdfHttpLoaderConfig_V1 {
    private String host = "localhost";
    private String port = "80";
    private String sparqlEndpoint = "/sparql";
    private String username = "";
    private String password = "";
    private String update = "";
    private boolean ssl = false;
    private boolean authentication = false;
    private boolean setGraph = false;
    private String graphUri = "DEFAULT";
    private String inputType = "RDF";
    private String contentType = "Turtle";
    private boolean overwritten = false;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
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

    public String getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    public void setSparqlEndpoint(String sparqlEndpoint) {
        this.sparqlEndpoint = sparqlEndpoint;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public boolean isAuthentication() {
        return authentication;
    }

    public void setAuthentication(boolean authentication) {
        this.authentication = authentication;
    }

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public boolean isSetGraph() {
        return setGraph;
    }

    public void setSetGraph(boolean setGraph) {
        this.setGraph = setGraph;
    }

    public String getGraphUri() {
        return graphUri;
    }

    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isOverwritten() {
        return overwritten;
    }

    public void setOverwritten(boolean overwritten) {
        this.overwritten = overwritten;
    }
}
