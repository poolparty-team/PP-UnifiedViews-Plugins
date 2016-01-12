package eu.unifiedviews.swc;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for MyDpu.
 *
 * @author Unknown
 */
public class ConfluenceJiraDialog extends AbstractDialog<ConfluenceJiraConfig> {

    public ConfluenceJiraDialog() {
        super(ConfluenceJira.class);
    }

    @Override
    public void setConfiguration(ConfluenceJiraConfig c) throws DPUConfigException {

    }

    @Override
    public ConfluenceJiraConfig getConfiguration() throws DPUConfigException {
        final ConfluenceJiraConfig c = new ConfluenceJiraConfig();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("MyDpu.dialog.label")));

        setCompositionRoot(mainLayout);
    }
}
