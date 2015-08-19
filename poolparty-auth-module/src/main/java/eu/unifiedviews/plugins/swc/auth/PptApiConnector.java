package eu.unifiedviews.plugins.swc.poolparty.api;

import eu.unifiedviews.plugins.swc.poolparty.api.json.model.Project;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.type.JavaType;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ServiceNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kreisera
 */
public class PptApiConnector {

    private final static Logger logger = LoggerFactory.getLogger(PptApiConnector.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String host;
    private final Authentication authentication;

    public PptApiConnector(String host, Authentication authentication) {
        this.host = host;
        this.authentication = authentication;
    }

    public List<Project> getProjects() throws AuthenticationFailedException, ServiceNotFoundException, IOException {
        HttpURLConnection con = getServiceConnection("PoolParty/api/projects");
        if (con.getResponseCode() == 401) {
            throw new AuthenticationFailedException();
        } else if (con.getResponseCode() == 404) {
            throw new ServiceNotFoundException();
        }
        JavaType type = CollectionType.construct(ArrayList.class, SimpleType.construct(Project.class));
        InputStream in = con.getInputStream();

        try {
            return (List<Project>) objectMapper.readValue(in, type);
        }
        finally {
            in.close();
        }
    }

    public void importRdf(String projectId, RepositoryConnection repCon, Resource sourceGraph, final Resource targetGraph) throws Exception {
        HttpURLConnection con = getServiceConnection("api/thesaurus/" + projectId + "/import");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", RDFFormat.TRIG.getDefaultMIMEType());
        try {
            RDFHandler handler = Rio.createWriter(RDFFormat.TRIG, con.getOutputStream());
            if (targetGraph != null) {
                handler = new RDFHandlerWrapper(handler) {
                    @Override
                    public void handleStatement(Statement st) throws RDFHandlerException {
                        super.handleStatement(new ContextStatementImpl(st.getSubject(), st.getPredicate(), st.getObject(), targetGraph));
                    }
                };
            }
            repCon.export(handler, sourceGraph);
            con.getOutputStream().close();
        } finally {
            con.disconnect();
        }
    }

    public void createSnapshot(String projectId) throws Exception {
        HttpURLConnection con = getServiceConnection("api/thesaurus/" + projectId + "/snapshot");
        try {
            if (con.getResponseCode() != 200) {
                throw new Exception("PPT API returned response code: " + con.getResponseCode());
            }
        } finally {
            con.disconnect();
        }
    }

    private HttpURLConnection getServiceConnection(String path) throws IOException {
        URL url = getServiceUrl(host, path);
        logger.debug("connecting to service url: '" +url.toString()+ "'");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        authentication.visit(con);
        return con;
    }

    public static URL getServiceUrl(String host, String path) {
        if (!host.endsWith("/")) {
            host += "/";
        }
        try {
            return new URL(host + path);
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
