package eu.unifiedviews.plugins.swc.poolparty.extract;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ThesaurusLinkDialog extends BaseConfigDialog<ThesaurusLinkConfig> {

    private final Logger logger = LoggerFactory.getLogger(ThesaurusLinkDialog.class);
    private final ComboBox linkProperty = new ComboBox("Linking Property");
    private PoolPartyApiPanel apiPanel = new PoolPartyApiPanel();

    public class LinkingProperty {

        String uri;
        String label;

        public LinkingProperty(String uri, String label) {
            this.uri = uri;
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    ThesaurusLinkDialog() {
        super(ThesaurusLinkConfig.class);
        buildMainLayout();
    }

    private void buildMainLayout() {
        setWidth("100%");
        setHeight("100%");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(apiPanel);
        
        LinkingProperty exactMatch = new LinkingProperty("http://www.w3.org/2004/02/skos/core#exactMatch", "skos:exactMatch");
        linkProperty.setContainerDataSource(new BeanItemContainer<>(LinkingProperty.class, Arrays.asList(
                exactMatch,
                new LinkingProperty("http://www.w3.org/2004/02/skos/core#closeMatch", "skos:closeMatch"),
                new LinkingProperty("http://www.w3.org/2004/02/skos/core#relatedMatch", "skos:relatedMatch"),
                new LinkingProperty("http://www.w3.org/2004/02/skos/core#broadMatch", "skos:broadMatch"),
                new LinkingProperty("http://www.w3.org/2004/02/skos/core#narrowMatch", "skos:narrowMatch"),
                new LinkingProperty("http://www.w3.org/2002/07/owl#sameAs", "owl:sameAs"),
                new LinkingProperty("http://www.w3.org/2000/01/rdf-schema#seeAlso", "rdfs:seeAlso"))));
        linkProperty.setNullSelectionAllowed(false);
        linkProperty.setRequired(true);
        linkProperty.setRequiredError("Linking property is required");
        linkProperty.setItemCaptionPropertyId("label");
        linkProperty.setItemCaptionMode(ComboBox.ITEM_CAPTION_MODE_PROPERTY);
        mainLayout.addComponent(linkProperty);
        setCompositionRoot(mainLayout);
    }

    @Override
    protected void setConfiguration(ThesaurusLinkConfig config) throws DPUConfigException {
        logger.debug("setting configuration: " +config.toString());

        apiPanel.setFromApiConfig(config.getApiConfig());

        for (Object p : linkProperty.getItemIds()) {
            if (((LinkingProperty) p).getUri().equals(config.getLinkProperty())) {
                linkProperty.setValue(p);
            }
        }
    }

    @Override
    protected ThesaurusLinkConfig getConfiguration() throws DPUConfigException {
        ThesaurusLinkConfig thesaurusLinkConfig = new ThesaurusLinkConfig();
        PoolPartyApiConfig apiConfig = apiPanel.getApiConfig();
        thesaurusLinkConfig.setApiConfig(apiConfig);
        thesaurusLinkConfig.setLinkProperty(((LinkingProperty) linkProperty.getValue()).getUri());
        logger.debug("providing configuration: " +thesaurusLinkConfig.toString());
        return thesaurusLinkConfig;

        /*
        try {
            URL url = PptApiConnector.getServiceUrl(apiConfig.getServer(), "PoolParty/sparql/" + apiConfig.getUriSupplement() + "?query=" + URLEncoder.encode("ASK {?x a <http://www.w3.org/2004/02/skos/core#Concept> }", "UTF-8"));
            logger.info(url.toString());
            Authentication authentication = apiConfig.getAuthentication();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            authentication.visit(con);
            if (con.getResponseCode() != 200) {
                Notification.show("Unable to query SPARQL endpoint of project", "", Notification.TYPE_ERROR_MESSAGE);
                throw new RuntimeException("Response code: "+con.getResponseCode());
            }


        } catch (IOException ex) {
            logger.error("Unable to query SPARQL endpoint of project", ex);
        }
        */
    }
}