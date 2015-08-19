package eu.unifiedviews.plugins.swc.poolparty.api;

import java.net.URLConnection;

/**
 *
 * @author kreisera
 */
public class NoAuthentication implements Authentication {

    @Override
    public void visit(URLConnection con) {
    }

    @Override
    public AuthType getType() {
        return AuthType.None;
    }

}
