package eu.unifiedviews.plugins.swc.poolparty.load;

import eu.unifiedviews.plugins.swc.poolparty.util.TestPPLinuxApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.util.TestRdfDataUnit;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

import java.util.Arrays;

public class ThesaurusImportLoaderTest {

    @Test
    public void importIntoDefaultGraph() throws Exception {
        ThesaurusImportLoader loader = new ThesaurusImportLoader();
        Statement st = new StatementImpl(new URIImpl("http://example.org/concept1"), RDF.TYPE, SKOS.CONCEPT);
        loader.inputDataUnit = new TestRdfDataUnit(Arrays.asList(st));
        loader.configureDirectly(createLoaderConfig());

        loader.execute(null);
    }

    private ThesaurusImportConfig createLoaderConfig() {
        ThesaurusImportConfig config = new ThesaurusImportConfig();
        config.setApiConfig(new TestPPLinuxApiConfig());
        return config;
    }

    @Test
    public void importIntoNamedGraph() {
        Assert.fail();
    }

}
