package eu.unifiedviews.plugins.swc.loader.rdfhttploader;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Vaadin configuration dialog for RdfHttpLoader.
 *
 * @author Yang Yuanzhe
 */
public class RdfHttpLoaderVaadinDialog extends AbstractDialog<RdfHttpLoaderConfig_V1> {
    private TextField host;
    private TextField port;
    private TextField sparqlEndpoint;
    private TextField username;
    private PasswordField password;
    private TextArea update;
    private CheckBox ssl;
    private CheckBox authentication;
    private OptionGroup inputType;
    private CheckBox singleGraph;
    private TextField graphUri;

    public RdfHttpLoaderVaadinDialog() {
        super(RdfHttpLoader.class);
    }

    @Override
    public void setConfiguration(RdfHttpLoaderConfig_V1 c) throws DPUConfigException {
        host.setValue(c.getHost());
        sparqlEndpoint.setValue(c.getSparqlEndpoint());
        port.setValue(c.getPort());
        username.setValue(c.getUsername());
        password.setValue(c.getPassword());
        update.setValue(c.getUpdate());
        ssl.setValue(c.isSsl());
        authentication.setValue(c.isAuthentication());
        inputType.setValue(c.getInputType());
        singleGraph.setValue(c.isSingleGraph());
        graphUri.setValue(c.getGraphUri());
    }

    @Override
    public RdfHttpLoaderConfig_V1 getConfiguration() throws DPUConfigException {
        if (!host.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.host"));
        }
        if (!port.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.port"));
        }
        if (!username.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.username"));
        }
        if (!password.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.password"));
        }

        final RdfHttpLoaderConfig_V1 c = new RdfHttpLoaderConfig_V1();
        c.setHost(host.getValue());
        c.setPort(port.getValue());
        c.setUsername(username.getValue());
        c.setPassword(password.getValue());
        c.setSparqlEndpoint(sparqlEndpoint.getValue());
        c.setUpdate(update.getValue());
        c.setSsl(ssl.getValue());
        c.setAuthentication(authentication.getValue());
        c.setInputType(inputType.getValue().toString());
        c.setSingleGraph(singleGraph.getValue());
        c.setGraphUri(graphUri.getValue());

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final GridLayout mainLayout = new GridLayout(2, 8);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        mainLayout.setColumnExpandRatio(0, 0.5f);
        mainLayout.setColumnExpandRatio(1, 0.5f);

        host = new TextField(ctx.tr("ConceptExtractor.dialog.host"));
        host.setWidth("400px");
        host.setRequired(true);
        host.addValidator(new Validator() {
            @Override
            public void validate(Object o) throws InvalidValueException {
                if (o.toString().startsWith("http://") || o.toString().startsWith("https://")) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.host"));
                }
            }
        });
        mainLayout.addComponent(host, 0, 0, 0, 0);

        port = new TextField(ctx.tr("ConceptExtractor.dialog.port"));
        port.setWidth("200px");
        port.setRequired(true);
        port.addValidator(new Validator() {
            @Override
            public void validate(Object o) throws InvalidValueException {
                int port = -1;
                try {
                    port = Integer.parseInt(o.toString());
                } catch (Exception e) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.port"));
                }
                if (port < 0 || port > 65535) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.port"));
                }
            }
        });
        mainLayout.addComponent(port, 1, 0, 1, 0);

        sparqlEndpoint = new TextField(ctx.tr("ConceptExtractor.dialog.sparqlEndpoint"));
        sparqlEndpoint.setWidth("400px");
        sparqlEndpoint.setRequired(true);
        sparqlEndpoint.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.sparqlEndpoint"),
                5, null, false));
        mainLayout.addComponent(sparqlEndpoint, 0, 1, 0, 1);

        ssl = new CheckBox("ssl", false);
        ssl.setRequired(true);
        mainLayout.addComponent(ssl, 1, 1, 1, 1);

        authentication = new CheckBox("auth", false);
        authentication.setRequired(true);
        authentication.setImmediate(true);
        authentication.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                boolean value = (boolean) event.getProperty().getValue();
                username.setEnabled(value);
                password.setEnabled(value);
            }
        });
        mainLayout.addComponent(authentication, 1, 1, 1, 1);

        username = new TextField(ctx.tr("ConceptExtractor.dialog.username"));
        username.setWidth("200px");
        username.setRequired(true);
        username.setEnabled(false);
        mainLayout.addComponent(username, 0, 2, 0, 2);

        password = new PasswordField(ctx.tr("ConceptExtractor.dialog.password"));
        password.setWidth("200px");
        password.setRequired(true);
        password.setEnabled(false);
        mainLayout.addComponent(password, 1, 2, 1, 2);

        inputType = new OptionGroup("Input Type");
        inputType.setMultiSelect(false);
        inputType.setImmediate(true);
        inputType.setNullSelectionAllowed(false);
        inputType.setRequired(true);
        inputType.addItem("RDF");
        inputType.setValue("RDF");
        inputType.addItem("File");
        inputType.setItemEnabled("File", false);
        inputType.addItem("Query");
        inputType.setItemEnabled("Query", false);
        inputType.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue().equals("Query")) {
                    update.setEnabled(true);
                } else {
                    update.setEnabled(false);
                }
            }
        });
        mainLayout.addComponent(inputType, 0, 3, 1, 3);

        singleGraph = new CheckBox("Export to Single Graph", false);
        singleGraph.setRequired(true);
        singleGraph.setImmediate(true);
        singleGraph.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                graphUri.setEnabled((boolean) event.getProperty().getValue());
            }
        });
        mainLayout.addComponent(singleGraph, 0, 4, 0, 4);

        graphUri = new TextField("Graph URI");
        graphUri.setWidth("200px");
        graphUri.setRequired(true);
        graphUri.setEnabled(false);
        mainLayout.addComponent(graphUri, 1, 4, 1, 4);


        update = new TextArea("SPARQL Update Query");
        update.setRequired(true);
        update.setEnabled(false);
        mainLayout.addComponent(update, 0, 5, 1, 7);

        setCompositionRoot(mainLayout);
    }
}
