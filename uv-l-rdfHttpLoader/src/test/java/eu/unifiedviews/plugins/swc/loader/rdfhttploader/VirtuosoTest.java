package eu.unifiedviews.plugins.swc.loader.rdfhttploader;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dpu.test.config.ConfigurationBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.turtle.TurtleWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by yyz on 26/07/16.
 */
public class VirtuosoTest {
    private static RdfHttpLoader loader;
    private static TestEnvironment env;
    private static WritableRDFDataUnit rdfInput;
    private static WritableFilesDataUnit fileInput;
    private static Properties properties;
    private static RepositoryConnection connection;
    private static RdfHttpLoaderConfig_V1 config;

    @BeforeClass
    public static void before() throws Exception {
        loader = new RdfHttpLoader();
        env = new TestEnvironment();

        properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("virtuoso.properties"));

        config = new RdfHttpLoaderConfig_V1();
        config.setHost(properties.getProperty("host"));
        config.setPort(properties.getProperty("port"));
        config.setSparqlEndpoint(properties.getProperty("endpoint"));
        config.setAuthentication(true);
        config.setUsername(properties.getProperty("username"));
        config.setPassword(properties.getProperty("password"));
        config.setSsl(false);
    }

    @AfterClass
    public static void after() throws Exception {
        env.release();
    }

    @Test
    public void loadSmallRdfFile() throws Exception {
        fileInput = env.createFilesInput("fileInput");
        File inputFile = new File(java.net.URI.create(fileInput.addNewFile("test")));
        try (FileOutputStream fout = new FileOutputStream(inputFile)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-small.ttl"), fout);
        }

        config.setSparqlEndpoint("/sparql-graph-crud-auth");
        config.setSetGraph(true);
        config.setGraphUri("http://smallfiletest.org");
        config.setInputType("File");
        config.setContentType("Turtle");
        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);
    }

    @Test
    public void loadAndOverwriteRdfFile() throws Exception {
        fileInput = env.createFilesInput("fileInput");
        File inputFile = new File(java.net.URI.create(fileInput.addNewFile("test")));
        try (FileOutputStream fout = new FileOutputStream(inputFile)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-small.ttl"), fout);
        }

        config.setSparqlEndpoint("/sparql-graph-crud-auth");
        config.setSetGraph(true);
        config.setGraphUri("http://overwrittenfiletest.org");
        config.setInputType("File");
        config.setContentType("Turtle");
        config.setOverwritten(true);

        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);

        fileInput = env.createFilesInput("fileInput");
        inputFile = new File(java.net.URI.create(fileInput.addNewFile("test")));
        try (FileOutputStream fout = new FileOutputStream(inputFile)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-one.ttl"), fout);
        }
        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);
    }

    @Test
    public void loadLargeRdfFile() throws Exception {
        fileInput = env.createFilesInput("fileInput");
        File inputFile = new File(java.net.URI.create(fileInput.addNewFile("test")));
        try (FileOutputStream fout = new FileOutputStream(inputFile)) {
            IOUtils.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("test-large.nt"), fout);
        }

        config.setSparqlEndpoint("/sparql-graph-crud-auth");
        config.setSetGraph(true);
        config.setGraphUri("http://largefiletest.org");
        config.setInputType("File");
        config.setContentType("N-Triples");
        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);
    }

    @Test
    public void loadSmallRdfObject() throws Exception {
        rdfInput = env.createRdfInput("rdfInput", false);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-small.ttl");
        connection = rdfInput.getConnection();
        URI graph = rdfInput.addNewDataGraph("test");
        connection.add(inputStream, "", RDFFormat.TURTLE, graph);
        ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
        connection.export(new TurtleWriter(inputBos), graph);
        Assert.assertTrue(connection.size(graph) > 0);

        config.setSparqlEndpoint("/sparql-auth");
        config.setSetGraph(true);
        config.setGraphUri("http://smallrdftest.org");
        config.setInputType("RDF");
        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);
    }

    @Test
    public void loadLargeRdfObject() throws Exception {
        rdfInput = env.createRdfInput("rdfInput", false);
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-large.nt");
        connection = rdfInput.getConnection();
        URI graph = rdfInput.addNewDataGraph("test");
        connection.add(inputStream, "", RDFFormat.NTRIPLES, graph);
        ByteArrayOutputStream inputBos = new ByteArrayOutputStream();
        connection.export(new TurtleWriter(inputBos), graph);
        Assert.assertTrue(connection.size(graph) > 0);

        config.setSparqlEndpoint("/sparql-auth");
        config.setSetGraph(true);
        config.setGraphUri("http://largerdftest.org");
        config.setInputType("RDF");
        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);
    }

    @Test
    public void runUpdateQuery() throws Exception {
        config.setSetGraph(false);
        config.setInputType("SPARQL Update");
        config.setSparqlEndpoint("/sparql-auth");
        config.setUpdate("drop silent graph <http://smallfiletest.org> ; " +
                "drop silent graph <http://largefiletest.org> ; " +
                "drop silent graph <http://smallrdftest.org> ; " +
                "drop silent graph <http://largerdftest.org> ;" +
                "drop silent graph <http://overwrittenfiletest.org> ");
        loader.configure((new ConfigurationBuilder()).setDpuConfiguration(config).toString());
        env.run(loader);
    }
}
