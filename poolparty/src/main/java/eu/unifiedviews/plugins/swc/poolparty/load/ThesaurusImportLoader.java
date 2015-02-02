package eu.unifiedviews.plugins.swc.poolparty.load;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.api.PptApiConnector;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

@DPU.AsLoader
public class ThesaurusImportLoader extends
        ConfigurableBase<ThesaurusImportConfig> implements
        ConfigDialogProvider<ThesaurusImportConfig>
{

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inputDataUnit;

    public ThesaurusImportLoader() {
        super(ThesaurusImportConfig.class);
    }

    @Override
    public void execute(DPUContext context) throws DPUException, InterruptedException {
        try {
            importRdf();
            createSnapshot();
        }
        catch (Exception e) {
            throw new DPUException("Error loading data", e);
        }
    }

    private void importRdf() throws IOException, DataUnitException, RDFHandlerException, RepositoryException {
        PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
        URL url = PptApiConnector.getServiceUrl(poolPartyApiConfig.getServer(),
                "PoolParty/!/projects/" +config.getApiConfig().getProjectId()+ "/import");

        URLConnection con = url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", RDFFormat.TRIG.getDefaultMIMEType());

        OutputStream os = con.getOutputStream();
        RDFHandler handler = Rio.createWriter(RDFFormat.TRIG, os);
        final String targetGraph = config.getGraph();
        if (targetGraph != null) {
            handler = new RDFHandlerWrapper(handler) {
                @Override
                public void handleStatement(Statement st) throws RDFHandlerException {
                    super.handleStatement(new ContextStatementImpl(st.getSubject(),
                            st.getPredicate(),
                            st.getObject(),
                            new URIImpl(targetGraph)));
                }
            };
        }
        inputDataUnit.getConnection().export(handler);
        os.close();
    }

    private void createSnapshot() throws IOException, DPUException {
        PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
        URL url = PptApiConnector.getServiceUrl(poolPartyApiConfig.getServer(),
                "PoolParty/!/projects/" +config.getApiConfig().getProjectId()+ "/snapshot");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        if (con.getResponseCode() != 200) {
            throw new DPUException("PPT API returned response code: " + con.getResponseCode());
        }
        con.disconnect();
    }

    @Override
    public AbstractConfigDialog<ThesaurusImportConfig> getConfigurationDialog() {
        return new ThesaurusImportDialog();
    }

}
