package eu.unifiedviews.plugins.swc.api;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.*;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.plugins.swc.auth.*;
import eu.unifiedviews.plugins.swc.api.json.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.ServiceNotFoundException;

/**
 *
 * @author kreisera
 */
public class PoolPartyApiPanel extends VerticalLayout {

    private final Logger logger = LoggerFactory.getLogger(PoolPartyApiPanel.class);
    private final TextField server = new TextField("PoolParty Server URL (without '/PoolParty')");
    private final TextField projectId = new TextField("Project UUID");
    private final TextField supplement = new TextField("URI Supplement");
    private final Select auth = new Select("Authentication");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");

    public PoolPartyApiPanel() {
        server.setInputPrompt("http://vocabulary.company.com");
        server.setWidth("300px");
        server.setRequired(true);
        server.setRequiredError("PoolParty server URL is required");
        addComponent(server);

        auth.addItem(AuthType.None);
        auth.addItem(AuthType.Basic_Auth);

        auth.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                AuthType type = (AuthType) event.getProperty().getValue();
                if (type == AuthType.None) {
                    username.setEnabled(false);
                    password.setEnabled(false);
                } else {
                    username.setEnabled(true);
                    password.setEnabled(true);
                }
            }
        });
        auth.setImmediate(true);
        auth.setNullSelectionAllowed(false);
        auth.setItemCaptionMode(Select.ITEM_CAPTION_MODE_ID);
        addComponent(auth);
        username.setRequired(false);
        username.setImmediate(true);
        addComponent(username);
        password.setRequired(false);
        password.setImmediate(true);
        addComponent(password);

        projectId.setWidth("300px");
        projectId.setRequired(true);
        projectId.setRequiredError("Project UUID is required");

        supplement.setWidth("300px");
        supplement.setRequired(true);
        supplement.setRequiredError("URI Supplement is required");

        final Table table = new Table("Select one of the projects below to copy the project ID");
        table.setContainerDataSource(new BeanItemContainer<Project>(Project.class));
        table.setVisibleColumns(new String[]{"title", "description", "defaultLanguage"});
        table.setSelectable(true);
        table.setMultiSelect(false);
        table.addListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                Project project = (Project) event.getItemId();
                projectId.setValue(project.getId());
                supplement.setValue(project.getUri().substring(project.getUri().lastIndexOf("/")+1));
            }
        });
        table.setSizeFull();
        Button fetchProjects = new Button("Fetch Projects", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                String serverUrl = server.getValue();
                PptApiConnector api = new PptApiConnector(serverUrl, getAuthentication());
                try {
                    table.getContainerDataSource().removeAllItems();
                    for (Project p : api.getProjects()) {
                        table.getContainerDataSource().addItem(p);
                    }
                } catch (AuthenticationFailedException ex) {
                    Notification.show("Access to PPT API denied - please check your authentication credentials.", Notification.TYPE_ERROR_MESSAGE);
                    logger.error("Unable to fetch projects from server: " + serverUrl, ex);
                } catch (ServiceNotFoundException ex) {
                    Notification.show("PPT API is not available: "+ PptApiConnector.getServiceUrl(serverUrl, "api"), Notification.TYPE_ERROR_MESSAGE);
                    logger.error("Unable to fetch projects from server: " + serverUrl, ex);
                } catch (Exception ex) {
                    Notification.show("Unable to fetch projects from server: " + serverUrl, "please fill out the project ID manually", Notification.TYPE_ERROR_MESSAGE);
                    logger.error("Unable to fetch projects from server: " + serverUrl, ex);
                }
            }
        });
        addComponent(fetchProjects);
        addComponent(table);
        addComponent(projectId);
        addComponent(supplement);
    }

    public void setFromApiConfig(PoolPartyApiConfig config) {
        server.setValue(config.getServer());
        auth.select(config.getAuthentication().getType());
        if (config.getAuthentication() instanceof UsernamePasswordCredentials) {
            username.setValue(((UsernamePasswordCredentials) config.getAuthentication()).getUsername());
            password.setValue(((UsernamePasswordCredentials) config.getAuthentication()).getPassword());
        }
        projectId.setValue(config.getProjectId());
        supplement.setValue(config.getUriSupplement());
    }

    public PoolPartyApiConfig getApiConfig() throws DPUConfigException {
        if (!server.isValid() || !projectId.isValid()) {
            throw new DPUConfigException("All values must be filled.");
        }

        PoolPartyApiConfig config = new PoolPartyApiConfig();
        config.setAuthentication(getAuthentication());
        config.setProjectId(projectId.getValue());
        config.setUriSupplement(supplement.getValue());
        config.setServer(server.getValue());
        return config;
    }

    private Authentication getAuthentication() {
        if (auth.getValue() == AuthType.Basic_Auth) {
            return new BasicAuthentication(username.getValue(), password.getValue());
        } else {
            return new NoAuthentication();
        }
    }
}
