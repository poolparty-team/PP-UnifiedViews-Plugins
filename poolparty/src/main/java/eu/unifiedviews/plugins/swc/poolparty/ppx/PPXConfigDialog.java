/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.plugins.swc.poolparty.ppx;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.BaseConfigDialog;

/**
 *
 * @author Kata
 */
public class PPXConfigDialog extends BaseConfigDialog<PPXConfig> {

    private FieldGroup fieldGroup;

    PPXConfigDialog() {
        super(PPXConfig.class);
        buildMainLayout();
    }

    private void buildMainLayout() {
        setWidth("100%");
        setHeight("100%");

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.addComponent(new ConfigForm());

        /*
        fieldGroup = new FieldGroup();
        fieldGroup.setFieldFactory(new DefaultFieldGroupFieldFactory() {
            @Override
            public <T extends Field> T createField(Class<?> type, Class<T> fieldType) {
                return super.createField(type, fieldType);
            }

            public Field createField(Item item, Object propertyId, Component uiContext) {
                if (propertyId.equals("numberOfConcepts")) {
                    TextField f = new TextField("Number of concepts");
                    f.setRequired(true);
                    f.setWidth("710px");
                    f.addValidator(new AbstractStringValidator("Must be a number.") {
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
                    return f;
                } else if (propertyId.equals("projectId")) {
                    TextField f = new TextField("Thesaurus project id.");
                    f.setRequired(true);
                    f.setWidth("710px");
                    f.setRequired(true);
                    return f;
                } else if (propertyId.equals("language")) {
                    ComboBox field = new ComboBox("Language");
                    field.addItem("en");
                    field.addItem("de");
                    field.setRequired(true);
                    field.setNullSelectionAllowed(false);
                    field.setDescription("The language of the extraction.");
                    return field;
                } else if (propertyId.equals("server")) {
                    TextField f = new TextField("Server url (eg. http://localhost:8080/extractor).");
                    f.setRequired(true);
                    f.setWidth("710px");
                    f.setRequired(true);
                    return f;
                } else if (propertyId.equals("text")) {
                    TextArea field = new TextArea("Text");
                    field.setSizeFull();
                    field.setRows(20);
                    field.setRequired(true);
                    return field;
                }
                return null;
            }
        });
        form.setItemDataSource(new BeanItem<PPXConfig>(config));
        addComponent(form);
        */

        setCompositionRoot(mainLayout);
    }

    @Override
    protected void setConfiguration(PPXConfig conf) throws DPUConfigException {

    }

    @Override
    protected PPXConfig getConfiguration() throws DPUConfigException {
        return new PPXConfig();
    }

    private class ConfigForm extends CustomComponent {

        TextField conceptCount = new TextField("Number of concepts");

        TextField projectId = new TextField("Thesaurus project id.");

        ComboBox language = new ComboBox("Language");

        TextField server = new TextField("Server url (eg. http://localhost:8080/extractor).");

        TextArea text = new TextArea("Text");

        ConfigForm() {
            FormLayout layout = new FormLayout();
            layout.addComponent(conceptCount);
            layout.addComponent(projectId);
            layout.addComponent(language);
            layout.addComponent(server);
            layout.addComponent(text);

            FieldGroup binder = new BeanFieldGroup<>(PPXConfig.class);
            binder.bind(conceptCount, "numberOfConcepts");

            setCompositionRoot(layout);
        }
    }
}
