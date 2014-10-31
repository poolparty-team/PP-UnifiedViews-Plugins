package eu.unifiedviews.plugins.swc.poolparty;

import eu.unifiedviews.plugins.swc.poolparty.api.AuthenticationFailedException;
import eu.unifiedviews.plugins.swc.poolparty.api.BasicAuthentication;
import eu.unifiedviews.plugins.swc.poolparty.api.PptApiConnector;
import org.junit.Assert;
import org.junit.Test;

import javax.management.ServiceNotFoundException;
import java.io.IOException;

public class PptApiConnectorTest {

    @Test
    public void getProjects_test_pp_linux() throws AuthenticationFailedException, IOException, ServiceNotFoundException
    {
        PptApiConnector pptApiConnector = new PptApiConnector(
            "http://test-pp-linux.semantic-web.at/",
            new BasicAuthentication("TestSuperAdmin", "testuspp"));

        Assert.assertFalse(pptApiConnector.getProjects().isEmpty());
    }

}
