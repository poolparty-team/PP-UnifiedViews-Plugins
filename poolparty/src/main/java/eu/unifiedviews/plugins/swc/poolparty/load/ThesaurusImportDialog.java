package eu.unifiedviews.plugins.swc.poolparty.load;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiPanel;
import org.openrdf.model.impl.URIImpl;

import java.util.Arrays;

/**
 *
 * @author kreisera
 */
public class ThesaurusImportDialog extends BaseConfigDialog<ThesaurusImportConfig> {

    private final ComboBox graph = new ComboBox("Graph");
    private PoolPartyApiPanel apiPanel = new PoolPartyApiPanel();

    ThesaurusImportDialog() {
        super(ThesaurusImportConfig.class);
        buildMainLayout();
    }

    private void buildMainLayout() {
        setWidth("100%");
        setHeight("100%");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(apiPanel);

        graph.addValidator(new AbstractStringValidator(null) {
            @Override
            protected boolean isValidValue(String value) {
                try {
                    new URIImpl(value);
                    return true;
                }
                catch (Exception ex){
                    setErrorMessage("Invalid Graph URI: "+ex.getMessage());
                    return false;
                }
            }
        });
        graph.setContainerDataSource(new BeanItemContainer(String.class, Arrays.asList(
                "http://dbpedia.org", "http://dbpedia.org/categories", "http://de.dbpedia.org",
                "http://de.dbpedia.org/categories", "http://freebase.org", "http://geonames.org", "http://umbel.org/",
                "http://sindice.com/", "http://wordnet.princeton.edu", "http://www.dmoz.org/")));
        mainLayout.addComponent(graph);
    }

    @Override
    protected void setConfiguration(ThesaurusImportConfig config) throws DPUConfigException {
        if (config.getGraph() != null && !config.getGraph().isEmpty())
            graph.setValue(config.getGraph());
    }

    @Override
    protected ThesaurusImportConfig getConfiguration() throws DPUConfigException {
        ThesaurusImportConfig config = new ThesaurusImportConfig();
        config.setApiConfig(apiPanel.getApiConfig());
        config.setGraph((String)graph.getValue());
        return config;
    }
}
