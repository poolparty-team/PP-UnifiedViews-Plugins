package eu.unifiedviews.plugins.swc.auth;

import org.apache.commons.codec.binary.Base64;

import java.net.URLConnection;

/**
 *
 * @author kreisera
 */
public class BasicAuthentication extends UsernamePasswordCredentials {

    public BasicAuthentication() {
        super();
    }

    public BasicAuthentication(String username, String password) {
        super(username, password);
    }
    
    @Override
    public void visit(URLConnection con) {
        String credentials = getUsername() + ":" + getPassword();
        String encodedCredentials = Base64.encodeBase64String(credentials.getBytes());
        con.setRequestProperty("Authorization", "Basic " + encodedCredentials);
    }

    @Override
    public AuthType getType() {
        return AuthType.Basic_Auth;
    }
}
