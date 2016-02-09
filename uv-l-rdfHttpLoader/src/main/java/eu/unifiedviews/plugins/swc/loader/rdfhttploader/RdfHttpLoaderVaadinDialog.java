package eu.unifiedviews.plugins.swc.loader.rdfhttploader;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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
            throw new DPUConfigException(ctx.tr("RdfHttpLoader.dialog.error.host"));
        }
        if (!port.isValid()) {
            throw new DPUConfigException(ctx.tr("RdfHttpLoader.dialog.error.port"));
        }
        if (!username.isValid()) {
            throw new DPUConfigException(ctx.tr("RdfHttpLoader.dialog.error.username"));
        }
        if (!password.isValid()) {
            throw new DPUConfigException(ctx.tr("RdfHttpLoader.dialog.error.password"));
        }
        if (!graphUri.isValid()) {
            throw new DPUConfigException(ctx.tr("regex"));
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
        final GridLayout mainLayout = new GridLayout(4, 8);
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        //mainLayout.setColumnExpandRatio(1, 0.5f);

        host = new TextField(ctx.tr("RdfHttpLoader.dialog.host"));
        host.setRequired(true);
        host.setWidth("100%");
        host.addValidator(new Validator() {
            @Override
            public void validate(Object o) throws InvalidValueException {
                if (o.toString().startsWith("http://") || o.toString().startsWith("https://")) {
                    throw new InvalidValueException(ctx.tr("RdfHttpLoader.dialog.error.host"));
                }
            }
        });
        mainLayout.addComponent(host, 0, 0, 1, 0);

        port = new TextField(ctx.tr("RdfHttpLoader.dialog.port"));
        port.setRequired(true);
        port.addValidator(new Validator() {
            @Override
            public void validate(Object o) throws InvalidValueException {
                int port = -1;
                try {
                    port = Integer.parseInt(o.toString());
                } catch (Exception e) {
                    throw new InvalidValueException(ctx.tr("RdfHttpLoader.dialog.error.port"));
                }
                if (port < 0 || port > 65535) {
                    throw new InvalidValueException(ctx.tr("RdfHttpLoader.dialog.error.port"));
                }
            }
        });
        mainLayout.addComponent(port, 3, 0, 3, 0);

        sparqlEndpoint = new TextField(ctx.tr("RdfHttpLoader.dialog.sparqlEndpoint"));
        sparqlEndpoint.setWidth("100%");
        sparqlEndpoint.setRequired(true);
        sparqlEndpoint.addValidator(new StringLengthValidator(ctx.tr("RdfHttpLoader.dialog.error.sparqlEndpoint"),
                5, null, false));
        mainLayout.addComponent(sparqlEndpoint, 2, 0, 2, 0);

        ssl = new CheckBox("TLS/SSL (Trust All)", false);
        ssl.setEnabled(false);
        mainLayout.addComponent(ssl, 0, 1, 0, 1);

        authentication = new CheckBox("Basic Authentication", false);
        authentication.setImmediate(true);
        authentication.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                boolean value = (boolean) event.getProperty().getValue();
                username.setEnabled(value);
                username.setRequired(value);
                password.setEnabled(value);
                password.setRequired(value);
            }
        });
        mainLayout.addComponent(authentication, 1, 1, 1, 1);

        username = new TextField(ctx.tr("RdfHttpLoader.dialog.username"));
        username.setRequired(false);
        username.setEnabled(false);
        mainLayout.addComponent(username, 2, 1, 2, 1);

        password = new PasswordField(ctx.tr("RdfHttpLoader.dialog.password"));
        password.setRequired(false);
        password.setEnabled(false);
        mainLayout.addComponent(password, 3, 1, 3, 1);

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
        //inputType.setItemEnabled("Query", false);
        inputType.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue().equals("Query")) {
                    update.setEnabled(true);
                    update.setRequired(true);
                } else {
                    update.setEnabled(false);
                    update.setRequired(false);
                }
            }
        });
        mainLayout.addComponent(inputType, 0, 2, 1, 3);

        singleGraph = new CheckBox("Specify Target Graph", false);
        singleGraph.setImmediate(true);
        singleGraph.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                graphUri.setEnabled((boolean) event.getProperty().getValue());
                graphUri.setRequired((boolean) event.getProperty().getValue());
            }
        });
        mainLayout.addComponent(singleGraph, 2, 2, 2, 2);

        graphUri = new TextField("Graph URI");
        graphUri.setRequired(false);
        graphUri.setEnabled(false);
        graphUri.setWidth("100%");
        graphUri.addValidator(new RegexpValidator("(?i)^[a-zA-Z][a-zA-Z0-9\\+\\._]*:|^default$", false, "regex"));
        mainLayout.addComponent(graphUri, 2, 3, 3, 3);


        update = new TextArea("SPARQL Update Query");
        update.setRequired(false);
        update.setEnabled(false);
        update.setWidth("100%");
        update.setHeightUndefined();
        mainLayout.addComponent(update, 0, 4, 3, 7);

        setCompositionRoot(mainLayout);
    }
}
