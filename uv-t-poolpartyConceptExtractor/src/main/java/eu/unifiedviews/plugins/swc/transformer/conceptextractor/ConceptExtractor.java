package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesHelper;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.openrdf.model.*;
import org.openrdf.model.impl.*;
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

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private static final URI PPX_RESPONSE_CODE = new URIImpl("http://schema.semantic-web.at/ppx/responseCode");
    private static final URI PPX_MESSAGE = new URIImpl("http://schema.semantic-web.at/ppx/message");
    private static final URI PPX_FILE_NAME = new URIImpl("http://schema.semantic-web.at/ppx/fileName");
    private static final Logger LOG = LoggerFactory.getLogger(ConceptExtractor.class);

    public Set<Statement> graphStatements = null;
    public Map<String, String> filenameUriMappings = null;
    public URI graphUri = null;
    public Set<Statement> failedExtractionResourceStatements = null;
    public Set<Statement> failedExtractionReasonStatements = null;
    public Map<String, File> failedExtractionFiles = null;
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsInput(name = "rdfInput", optional = true)
    public RDFDataUnit rdfInput;

    @DataUnit.AsInput(name = "fileInput", optional = true)
    public FilesDataUnit fileInput;

    @DataUnit.AsOutput(name = "rdfOutput")
    public WritableRDFDataUnit rdfOutput;

    @DataUnit.AsOutput(name = "failedExtractionOutput", optional = true)
    public WritableRDFDataUnit failedExtractionOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
    public WritableSimpleRdf rdfWrapper;

    @ExtensionInitializer.Init(param = "failedExtractionOutput")
    public WritableSimpleRdf failedExtractionWrapper;

	public ConceptExtractor() {
		super(ConceptExtractorVaadinDialog.class, ConfigHistory.noHistory(ConceptExtractorConfig_V1.class));
	}

    /**
     * DPU execution entrypoint
     * @throws DPUException
     */
    @Override
    protected void innerExecute() throws DPUException {
        if (fileInput == null && rdfInput == null) {
            throw new DPUException("Input data is not provided for this DPU");
        }

        final RDFDataUnit.Entry outputEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(rdfOutput, DataUnitUtils.generateSymbolicName(this.getClass()));
            }
        });
        rdfWrapper.setOutput(outputEntry);
        final RDFDataUnit.Entry failedExtractionEntry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(failedExtractionOutput, DataUnitUtils.generateSymbolicName(this.getClass()));
            }
        });
        failedExtractionWrapper.setOutput(failedExtractionEntry);

        ContextUtils.sendShortInfo(ctx, "Prepare for concept extraction");
        String serviceUrl = config.getExtractionServiceUrl();
        HttpStateWrapper httpWrapper = createHttpStateWithAuth();

        ContextUtils.sendShortInfo(ctx, "Check extraction model status");
        requestExtractionModelUpdateService(httpWrapper);

        failedExtractionResourceStatements = new HashSet<>();
        failedExtractionReasonStatements = new HashSet<>();
        failedExtractionFiles = new HashMap<>();

        if (fileInput == null && rdfInput != null) {
            final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance, rdfInput, RDFDataUnit.Entry.class);
            for (RDFDataUnit.Entry entry : entries) {
                graphUri = FaultToleranceUtils.asGraph(faultTolerance, entry);
                ContextUtils.sendShortInfo(ctx, "Start loading statements from graph " + graphUri.toString());
                loadGraphStatements(graphUri);
                ContextUtils.sendShortInfo(ctx, "Finish loading");
                ContextUtils.sendShortInfo(ctx, "Start extracting objects from graph " + graphUri.toString());
                executeConceptExtraction(serviceUrl, httpWrapper);
                ContextUtils.sendShortInfo(ctx, "Finish extraction");
            }
        } else {
            if (rdfInput != null) {
                final List<RDFDataUnit.Entry> entries = FaultToleranceUtils.getEntries(faultTolerance, rdfInput, RDFDataUnit.Entry.class);
                if (entries.size() > 1) {
                    throw new DPUException("Concept extraction on files must be provided with one file input and no more " +
                            "than one RDF input denoting file URIs");
                } else if (entries.size() == 1) {
                    graphUri = FaultToleranceUtils.asGraph(faultTolerance, entries.get(0));
                    ContextUtils.sendShortInfo(ctx, "Start loading filenames from graph " + graphUri.toString());
                    loadFilenameUriMappings(graphUri);
                    ContextUtils.sendShortInfo(ctx, "Finish loading");
                }
            }
            ContextUtils.sendShortInfo(ctx, "Start extracting files");
            executeConceptExtraction(serviceUrl, httpWrapper);
            ContextUtils.sendShortInfo(ctx, "Finish extraction");
        }

        int retriedTimes = 0;
        int numberOfFailedExtractions = 0;
        while (retriedTimes < config.getMaxRetry() && !failedExtractionResourceStatements.isEmpty()
                && failedExtractionResourceStatements.size() != numberOfFailedExtractions) {
            numberOfFailedExtractions = failedExtractionResourceStatements.size();
            retriedTimes++;
            ContextUtils.sendShortInfo(ctx, "Retry failed extractions, Iteration " + retriedTimes);
            retryFailedExtractions(serviceUrl, httpWrapper);
        }

        ContextUtils.sendShortInfo(ctx, "All extraction tasks accomplished, failed extractions: "
                + numberOfFailedExtractions);

        rdfWrapper.flushBuffer();
        failedExtractionWrapper.add(new ArrayList<>(failedExtractionResourceStatements));
        failedExtractionWrapper.add(new ArrayList<>(failedExtractionReasonStatements));
        failedExtractionWrapper.flushBuffer();
    }

    /**
     * Retry failed extractions to tolerate network failures
     * @param serviceUrl URL of concept extraction service
     * @param httpWrapper wrapped HTTP state used for requests
     */
    private void retryFailedExtractions(String serviceUrl, HttpStateWrapper httpWrapper) throws DPUException {
        failedExtractionReasonStatements.clear();
        for (Statement s : failedExtractionResourceStatements) {
            if (s.getPredicate().equals(PPX_FILE_NAME) && failedExtractionFiles.containsKey(s.getObject().stringValue())) {
                extractSingleFile(failedExtractionFiles.get(s.getObject().stringValue()), s.getSubject().stringValue(),
                        serviceUrl, httpWrapper);
            } else {
                extractSingleObject(s, serviceUrl, httpWrapper);
            }
        }
    }

    /**
     * Read all the statements in the given graph
     * @param graphUri URI of the graph
     * @throws DPUException
     */
    private void loadGraphStatements(final URI graphUri) throws DPUException {
        graphStatements = new HashSet<>();
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {
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
     * Read filename to URI mappings from the given graph
     * @param graphUri URI of the graph
     * @throws DPUException
     */
    private void loadFilenameUriMappings(final URI graphUri) throws DPUException {
        filenameUriMappings = new HashMap<>();
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                RepositoryResult<Statement> repositoryResult = connection.getStatements(null, null, null, false, graphUri);
                filenameUriMappings.clear();
                while (repositoryResult.hasNext()) {
                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                    Statement filenameStmt = repositoryResult.next();
                    filenameUriMappings.put(filenameStmt.getObject().stringValue(), filenameStmt.getSubject().stringValue());
                }

            }
        });
    }

    /**
     * Execute concept extraction for files or string literals in the RDF statements
     * @throws DPUException
     */
    private void executeConceptExtraction(String serviceUrl, HttpStateWrapper httpWrapper) throws DPUException {
        if (graphStatements != null && !graphStatements.isEmpty()) {
            int graphSize = graphStatements.size();
            int blockSize = graphSize/10 > 0 ? graphSize/10 : 1;
            int reportIndex = blockSize;
            int index = 0;
            for (Statement statement : graphStatements) {
                if (ctx.canceled()) {
                    throw ContextUtils.dpuExceptionCancelled(ctx);
                }
                extractSingleObject(statement, serviceUrl, httpWrapper);
                index++;
                if (index >= reportIndex) {
                    reportIndex += blockSize;
                    ContextUtils.sendShortInfo(ctx, "Extracted " + index + " of " + graphSize + " texts");
                }
            }
        } else if (fileInput != null) {
            final List<FilesDataUnit.Entry> fileEntries = FaultToleranceUtils.getEntries(faultTolerance, fileInput, FilesDataUnit.Entry.class);
            int fileSize = fileEntries.size();
            int blockSize = fileSize/10 > 0 ? fileSize/10 : 1;
            int reportIndex = blockSize;
            int index = 0;
            if (filenameUriMappings != null && !filenameUriMappings.isEmpty()) {
                for (FilesDataUnit.Entry entry : fileEntries) {
                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                    try {
                        String filename = entry.getSymbolicName();
                        String uri = filenameUriMappings.get(filename);
                        if (uri == null) {
                            LOG.warn("Unable to find URI for file: " + filename);
                            continue;
                        }
                        extractSingleFile(FilesHelper.asFile(entry), uri, serviceUrl, httpWrapper);
                        index++;
                        if (index >= reportIndex) {
                            reportIndex += blockSize;
                            ContextUtils.sendShortInfo(ctx, "Extracted " + index + " of " + fileSize + " files");
                        }
                    } catch (DataUnitException e) {
                        LOG.warn("Unable to read the file from files data unit entry", e);
                    }
                }
            } else {
                for (FilesDataUnit.Entry entry : fileEntries) {
                    if (ctx.canceled()) {
                        throw ContextUtils.dpuExceptionCancelled(ctx);
                    }
                    try {
                        extractSingleFile(FilesHelper.asFile(entry),
                                PPX_NS + "document/" + URLEncoder.encode(entry.getSymbolicName(), "UTF-8"),
                                serviceUrl, httpWrapper);
                        index++;
                        if (index >= reportIndex) {
                            reportIndex += blockSize;
                            ContextUtils.sendShortInfo(ctx, "Extracted " + index + " of " + fileSize + " files");
                        }
                    } catch (DataUnitException | UnsupportedEncodingException e) {
                        LOG.warn("Unable to read the file from files data unit entry", e);
                    }
                }
            }
        } else {
            ContextUtils.sendShortInfo(ctx, "Nothing to do because no input data is found");
        }
    }

    /**
     * Extract concepts from the object of the given RDF statement and write result to output
     * @param statement an RDF statement of which the object should be extracted
     * @param serviceUrl URL of concept extraction service
     * @param httpWrapper wrapped HTTP state used for requests
     * @throws DPUException
     */
    private void extractSingleObject(Statement statement, String serviceUrl,
                                     HttpStateWrapper httpWrapper) throws DPUException {
        MultipartEntityBuilder builder = createMultipartEntityBuilder();
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
        URI predicate = statement.getPredicate();
        char c[] = predicate.getLocalName().toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        String predicateLocalName = new String(c);

        URI tagPredicate = new URIImpl(PPX_NS + predicateLocalName + "IsTaggedBy");
        URI taggedResource = new URIImpl(PPX_NS + predicateLocalName + "/"
                + UUID.randomUUID().toString());
        builder.addTextBody("text", text);
        builder.addTextBody("documentUri", taggedResource.toString());

        ExtractionResultWrapper extraction = requestExtractionService(serviceUrl, httpWrapper, builder, null);
        if (extraction.rdf == null) {
            failedExtractionResourceStatements.add(new StatementImpl(subject, predicate, new LiteralImpl(text)));
            failedExtractionReasonStatements.add(new StatementImpl(subject, tagPredicate, taggedResource));
            failedExtractionReasonStatements.add(new StatementImpl(taggedResource, PPX_RESPONSE_CODE, new NumericLiteralImpl(extraction.responseCode)));
            failedExtractionReasonStatements.add(new StatementImpl(taggedResource, PPX_MESSAGE, new LiteralImpl(extraction.responseMessage)));
        } else {
            failedExtractionResourceStatements.remove(new StatementImpl(subject, predicate, new LiteralImpl(text)));
            rdfWrapper.add(extraction.rdf);
            rdfWrapper.add(subject, tagPredicate, taggedResource);
        }
    }

    /**
     * Extract concepts from the given file and write result to output
     * @param file the file to be extracted
     * @param uri resource identifier of the given file
     * @param serviceUrl URL of concept extraction service
     * @param httpWrapper wrapped HTTP state used for requests
     * @throws DPUException
     */
    private void extractSingleFile(File file, String uri, String serviceUrl,
                                     HttpStateWrapper httpWrapper) throws DPUException {
        MultipartEntityBuilder builder = createMultipartEntityBuilder();
        builder.addTextBody("documentUri", uri);

        ExtractionResultWrapper extraction = requestExtractionService(serviceUrl, httpWrapper, builder, file);
        URI fileUri = new URIImpl(uri);
        if (extraction.rdf == null) {
            failedExtractionResourceStatements.add(new StatementImpl(fileUri, PPX_FILE_NAME, new LiteralImpl(file.getPath())));
            failedExtractionFiles.put(file.getPath(), file);
            failedExtractionReasonStatements.add(new StatementImpl(fileUri, PPX_RESPONSE_CODE, new NumericLiteralImpl(extraction.responseCode)));
            failedExtractionReasonStatements.add(new StatementImpl(fileUri, PPX_MESSAGE, new LiteralImpl(extraction.responseMessage)));
        } else {
            failedExtractionResourceStatements.remove(new StatementImpl(fileUri, PPX_FILE_NAME, new LiteralImpl(file.getPath())));
            failedExtractionFiles.remove(file.getPath());
            rdfWrapper.add(extraction.rdf);
        }
    }

    /**
     * Issue an HTTP post request to the concept extraction service for a result in RDF/XML format
     * @param serviceUrl URL of concept extraction service
     * @param builder entity builder with request parameters
     * @param wrapper Wrapped HTTP state used for requests
     * @return extraction result as an RDF/XML document deserialized to string
     * @throws DPUException
     */
    private ExtractionResultWrapper requestExtractionService(String serviceUrl, HttpStateWrapper wrapper, MultipartEntityBuilder builder, File file) throws DPUException {
        try {
            HttpPost httpPost = new HttpPost(serviceUrl);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (file != null) {
                builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
            }
            httpPost.setEntity(builder.build());
            CloseableHttpResponse response = wrapper.client.execute(wrapper.host, httpPost, wrapper.context);

            int status = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            response.close();

            if (status == HttpStatus.SC_OK) {
                List<Statement> rdfExtractionResult = new ArrayList<>();
                RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
                rdfParser.setRDFHandler(new StatementCollector(rdfExtractionResult));
                try {
                    rdfParser.parse(new StringReader(responseBody), PPX_NS);
                    return new ExtractionResultWrapper(rdfExtractionResult, status, "");
                } catch (Exception e) {
                    LOG.warn("Encountered an exception when requesting parsing extraction result to RDF", e);
                    return new ExtractionResultWrapper(null, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                return new ExtractionResultWrapper(null, status, responseBody);
            }

        } catch (Exception e) {
            LOG.warn("Encountered an exception when requesting remote concept extraction service", e);
            return new ExtractionResultWrapper(null, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Check and refresh extraction model of the PoolParty project
     * @param wrapper Wrapped HTTP state used for requests
     */
    private void requestExtractionModelUpdateService(HttpStateWrapper wrapper) {
        try {
            String modelStatus = null;
            String requestUrl = config.getExtractionModelServiceUrl() + "/" + config.getProjectId();
            HttpGet httpGet = new HttpGet(requestUrl);
            CloseableHttpResponse response = wrapper.client.execute(wrapper.host, httpGet, wrapper.context);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                modelStatus = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            response.close();

            if (modelStatus != null && modelStatus.contains("\"upToDate\" : true")) {
                LOG.info("Extraction model is up-to-date");
                return;
            } else {
                LOG.info("Start to update extraction model because its status is unknown");
                requestUrl = requestUrl + "/refresh";
                httpGet = new HttpGet(requestUrl);
                response = wrapper.client.execute(wrapper.host, httpGet, wrapper.context);
                status = response.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                    ContextUtils.sendShortInfo(ctx, "Extraction model update failed, extraction result may be outdated");
                    LOG.warn("Extraction model update failed, extraction result may be outdated");
                }
            }
        } catch (Exception e) {
            LOG.warn("Encountered an exception when requesting extraction model update service", e);
        }
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

    /**
     * Create a multipart entity builder with the given request parameters
     * TODO: MultipartEntityBuilder provided by Apache HTTP Client is not mutable or cloneable, therefore the HTTP entity has to be build from scratch every time
     * @return MultipartEntityBuilder
     */
    private MultipartEntityBuilder createMultipartEntityBuilder() {
        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.addTextBody("projectId", config.getProjectId());
        entityBuilder.addTextBody("language", config.getLanguage());
        for (String param : config.getBooleanParams()) {
            entityBuilder.addTextBody(param, "true");
        }
        if (!config.getNumberOfConcepts().equals("")) {
            entityBuilder.addTextBody("numberOfConcepts", config.getNumberOfConcepts());
        }
        if (!config.getNumberOfTerms().equals("")) {
            entityBuilder.addTextBody("numberOfTerms", config.getNumberOfTerms());
        }
        if (!config.getCorpusScoring().equals("")) {
            entityBuilder.addTextBody("corpusScoring", config.getCorpusScoring());
        }
        return entityBuilder;
    }

    /**
     * A class to wrap HTTP response of concept extraction request
     */
    private class ExtractionResultWrapper {
        private List<Statement> rdf;
        private int responseCode;
        private String responseMessage;

        public ExtractionResultWrapper(List<Statement> rdf, int responseCode, String responseMessage) {
            this.rdf = rdf;
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
        }
    }

}
