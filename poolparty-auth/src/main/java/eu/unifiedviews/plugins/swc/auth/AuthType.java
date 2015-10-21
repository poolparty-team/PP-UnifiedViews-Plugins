package eu.unifiedviews.plugins.swc.auth;

/**
 *
 * @author kreisera
 */
public enum AuthType {

    None("None"),
    Basic_Auth("Basic Authentication");
    private final String label;

    private AuthType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
