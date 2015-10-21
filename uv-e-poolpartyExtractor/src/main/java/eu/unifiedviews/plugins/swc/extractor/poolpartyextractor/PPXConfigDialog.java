/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.plugins.swc.poolparty.ppx;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;
import eu.unifiedviews.plugins.swc.poolparty.PoolPartyApiPanel;

/**
 *
 * @author Kata
 */
public class PPXConfigDialog extends BaseConfigDialog<PPXConfig> {

    private PoolPartyApiPanel apiPanel = new PoolPartyApiPanel();
    private TextField numberOfConcepts = new TextField("Number of concepts");
    private ComboBox language = new ComboBox("Language");
    private TextArea text = new TextArea("Text");

    PPXConfigDialog() {
        super(PPXConfig.class);
        configureFormElements();
        buildMainLayout();
    }

    private void configureFormElements() {
        numberOfConcepts.setRequired(true);
        numberOfConcepts.setWidth("710px");
        numberOfConcepts.addValidator(new AbstractStringValidator("Must be a number.") {
            @Override
            protected boolean isValidValue(String value) {
                try {
                    Integer num = new Integer(value);
                    if (num > 0) {
                        return true;
                    }
                    return false;
                } catch (Exception ex) {
                    setErrorMessage(ex.getMessage());
                    return false;
                }
            }
        });

        language.addItem("en");
        language.addItem("de");
        language.setRequired(true);
        language.setNullSelectionAllowed(false);
        language.setDescription("The language of the extraction.");

        text.setSizeFull();
        text.setRows(20);
        text.setRequired(true);
    }

    private void buildMainLayout() {
        setWidth("100%");
        setHeight("100%");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(apiPanel);
        mainLayout.addComponent(numberOfConcepts);
        mainLayout.addComponent(language);
        mainLayout.addComponent(text);

        setCompositionRoot(mainLayout);
    }

    @Override
    protected void setConfiguration(PPXConfig conf) throws DPUConfigException {
        apiPanel.setFromApiConfig(conf.getApiConfig());
        numberOfConcepts.setValue(Integer.toString(conf.getNumberOfConcepts()));
        language.setValue(conf.getLanguage());
        text.setValue(conf.getText());
    }

    @Override
    protected PPXConfig getConfiguration() throws DPUConfigException {
        PPXConfig ppxConfig = new PPXConfig();
        ppxConfig.setApiConfig(apiPanel.getApiConfig());
        ppxConfig.setNumberOfConcepts(Integer.parseInt(numberOfConcepts.getValue()));
        ppxConfig.setLanguage(language.getValue().toString());
        ppxConfig.setText(text.getValue());
        return ppxConfig;
    }

}
