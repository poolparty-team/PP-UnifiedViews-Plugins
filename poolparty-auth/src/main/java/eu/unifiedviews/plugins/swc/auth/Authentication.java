package eu.unifiedviews.plugins.swc.auth;

import java.net.URLConnection;

/**
 *
 * @author kreisera
 */
public interface Authentication {

    public void visit(URLConnection con);
    public AuthType getType();
}
