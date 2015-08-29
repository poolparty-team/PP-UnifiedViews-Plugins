package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.validator.UrlValidator;

/**
 * Vaadin configuration dialog for ConceptExtractor.
 *
 * @author Unknown
 */
public class ConceptExtractorVaadinDialog extends AbstractDialog<ConceptExtractorConfig_V1> {
    private TextField serverUrl;
    private TextField serverPort;
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
        serverUrl.setValue(c.getServerUrl());
        serverPort.setValue(Integer.toString(c.getServerPort()));
        extractorApi.setValue(c.getExtractorApi());
        uriSupplement.setValue(c.getUriSupplement());
        projectId.setValue(c.getProjectId());
        language.setValue(c.getLanguage());
        username.setValue(c.getUsername());
        password.setValue(c.getPassword());
    }

    @Override
    public ConceptExtractorConfig_V1 getConfiguration() throws DPUConfigException {
        if (!serverUrl.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.serverUrl"));
        }
        if (!serverPort.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.serverPort"));
        }
        if (!extractorApi.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.extractorApi"));
        }
        if (!language.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.language"));
        }

        final ConceptExtractorConfig_V1 c = new ConceptExtractorConfig_V1();
        c.setServerUrl(serverUrl.getValue());
        c.setServerPort(Integer.parseInt(serverPort.getValue()));
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

        serverUrl = new TextField(ctx.tr("ConceptExtractor.dialog.serverUrl"));
        serverUrl.setWidth("100%");
        serverUrl.setRequired(true);
        serverUrl.addValidator(new UrlValidator(false));
        mainLayout.addComponent(serverUrl);
        mainLayout.setExpandRatio(serverUrl, 0);

        serverPort = new TextField(ctx.tr("ConceptExtractor.dialog.serverPort"));
        serverPort.setWidth("100%");
        serverPort.setRequired(true);
        serverPort.addValidator(new IntegerRangeValidator(ctx.tr("ConceptExtractor.dialog.error.serverPort"),
                0, 65535));
        mainLayout.addComponent(serverPort);
        mainLayout.setExpandRatio(serverPort, 0);

        extractorApi = new TextField(ctx.tr("ConceptExtractor.dialog.extractorApi"));
        extractorApi.setWidth("100%");
        extractorApi.setRequired(true);
        extractorApi.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.extractorApi"),
                8, null, false));
        mainLayout.addComponent(extractorApi);
        mainLayout.setExpandRatio(extractorApi, 0);

        uriSupplement = new TextField(ctx.tr("ConceptExtractor.dialog.uriSupplement"));
        uriSupplement.setWidth("100%");
        uriSupplement.setRequired(false);
        mainLayout.addComponent(uriSupplement);
        mainLayout.setExpandRatio(uriSupplement, 0);

        projectId = new TextField(ctx.tr("ConceptExtractor.dialog.projectId"));
        projectId.setWidth("100%");
        projectId.setRequired(true);
        mainLayout.addComponent(projectId);
        mainLayout.setExpandRatio(projectId, 0);

        language = new TextField(ctx.tr("ConceptExtractor.dialog.language"));
        language.setWidth("100%");
        language.setRequired(true);
        language.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.language"),
                2, 2, false));
        mainLayout.addComponent(language);
        mainLayout.setExpandRatio(language, 0);

        username = new TextField(ctx.tr("ConceptExtractor.dialog.username"));
        username.setWidth("100%");
        username.setRequired(true);
        mainLayout.addComponent(username);
        mainLayout.setExpandRatio(username, 0);

        password = new PasswordField(ctx.tr("ConceptExtractor.dialog.password"));
        password.setWidth("100%");
        password.setRequired(true);
        mainLayout.addComponent(password);
        mainLayout.setExpandRatio(password, 0);

        setCompositionRoot(mainLayout);
    }
}
