package eu.unifiedviews.plugins.swc.extractor.thesauruslinker;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.plugins.swc.api.PoolPartyApiConfig;
import eu.unifiedviews.plugins.swc.api.PoolPartyApiPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ThesaurusLinkerVaadinDialog extends AbstractDialog<ThesaurusLinkerConfig_V1> {

    private final Logger logger = LoggerFactory.getLogger(ThesaurusLinkerVaadinDialog.class);
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

    ThesaurusLinkerVaadinDialog() {
        super(ThesaurusLinker.class);
    }

    @Override
    protected void buildDialogLayout() {
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
    protected void setConfiguration(ThesaurusLinkerConfig_V1 config) throws DPUConfigException {
        logger.debug("setting configuration: " +config.toString());

        apiPanel.setFromApiConfig(config.getApiConfig());

        for (Object p : linkProperty.getItemIds()) {
            if (((LinkingProperty) p).getUri().equals(config.getLinkProperty())) {
                linkProperty.setValue(p);
            }
        }
    }

    @Override
    protected ThesaurusLinkerConfig_V1 getConfiguration() throws DPUConfigException {
        ThesaurusLinkerConfig_V1 thesaurusLinkerConfigV1 = new ThesaurusLinkerConfig_V1();
        PoolPartyApiConfig apiConfig = apiPanel.getApiConfig();
        thesaurusLinkerConfigV1.setApiConfig(apiConfig);
        thesaurusLinkerConfigV1.setLinkProperty(((LinkingProperty) linkProperty.getValue()).getUri());
        logger.debug("providing configuration: " + thesaurusLinkerConfigV1.toString());
        return thesaurusLinkerConfigV1;
    }
}