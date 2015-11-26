package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dataunit.rdf.RDFHelper;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import org.apache.commons.io.IOUtils;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by yyz on 26/11/15.
 */
public class FileWithoutUriExtractionTest {
    private static ConceptExtractor extractor;
    private static TestEnvironment env;
    private static WritableRDFDataUnit output;
    private static WritableRDFDataUnit input;
    private static WritableFilesDataUnit fileInput;
    private static RepositoryConnection connection;
    private static Properties properties;
    private static ConceptExtractorConfig_V1 config;

    @BeforeClass
    public static void before() throws Exception {
        extractor = new ConceptExtractor();
        env = new TestEnvironment();

        fileInput = env.createFilesInput("fileInput");
        output = env.createRdfOutput("rdfOutput", false);

        File inputFile = new File(java.net.URI.create(fileInput.addNewFile("file1.txt")));
        try (FileOutputStream fout = new FileOutputStream(inputFile)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("file1.txt"), fout);
        }

        inputFile = new File(java.net.URI.create(fileInput.addNewFile("file2.txt")));
        try (FileOutputStream fout = new FileOutputStream(inputFile)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("file2.txt"), fout);
        }

        properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("target.properties"));

        config = new ConceptExtractorConfig_V1();
        config.setHost(properties.getProperty("host"));
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
        connection = output.getConnection();
        connection.export(new TurtleWriter(outputStream), RDFHelper.getGraphsURIArray(output));
        Assert.assertTrue(outputStream.size() > 0);
        System.out.println(outputStream.toString());
    }
}
