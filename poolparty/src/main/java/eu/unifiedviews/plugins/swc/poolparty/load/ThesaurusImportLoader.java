package eu.unifiedviews.plugins.swc.poolparty.load;

import eu.unifiedviews.dataunit.DataUnit;
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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.RDFHandlerWrapper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private void importRdf() throws Exception {
        PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
        URL url = PptApiConnector.getServiceUrl(poolPartyApiConfig.getServer(),
                "PoolParty/api/projects/" +config.getApiConfig().getProjectId()+ "/import");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        poolPartyApiConfig.getAuthentication().visit(con);

        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", RDFFormat.TRIG.getDefaultMIMEType());
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

        RDFHandler handler = Rio.createWriter(RDFFormat.TRIG, out);
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
        ensureOk(con);
    }

    private void ensureOk(HttpURLConnection con) throws IOException, DPUException {
        int resp = con.getResponseCode();
        if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new DPUException("PPT API returned response code: " + resp);
        }
    }

    private void createSnapshot() throws IOException, DPUException {
        PoolPartyApiConfig poolPartyApiConfig = config.getApiConfig();
        URL url = PptApiConnector.getServiceUrl(poolPartyApiConfig.getServer(),
                "PoolParty/api/projects/" +config.getApiConfig().getProjectId()+ "/snapshot");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        poolPartyApiConfig.getAuthentication().visit(con);
        ensureOk(con);
    }

    @Override
    public AbstractConfigDialog<ThesaurusImportConfig> getConfigurationDialog() {
        return new ThesaurusImportDialog();
    }

}
