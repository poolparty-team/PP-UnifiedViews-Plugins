package eu.unifiedviews.plugins.swc.poolparty.api;

/**
 *
 * @author kreisera
 */
public abstract class UsernamePasswordCredentials implements Authentication {

    private String username;
    private String password;

    public UsernamePasswordCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public UsernamePasswordCredentials() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
