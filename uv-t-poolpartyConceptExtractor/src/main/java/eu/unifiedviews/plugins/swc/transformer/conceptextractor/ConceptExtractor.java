package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import org.apache.commons.codec.Charsets;
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
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main data processing unit class.
 *
 * @author Unknown
 */
@DPU.AsExtractor
public class ConceptExtractor extends AbstractDpu<ConceptExtractorConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(ConceptExtractor.class);

    public RepositoryResult<Statement> graphStatements = null;
		
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
		
    @Override
    protected void innerExecute() throws DPUException {
        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(output, DataUnitUtils.generateSymbolicName(this.getClass()));
            }
        });
        rdfWrapper.setOutput(outputEntry);

        ContextUtils.sendShortInfo(ctx, "Extraction start");
        loadGraphStatements();
        executeConceptExtraction();
        rdfWrapper.flushBuffer();
    }

    private void loadGraphStatements() throws DPUException {
        faultTolerance.execute(input, new FaultTolerance.ConnectionAction() {
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                graphStatements = connection.getStatements(null, null, null, false);
            }
        });
    }

    private void executeConceptExtraction() throws DPUException {
        try {
            if (graphStatements != null && graphStatements.hasNext()) {
                String serviceUrl = config.getServiceRequestUrl();
                HttpStateWrapper httpWrapper = createHttpStateWithAuth();
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("text", ""));
                nvps.add(new BasicNameValuePair("documentUri", ""));
                nvps.add(new BasicNameValuePair("projectId", config.getProjectId()));
                nvps.add(new BasicNameValuePair("language", config.getLanguage()));

                String[] paramPairs = config.getUriSupplement().split("&");
                for (String paramPair : paramPairs) {
                    String[] keyValue = paramPair.split("=");
                    if (keyValue.length == 2) {
                        nvps.add(new BasicNameValuePair(keyValue[0], keyValue[1]));
                    }
                }

                while (graphStatements.hasNext()) {
                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                    extractSingleObject(graphStatements.next(), nvps, serviceUrl, httpWrapper);
                }
            }
        } catch (RepositoryException e) {
            throw new DPUException(e);
        }

    }

    private void extractSingleObject(Statement statement, List<NameValuePair> nvps, String serviceUrl,
                                     HttpStateWrapper httpWrapper) throws DPUException {
        Resource subject = statement.getSubject();
        String predicateLocalName = statement.getPredicate().getLocalName();
        String text = statement.getObject().stringValue();
        URI tagPredicate = new URIImpl("http://schema.semantic-web.at/ppx/taggedResourceFor"
                + predicateLocalName.charAt(0) + predicateLocalName.substring(1));
        URI taggedResource = new URIImpl("http://schema.semantic-web.at/ppx/" + predicateLocalName + "/"
                + UUID.randomUUID().toString() + "#id");
        nvps.set(0, new BasicNameValuePair("text", text));
        nvps.set(1, new BasicNameValuePair("documentUri", taggedResource.toString()));

        String rdf = requestExtractionService(serviceUrl, nvps, httpWrapper);
        List<Statement> rdfExtractionResult = new ArrayList<>();
        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
        rdfParser.setRDFHandler(new StatementCollector(rdfExtractionResult));
        try {
            rdfParser.parse(new StringReader(rdf), "http://schema.semantic-web.at/ppx/");
        } catch (Exception e) {
            throw new DPUException(e);
        }

        rdfWrapper.add(rdfExtractionResult);
        rdfWrapper.add(subject, tagPredicate, taggedResource);
    }

    private String requestExtractionService(String serviceUrl, List<NameValuePair> nvps, HttpStateWrapper wrapper) throws DPUException {
        String triples = null;
        try {
            HttpPost httpPost = new HttpPost(serviceUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            CloseableHttpResponse response = wrapper.client.execute(wrapper.host, httpPost, wrapper.context);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                triples = EntityUtils.toString(response.getEntity(), Charsets.UTF_8);
            }
            response.close();
        } catch (Exception e) {
            throw new DPUException(e);
        }
        return triples;
    }

    private HttpStateWrapper createHttpStateWithAuth() {
        HttpHost host = new HttpHost(config.getServerUrl(), config.getServerPort(), "http");
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
