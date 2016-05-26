package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.turtle.TurtleWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by yyz on 15/10/15.
 */
public class FailedExtractionRetryTest {
    private static ConceptExtractor extractor;
    private static TestEnvironment env;
    private static WritableRDFDataUnit output;
    private static WritableRDFDataUnit input;
    private static WritableRDFDataUnit failedExtractionOutput;
    private static RepositoryConnection connection;
    private static Properties properties;
    private static ConceptExtractorConfig_V1 config;

    @BeforeClass
    public static void before() throws Exception {
        extractor = new ConceptExtractor();
        env = new TestEnvironment();
        input = env.createRdfInput("rdfInput", false);
        output = env.createRdfOutput("rdfOutput", false);
        failedExtractionOutput = env.createRdfOutput("failedExtractionOutput", false);

        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.ttl");

        connection = input.getConnection();
        URI graph = input.addNewDataGraph("test");
        connection.add(inputStream, "", RDFFormat.TURTLE, graph);
        ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
        connection.export(new TurtleWriter(inputBos), graph);

        Assert.assertTrue(connection.size(graph) > 0);

        properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("target.properties"));

        config = new ConceptExtractorConfig_V1();
        config.setHost("four.zero.four");
        config.setPort(properties.getProperty("port"));
        config.setExtractorApi(properties.getProperty("extractorApi"));
        config.setProjectId(properties.getProperty("projectId"));
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));
        config.setLanguage(properties.getProperty("en"));
        List<String> options = new ArrayList<>();
        options.add("useTransitiveBroaderConcepts");
        options.add("useTransitiveBroaderTopConcepts");
        options.add("useRelatedConcepts");
        options.add("filterNestedConcepts");
        options.add("tfidfScoring");
        options.add("useTypes");
        config.setBooleanParams(options);
    }

    @AfterClass
    public static void after() throws Exception {
        if (connection != null) {
            try {
                connection.close();
            } catch (RepositoryException ex) {

            }
        }
        env.release();
    }

    @Test
    public void extractConcepts() throws Exception {
        extractor.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(extractor);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        connection.export(new TurtleWriter(outputStream), RDFHelper.getGraphsURIArray(failedExtractionOutput));
        Assert.assertTrue(outputStream.size() > 0);
        System.out.println(outputStream.toString());
    }
}
