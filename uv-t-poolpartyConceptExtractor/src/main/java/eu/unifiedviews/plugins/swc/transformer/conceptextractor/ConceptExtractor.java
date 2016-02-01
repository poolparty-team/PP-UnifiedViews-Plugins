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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
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
    private static final Logger LOG = LoggerFactory.getLogger(ConceptExtractor.class);

    public Set<Statement> graphStatements = null;
    public HashMap<String, String> filenameUriMappings = null;
    public URI graphUri = null;
		
    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsInput(name = "rdfInput", optional = true)
    public RDFDataUnit rdfInput;

    @DataUnit.AsInput(name = "fileInput", optional = true)
    public FilesDataUnit fileInput;

    @DataUnit.AsOutput(name = "rdfOutput")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
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

        ContextUtils.sendShortInfo(ctx, "Prepare for concept extraction");
        String serviceUrl = config.getServiceRequestUrl();
        HttpStateWrapper httpWrapper = createHttpStateWithAuth();

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

        ContextUtils.sendShortInfo(ctx, "All extraction tasks accomplished");
        rdfWrapper.flushBuffer();
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
        } else {
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

        String rdf = requestExtractionService(serviceUrl, httpWrapper, builder, null);
        if (rdf == null) {
            if (text.length() > 200) {
                text = text.substring(0, 200);
            }
            LOG.warn("Extraction for string literal \"" + text + "...\" of subject <" + subject.stringValue() + "> and predicate <" + predicate.stringValue() + "> failed");
            return;
        }
        List<Statement> rdfExtractionResult = new ArrayList<>();
        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
        rdfParser.setRDFHandler(new StatementCollector(rdfExtractionResult));
        try {
            rdfParser.parse(new StringReader(rdf), PPX_NS);
        } catch (Exception e) {
            try {
                rdfParser.parse(new StringReader(requestExtractionService(serviceUrl, httpWrapper, builder, null)), PPX_NS);
            } catch (Exception e2) {
                if (text.length() > 200) {
                    text = text.substring(0, 200);
                }
                LOG.warn("Extraction for string literal \"" + text + "...\" of subject <" + subject.stringValue() + "> and predicate <" + predicate.stringValue() + "> failed");
                LOG.warn(e.getMessage());
                return;
            }
        }

        rdfWrapper.add(rdfExtractionResult);
        rdfWrapper.add(subject, tagPredicate, taggedResource);
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

        String rdf = requestExtractionService(serviceUrl, httpWrapper, builder, file);
        if (rdf == null) {
            LOG.warn("Extraction for file \"" + file.getName() + "\" failed");
            return;
        }
        List<Statement> rdfExtractionResult = new ArrayList<>();
        RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
        rdfParser.setRDFHandler(new StatementCollector(rdfExtractionResult));
        try {
            rdfParser.parse(new StringReader(rdf), PPX_NS);
        } catch (Exception e) {
            try {
                rdfParser.parse(new StringReader(requestExtractionService(serviceUrl, httpWrapper, builder, file)), PPX_NS);
            } catch (Exception e2) {
                LOG.warn("Extraction for file \"" + file.getName() + "\" failed");
                LOG.warn(e.getMessage());
                return;
            }
        }

        rdfWrapper.add(rdfExtractionResult);
    }

    /**
     * Issue an HTTP post request to the concept extraction service for a result in RDF/XML format
     * @param serviceUrl URL of concept extraction service
     * @param builder entity builder with request parameters
     * @param wrapper Wrapped HTTP state used for requests
     * @return extraction result as an RDF/XML document deserialized to string
     * @throws DPUException
     */
    private String requestExtractionService(String serviceUrl, HttpStateWrapper wrapper, MultipartEntityBuilder builder, File file) throws DPUException {
        String triples = null;
        try {
            HttpPost httpPost = new HttpPost(serviceUrl);
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            if (file != null) {
                builder.addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName());
            }
            httpPost.setEntity(builder.build());
            CloseableHttpResponse response = wrapper.client.execute(wrapper.host, httpPost, wrapper.context);

            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                triples = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            }
            response.close();
        } catch (Exception e) {
            LOG.warn("Encountered an exception when requesting remote concept extraction service", e);
            return null;
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


}
