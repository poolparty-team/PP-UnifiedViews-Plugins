package eu.unifiedviews.plugins.swc.poolparty.ppx;

import eu.unifiedviews.plugins.swc.poolparty.util.ConfigProvider;
import eu.unifiedviews.plugins.swc.poolparty.util.TestWritableRdfUnit;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.repository.RepositoryConnection;

public class PPXExtractorTest {

    @Test
    public void test() throws Exception {
        PPXExtractor transformer = new PPXExtractor();
        PPXConfig config = new ConfigProvider().createTestPPLinuxPPXConfig();
        config.setText("blabla Reichstag bli bla blu");
        transformer.configureDirectly(config);

        transformer.rdfOutput = new TestWritableRdfUnit();

        RepositoryConnection repCon = transformer.rdfOutput.getConnection();
        transformer.execute(null);

        Assert.assertTrue(repCon.isEmpty());
    }

}
