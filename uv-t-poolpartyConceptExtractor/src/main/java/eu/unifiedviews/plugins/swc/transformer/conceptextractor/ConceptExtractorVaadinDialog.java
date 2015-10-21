package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.*;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Vaadin configuration dialog for ConceptExtractor.
 *
 * @author Yang Yuanzhe
 */
public class ConceptExtractorVaadinDialog extends AbstractDialog<ConceptExtractorConfig_V1> {
    private TextField host;
    private TextField port;
    private TextField projectId;
    private TextField language;
    private TextField extractorApi;
    private TextField username;
    private PasswordField password;
    private TextField numberOfConcepts;
    private TextField numberOfTerms;
    private TextField corpusScoring;
    private OptionGroup booleanOptions;

    public ConceptExtractorVaadinDialog() {
        super(ConceptExtractor.class);
    }

    @Override
    public void setConfiguration(ConceptExtractorConfig_V1 c) throws DPUConfigException {
        host.setValue(c.getHost());
        port.setValue(c.getPort());
        extractorApi.setValue(c.getExtractorApi());
        projectId.setValue(c.getProjectId());
        language.setValue(c.getLanguage());
        username.setValue(c.getUsername());
        password.setValue(c.getPassword());
        numberOfConcepts.setValue(c.getNumberOfConcepts());
        numberOfTerms.setValue(c.getNumberOfTerms());
        corpusScoring.setValue(c.getCorpusScoring());
        for (String param : c.getBooleanParams()) {
            booleanOptions.select(param);
        }
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
        if (!username.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.username"));
        }
        if (!password.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.password"));
        }
        if (!projectId.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.projectId"));
        }
        if (!numberOfConcepts.isValid() || !numberOfTerms.isValid()) {
            throw new DPUConfigException(ctx.tr("ConceptExtractor.dialog.error.numberOf"));
        }

        final ConceptExtractorConfig_V1 c = new ConceptExtractorConfig_V1();
        c.setHost(host.getValue());
        c.setPort(port.getValue());
        c.setExtractorApi(extractorApi.getValue());
        c.setProjectId(projectId.getValue());
        c.setLanguage(language.getValue());
        c.setUsername(username.getValue());
        c.setPassword(password.getValue());
        c.setNumberOfConcepts(numberOfConcepts.getValue());
        c.setNumberOfTerms(numberOfTerms.getValue());
        c.setCorpusScoring(corpusScoring.getValue());
        List<String> booleanParams = new ArrayList<>();
        Collection selectedItems = (Collection) booleanOptions.getValue();
        for (Object id : selectedItems) {
            booleanParams.add(id.toString());
        }
        c.setBooleanParams(booleanParams);
        return c;
    }

    @Override
    public void buildDialogLayout() {
        final GridLayout mainLayout = new GridLayout(2, 7);
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);
        mainLayout.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
        mainLayout.setColumnExpandRatio(0, 0.5f);
        mainLayout.setColumnExpandRatio(1, 0.5f);

        host = new TextField(ctx.tr("ConceptExtractor.dialog.host"));
        host.setWidth("400px");
        host.setRequired(true);
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

        extractorApi = new TextField(ctx.tr("ConceptExtractor.dialog.extractorApi"));
        extractorApi.setWidth("400px");
        extractorApi.setRequired(true);
        extractorApi.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.extractorApi"),
                8, null, false));
        mainLayout.addComponent(extractorApi, 0, 1, 0, 1);

        projectId = new TextField(ctx.tr("ConceptExtractor.dialog.projectId"));
        projectId.setWidth("400px");
        projectId.setRequired(true);
        mainLayout.addComponent(projectId, 0, 2, 0, 2);

        language = new TextField(ctx.tr("ConceptExtractor.dialog.language"));
        language.setWidth("200px");
        language.setRequired(true);
        language.addValidator(new StringLengthValidator(ctx.tr("ConceptExtractor.dialog.error.language"),
                2, 2, false));
        mainLayout.addComponent(language, 0, 3, 0, 3);

        username = new TextField(ctx.tr("ConceptExtractor.dialog.username"));
        username.setWidth("200px");
        username.setRequired(true);
        mainLayout.addComponent(username, 1, 1, 1, 1);

        password = new PasswordField(ctx.tr("ConceptExtractor.dialog.password"));
        password.setWidth("200px");
        password.setRequired(true);
        mainLayout.addComponent(password, 1, 2, 1, 2);

        numberOfTerms = new TextField(ctx.tr("ConceptExtractor.dialog.numberOfTerms"));
        numberOfTerms.setWidth("200px");
        numberOfTerms.setRequired(false);
        numberOfTerms.addValidator(new Validator() {
            @Override
            public void validate(Object o) throws InvalidValueException {
                if (o.equals("")) return;
                int number = 0;
                try {
                    number = Integer.parseInt(o.toString());
                } catch (Exception e) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.numberOf"));
                }
                if (number < 1) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.numberOf"));
                }
            }
        });
        mainLayout.addComponent(numberOfTerms, 0, 4, 0, 4);

        numberOfConcepts = new TextField(ctx.tr("ConceptExtractor.dialog.numberOfConcepts"));
        numberOfConcepts.setWidth("200px");
        numberOfConcepts.setRequired(false);
        numberOfConcepts.addValidator(new Validator() {
            @Override
            public void validate(Object o) throws InvalidValueException {
                if (o.equals("")) return;
                int number = 0;
                try {
                    number = Integer.parseInt(o.toString());
                } catch (Exception e) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.numberOf"));
                }
                if (number < 1) {
                    throw new InvalidValueException(ctx.tr("ConceptExtractor.dialog.error.numberOf"));
                }
            }
        });
        mainLayout.addComponent(numberOfConcepts, 0, 5, 0, 5);

        corpusScoring = new TextField(ctx.tr("ConceptExtractor.dialog.corpusScoring"));
        corpusScoring.setWidth("200px");
        corpusScoring.setRequired(false);
        mainLayout.addComponent(corpusScoring, 0, 6, 0, 6);

        booleanOptions = new OptionGroup(ctx.tr("ConceptExtractor.dialog.booleanOptions"));
        booleanOptions.setMultiSelect(true);
        booleanOptions.addItems("useTransitiveBroaderConcepts", "useTransitiveBroaderTopConcepts", "useRelatedConcepts", "filterNestedConcepts", "tfidfScoring", "useTypes");;
        mainLayout.addComponent(booleanOptions, 1, 3, 1, 6);

        setCompositionRoot(mainLayout);
    }
}
