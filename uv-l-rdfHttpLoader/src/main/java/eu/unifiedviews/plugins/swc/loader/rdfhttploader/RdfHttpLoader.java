package eu.unifiedviews.plugins.swc.loader.rdfhttploader;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Main data processing unit class.
 *
 * @author Yang Yuanzhe
 */
@DPU.AsLoader
public class RdfHttpLoader extends AbstractDpu<RdfHttpLoaderConfig_V1> {
    private static final Logger LOG = LoggerFactory.getLogger(RdfHttpLoader.class);
    private static final String UPDATE_TEMPLATE = "INSERT DATA { %s }";
    private static final String GRAPH_TEMPLATE = "GRAPH <%s> { %s }";
    private static final int MAX_RETRY = 3;

    public Set<Statement> graphStatements = null;
    public URI internalGraphUri = null;
    StringWriter rdfStringWriter = new StringWriter();
    RDFWriter rdfWriter = Rio.createWriter(RDFFormat.NTRIPLES, rdfStringWriter);
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsInput(name = "rdfInput", optional = true)
    public RDFDataUnit rdfInput;

    @DataUnit.AsInput(name = "fileInput", optional = true)
    public FilesDataUnit fileInput;

	public RdfHttpLoader() {
		super(RdfHttpLoaderVaadinDialog.class, ConfigHistory.noHistory(RdfHttpLoaderConfig_V1.class));
	}

    /**
     * DPU execution entrypoint
     * @throws DPUException
     */
    @Override
    protected void innerExecute() throws DPUException {
        HttpStateWrapper httpWrapper = createHttpStateWithAuth();
        ContextUtils.sendShortInfo(ctx, "Prepare for SPARQL update based on " + config.getInputType());
        String update = "";

        if (rdfInput != null && config.getInputType().equals("RDF")) {
            String targetGraphUri = config.getGraphUri();
            if (targetGraphUri.toLowerCase().equals("default")) {
                update = UPDATE_TEMPLATE;
            } else {
                update = String.format(UPDATE_TEMPLATE, String.format(GRAPH_TEMPLATE, targetGraphUri, "%s"));
            }
            final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance, rdfInput, RDFDataUnit.Entry.class);
            for (RDFDataUnit.Entry entry : entries) {
                internalGraphUri = FaultToleranceUtils.asGraph(faultTolerance, entry);
                ContextUtils.sendShortInfo(ctx, "Start parsing statements from graph " + internalGraphUri.toString());
                loadGraphStatements(internalGraphUri);
                ContextUtils.sendShortInfo(ctx, "Finish parsing");
                ContextUtils.sendShortInfo(ctx, "Start inserting graph " + internalGraphUri.toString() + " into graph " + targetGraphUri);
                int trial = 0;
                while (true) {
                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                    try {
                        trial ++;
                        LOG.info("Start Trial " + trial + " in updating the remote store");
                        updateRemoteStore(httpWrapper, new StringEntity(String.format(update, rdfStringWriter.toString()), ContentType.create("application/sparql-update", StandardCharsets.UTF_8)), null);
                        LOG.info("Update committed successfully in Trial " + trial);
                        break;
                    } catch (DPUException e) {
                        if (trial == MAX_RETRY) throw e;
                    }
                }
                ContextUtils.sendShortInfo(ctx, "Finish update");
            }
        }

        if (fileInput != null && config.getInputType().equals("File")) {
            String graphParam = null;
            if (config.isSetGraph()) {
                if (config.getGraphUri().toLowerCase().equals("default")) {
                    graphParam = "?default";
                } else {
                    try {
                        graphParam = "?graph=" + URLEncoder.encode(config.getGraphUri(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        throw new DPUException("Graph URI cannot be encoded as URL", e);
                    }
                }
            }
            final List<FilesDataUnit.Entry> fileEntries = FaultToleranceUtils.getEntries(faultTolerance, fileInput, FilesDataUnit.Entry.class);
            for (FilesDataUnit.Entry entry : fileEntries) {
                try {
                    ContextUtils.sendShortInfo(ctx, "Start posting file " + entry.getSymbolicName() + " to remote store");
                    int trial = 0;
                    while (true) {
                        if (ctx.canceled()) {
                            throw ContextUtils.dpuExceptionCancelled(ctx);
                        }
                        try {
                            trial ++;
                            LOG.info("Start Trial " + trial + " in updating the remote store");
                            updateRemoteStore(httpWrapper, new FileEntity(FilesHelper.asFile(entry), ContentType.create(RDFFormat.valueOf(config.getContentType()).getDefaultMIMEType(), StandardCharsets.UTF_8)), graphParam);
                            LOG.info("Update committed successfully in Trial " + trial);
                            break;
                        } catch (DPUException e) {
                            if (trial == MAX_RETRY) throw e;
                        }
                    }
                    ContextUtils.sendShortInfo(ctx, "Finish update");
                } catch (DataUnitException e) {
                    throw new DPUException("Unable to read the file from files data unit entry", e);
                }
            }
        }

        if (config.getInputType().equals("SPARQL Update")) {
            ContextUtils.sendInfo(ctx, "Start executing update query", "Update:\n-------\n" + config.getUpdate());
            int trial = 0;
            while (true) {
                if (ctx.canceled()) {
                    throw ContextUtils.dpuExceptionCancelled(ctx);
                }
                try {
                    trial ++;
                    LOG.info("Start Trial " + trial + " in updating the remote store");
                    updateRemoteStore(httpWrapper, new StringEntity(config.getUpdate(), ContentType.create("application/sparql-update", StandardCharsets.UTF_8)), null);
                    LOG.info("Update committed successfully in Trial " + trial);
                    break;
                } catch (DPUException e) {
                    if (trial == MAX_RETRY) throw e;
                }
            }
            ContextUtils.sendShortInfo(ctx, "Finish update");
        }
    }

    /**
     * Read all the statements in the given graph
     * @param graphUri URI of the graph
     * @throws DPUException
     */
    private void loadGraphStatements(final URI graphUri) throws DPUException {
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                RepositoryResult<Statement> repositoryResult = connection.getStatements(null, null, null, false, graphUri);
                try {
                    rdfStringWriter.flush();
                    rdfWriter.startRDF();
                    while (repositoryResult.hasNext()) {
                        if (ctx.canceled()) {
                            throw ContextUtils.dpuExceptionCancelled(ctx);
                        }
                        rdfWriter.handleStatement(repositoryResult.next());
                    }
                    rdfWriter.endRDF();
                } catch (RDFHandlerException e) {
                    throw new DPUException("Encountered an exception when parsing RDF statements from graph: " + graphUri.stringValue(), e);
                }
            }
        });
    }

    /**
     * Issue an HTTP post request to the concept extraction service for a result in RDF/XML format
     * @param wrapper Wrapped HTTP state used for requests
     * @return extraction result as an RDF/XML document deserialized to string
     * @throws DPUException
     */
    private boolean updateRemoteStore(HttpStateWrapper wrapper, HttpEntity entity, String graphParam) throws DPUException {
        String requestUrl;
        int status;
        String message;
        CloseableHttpResponse response = null;
        if (graphParam == null) {
            requestUrl = wrapper.host.toURI() + config.getSparqlEndpoint();
        } else {
            requestUrl = wrapper.host.toURI() + config.getSparqlEndpoint() + graphParam;
        }
        HttpPost httpPost = new HttpPost(requestUrl);
        /*
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (file != null) {
            entityBuilder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
        }
        */
        httpPost.setEntity(entity);
        try {
            response = wrapper.client.execute(wrapper.host, httpPost, wrapper.context);
            status = response.getStatusLine().getStatusCode();
            message = EntityUtils.toString(response.getEntity());
            if (message.length() > 1000) {
                message = message.substring(0, 1000) + "\n ...";
            }
        } catch (Exception e) {
            throw new DPUException("Encountered an exception when sending request to the remote SPARQL endpoint", e);
        } finally {
            HttpClientUtils.closeQuietly(response);
        }
        if (status < 300 ) {
            LOG.info("HTTP request succeeded with a response code " + status + ", please check next log for possible server response");
            LOG.info("The remote store returned a message: " + message);
            return true;
        } else {
            LOG.error("HTTP request failed with a response code " + status + ", please check next log for possible server response");
            LOG.error("The remote store returned a message: " + message);
            throw new DPUException("HTTP request failed with a response code " + status + ", please check next log for possible server response");
        }
    }

    /**
     * Create an HTTP state after authentication with credentials for future requests
     * @return a class wrapping HTTP host, client and context used for future requests
     */
    private HttpStateWrapper createHttpStateWithAuth() {
        CloseableHttpClient httpclient = null;
        String scheme = "http";
        if (config.isSsl()) {
            scheme = "https";
        }
        HttpHost host = new HttpHost(config.getHost(), Integer.parseInt(config.getPort()), scheme);

        if (config.isAuthentication()) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(host.getHostName(), host.getPort()),
                    new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
            httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        }

        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(host, basicAuth);
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setAuthCache(authCache);

        return new HttpStateWrapper(host, httpclient, localContext);
    }

    /**
     * A class to wrap HTTP states after authentication
     */
    private class HttpStateWrapper {
        private HttpHost host;
        private CloseableHttpClient client;
        private HttpClientContext context;

        public HttpStateWrapper(HttpHost host, CloseableHttpClient client, HttpClientContext context) {
            this.host = host;
            this.client = client;
            this.context = context;
        }
    }


}
