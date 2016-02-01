package eu.unifiedviews.swc;

import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ConfluenceJiraExtractorDialog extends AbstractDialog<ConfluenceJiraExtractorConfig> {

    private TextField confluenceApiBaseUri, jiraApiBaseUri, username, jiraProjectKeys;
    private PasswordField password;

    public ConfluenceJiraExtractorDialog() {
        super(ConfluenceJiraExtractor.class);
    }

    @Override
    public void setConfiguration(ConfluenceJiraExtractorConfig c) throws DPUConfigException {
        confluenceApiBaseUri.setValue(c.getConfluenceApiBaseUri());
        jiraApiBaseUri.setValue(c.getJiraApiBaseUri());
        username.setValue(c.getUsername());
        password.setValue(c.getPassword());

        String projectKeys = "";
        Iterator<String> keyIt = c.getJiraProjectKeys().iterator();
        while (keyIt.hasNext()) {
            projectKeys += keyIt.next() + (keyIt.hasNext() ? ", " : "");
        }
        jiraProjectKeys.setValue(projectKeys);
    }

    @Override
    public ConfluenceJiraExtractorConfig getConfiguration() throws DPUConfigException {
        final ConfluenceJiraExtractorConfig c = new ConfluenceJiraExtractorConfig();

        c.setConfluenceApiBaseUri(confluenceApiBaseUri.getValue());
        c.setJiraApiBaseUri(jiraApiBaseUri.getValue());
        c.setUsername(username.getValue());
        c.setPassword(password.getValue());

        StringTokenizer tokenizer = new StringTokenizer(jiraProjectKeys.getValue(), ",");
        Collection<String> projectKeys = new ArrayList<>();
        while (tokenizer.hasMoreElements()) {
            projectKeys.add(tokenizer.nextToken().trim());
        }
        c.setJiraProjectKeys(projectKeys);

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        confluenceApiBaseUri = new TextField(ctx.tr("ConfluenceJira.dialog.confluenceApiBaseUri"));
        confluenceApiBaseUri.setWidth("90%");
        confluenceApiBaseUri.setRequired(true);
        mainLayout.addComponent(confluenceApiBaseUri);

        jiraApiBaseUri = new TextField(ctx.tr("ConfluenceJira.dialog.jiraApiBaseUri"));
        jiraApiBaseUri.setWidth("90%");
        jiraApiBaseUri.setRequired(true);
        mainLayout.addComponent(jiraApiBaseUri);

        jiraProjectKeys = new TextField(ctx.tr("ConfluenceJira.dialog.jiraProjectKeys"));
        jiraProjectKeys.setWidth("90%");
        jiraProjectKeys.setRequired(true);
        mainLayout.addComponent(jiraProjectKeys);

        username = new TextField(ctx.tr("ConfluenceJira.dialog.username"));
        username.setWidth("90%");
        username.setRequired(true);
        mainLayout.addComponent(username);

        password = new PasswordField(ctx.tr("ConfluenceJira.dialog.password"));
        password.setWidth("90%");
        password.setRequired(true);
        mainLayout.addComponent(password);

        setCompositionRoot(mainLayout);
    }
}
