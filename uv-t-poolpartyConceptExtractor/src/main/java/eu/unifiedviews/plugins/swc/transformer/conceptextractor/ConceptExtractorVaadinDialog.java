package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for ConceptExtractor.
 *
 * @author Yang Yuanzhe
 */
public class ConceptExtractorVaadinDialog extends AbstractDialog<ConceptExtractorConfig_V1> {
    private TextField host;
    private TextField port;
    private TextField projectId;
    private TextField uriSupplement;
    private TextField language;
    private TextField extractorApi;
    private TextField username;
    private PasswordField password;

    public ConceptExtractorVaadinDialog() {
        super(ConceptExtractor.class);
    }

    @Override
    public void setConfiguration(ConceptExtractorConfig_V1 c) throws DPUConfigException {
        host.setValue(c.getHost());
        port.setValue(c.getPort());
        extractorApi.setValue(c.getExtractorApi());
        uriSupplement.setValue(c.getUriSupplement());
        projectId.setValue(c.getProjectId());
        language.setValue(c.getLanguage());
        username.setValue(c.getUsername());
        password.setValue(c.getPassword());
    }

    @Override
    public ConceptExtractorConfig_V1 getConfiguration() throws DPUConfigException {
        if (!host.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.host"));
        }
        if (!port.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.port"));
        }
        if (!extractorApi.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.extractorApi"));
        }
        if (!language.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.language"));
        }

        final ConceptExtractorConfig_V1 c = new ConceptExtractorConfig_V1();
        c.setHost(host.getValue());
        c.setPort(port.getValue());
        c.setExtractorApi(extractorApi.getValue());
        c.setUriSupplement(uriSupplement.getValue());
        c.setProjectId(projectId.getValue());
        c.setLanguage(language.getValue());
        c.setUsername(username.getValue());
        c.setPassword(password.getValue());
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        host = new TextField(ctx.tr("ConceptExtractor.dialog.host"));
        host.setWidth("100%");
        host.setRequired(true);
        mainLayout.addComponent(host);

        port = new TextField(ctx.tr("ConceptExtractor.dialog.port"));
        port.setWidth("100%");
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
        mainLayout.addComponent(port);

        extractorApi = new TextField(ctx.tr("ConceptExtractor.dialog.extractorApi"));
        extractorApi.setWidth("100%");
        extractorApi.setRequired(true);
        extractorApi.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.extractorApi"),
                8, null, false));
        mainLayout.addComponent(extractorApi);

        uriSupplement = new TextField(ctx.tr("ConceptExtractor.dialog.uriSupplement"));
        uriSupplement.setWidth("100%");
        uriSupplement.setRequired(false);
        mainLayout.addComponent(uriSupplement);

        projectId = new TextField(ctx.tr("ConceptExtractor.dialog.projectId"));
        projectId.setWidth("100%");
        projectId.setRequired(true);
        mainLayout.addComponent(projectId);

        language = new TextField(ctx.tr("ConceptExtractor.dialog.language"));
        language.setWidth("100%");
        language.setRequired(true);
        language.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.language"),
                2, 2, false));
        mainLayout.addComponent(language);

        username = new TextField(ctx.tr("ConceptExtractor.dialog.username"));
        username.setWidth("100%");
        username.setRequired(true);
        mainLayout.addComponent(username);

        password = new PasswordField(ctx.tr("ConceptExtractor.dialog.password"));
        password.setWidth("100%");
        password.setRequired(true);
        mainLayout.addComponent(password);

        setCompositionRoot(mainLayout);
    }
}
