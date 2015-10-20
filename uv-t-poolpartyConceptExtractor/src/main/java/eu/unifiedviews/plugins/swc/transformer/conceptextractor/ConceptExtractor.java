package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.*;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Main data processing unit class.
 *
 * @author Yang Yuanzhe
 */
@DPU.AsTransformer
public class ConceptExtractor extends AbstractDpu<ConceptExtractorConfig_V1> {
    private static final String PPX_NS = "http://schema.semantic-web.at/ppx/";
    private static final Logger LOG = LoggerFactory.getLogger(ConceptExtractor.class);

    public Set<Statement> graphStatements = null;
    public URI graphUri = null;
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit input;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit output;

    @ExtensionInitializer.Init(param = "output")
    public WritableSimpleRdf rdfWrapper;

	public ConceptExtractor() {
		super(ConceptExtractorVaadinDialog.class, ConfigHistory.noHistory(ConceptExtractorConfig_V1.class));
	}

    /**
     * DPU execution entrypoint
     * @throws DPUException
     */
    @Override
    protected void innerExecute() throws DPUException {
        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(output, DataUnitUtils.generateSymbolicName(this.getClass()));
            }
        });
        rdfWrapper.setOutput(outputEntry);

        ContextUtils.sendShortInfo(ctx, "Prepare for concept extraction");
        final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance, input, RDFDataUnit.Entry.class);
        for (RDFDataUnit.Entry entry : entries) {
            graphUri = FaultToleranceUtils.asGraph(faultTolerance, entry);
            ContextUtils.sendShortInfo(ctx, "Start loading statements from graph " + graphUri.toString());
            loadGraphStatements(graphUri);
            ContextUtils.sendShortInfo(ctx, "Finish loading");
            ContextUtils.sendShortInfo(ctx, "Start extracting objects from graph " + graphUri.toString());
            executeConceptExtraction();
            ContextUtils.sendShortInfo(ctx, "Finish extraction");
        }
        ContextUtils.sendShortInfo(ctx, "All extractions finished");
        rdfWrapper.flushBuffer();
    }

    /**
     * Read all the statements in the given graph
     * @param graphUri URI of the graph
     * @throws DPUException
     */
    private void loadGraphStatements(final URI graphUri) throws DPUException {
        graphStatements = new HashSet<>();
        faultTolerance.execute(input, new FaultTolerance.ConnectionAction() {
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                RepositoryResult<Statement> repositoryResult = connection.getStatements(null, null, null, false, graphUri);
                graphStatements.clear();
                while (repositoryResult.hasNext()) {
                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                    graphStatements.add(repositoryResult.next());
                }

            }
        });
    }

    /**
     * Execute concept extraction for objects of all statements in the current graph
     * @throws DPUException
     */
    private void executeConceptExtraction() throws DPUException {
        if (graphStatements != null && !graphStatements.isEmpty()) {
            String serviceUrl = config.getServiceRequestUrl();
            HttpStateWrapper httpWrapper = createHttpStateWithAuth();
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("text", ""));
            nvps.add(new BasicNameValuePair("documentUri", ""));
            nvps.add(new BasicNameValuePair("projectId", config.getProjectId()));
            nvps.add(new BasicNameValuePair("language", config.getLanguage()));
            for (String param : config.getBooleanParams()) {
                nvps.add(new BasicNameValuePair(param, "true"));
            }
            if (!config.getNumberOfConcepts().equals("")) {
                nvps.add(new BasicNameValuePair("numberOfConcepts", config.getNumberOfConcepts()));
            }
            if (!config.getNumberOfTerms().equals("")) {
                nvps.add(new BasicNameValuePair("numberOfTerms", config.getNumberOfTerms()));
            }
            if (!config.getCorpusScoring().equals("")) {
                nvps.add(new BasicNameValuePair("corpusScoring", config.getCorpusScoring()));
            }

            for (NameValuePair nvp : nvps) {
                LOG.info("Extraction parameters: " + nvp.toString());
            }

            for (Statement statement : graphStatements) {
                if (ctx.canceled()) {
                    throw ContextUtils.dpuExceptionCancelled(ctx);
                }
                extractSingleObject(statement, nvps, serviceUrl, httpWrapper);
            }
        }

    }

    /**
     * Extract concepts from the object of the given RDF statement and write result to output
     * @param statement an RDF statement of which the object should be extracted
     * @param nvps request parameters used in the HTTP requests
     * @param serviceUrl URL of concept extraction service
     * @param httpWrapper wrapped HTTP state used for requests
     * @throws DPUException
     */
    private void extractSingleObject(Statement statement, List<NameValuePair> nvps, String serviceUrl,
                                     HttpStateWrapper httpWrapper) throws DPUException {
        Value object = statement.getObject();
        if (object instanceof Literal) {
            if (!(((Literal) object).getDatatype().getLocalName().equals("string"))) {
                return;
            }
        } else {
            return;
        }
        String text = object.stringValue();

        Resource subject = statement.getSubject();
        String predicateLocalName = statement.getPredicate().getLocalName();
        URI tagPredicate = new URIImpl(PPX_NS + predicateLocalName + "IsTaggedBy");
        URI taggedResource = new URIImpl(PPX_NS + predicateLocalName + "/"
                + UUID.randomUUID().toString() + "#id");
        nvps.set(0, new BasicNameValuePair("text", text));
        nvps.set(1, new BasicNameValuePair("documentUri", taggedResource.toString()));

        String rdf = requestExtractionService(serviceUrl, nvps, httpWrapper);
        if (rdf == null) {
            return;
        }
        List<Statement> rdfExtractionResult = new ArrayList<>();
        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
        rdfParser.setRDFHandler(new StatementCollector(rdfExtractionResult));
        try {
            rdfParser.parse(new StringReader(rdf), PPX_NS);
        } catch (Exception e) {
            throw new DPUException(e);
        }

        rdfWrapper.add(rdfExtractionResult);
        rdfWrapper.add(subject, tagPredicate, taggedResource);
    }

    /**
     * Issue an HTTP post request to the concept extraction service for a result in RDF/XML format
     * @param serviceUrl URL of concept extraction service
     * @param nvps Name-value pairs of request parameters
     * @param wrapper Wrapped HTTP state used for requests
     * @return extraction result as an RDF/XML document deserialized to string
     * @throws DPUException
     */
    private String requestExtractionService(String serviceUrl, List<NameValuePair> nvps, HttpStateWrapper wrapper) throws DPUException {
        String triples = null;
        try {
            HttpPost httpPost = new HttpPost(serviceUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = wrapper.client.execute(wrapper.host, httpPost, wrapper.context);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                triples = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            response.close();
        } catch (Exception e) {
            throw new DPUException(e);
        }
        return triples;
    }

    /**
     * Create an HTTP state after authentication with credentials for future requests
     * @return a class wrapping HTTP host, client and context used for future requests
     */
    private HttpStateWrapper createHttpStateWithAuth() {
        HttpHost host = new HttpHost(config.getHost(), Integer.parseInt(config.getPort()), "http");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(host.getHostName(), host.getPort()),
                new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

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
