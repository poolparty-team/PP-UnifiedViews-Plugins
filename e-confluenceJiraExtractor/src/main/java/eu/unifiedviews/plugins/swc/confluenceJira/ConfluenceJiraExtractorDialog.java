package eu.unifiedviews.plugins.swc.confluenceJira;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class ConfluenceJiraExtractorDialog extends AbstractDialog<ConfluenceJiraExtractorConfig>  {

    public ConfluenceJiraExtractorDialog() {
        super(ConfluenceJiraExtractor.class);
    }

    @Override
    protected void buildDialogLayout() {

    }

    @Override
    protected void setConfiguration(ConfluenceJiraExtractorConfig confluenceJiraExtractorConfig) throws DPUConfigException {

    }

    @Override
    protected ConfluenceJiraExtractorConfig getConfiguration() throws DPUConfigException {
        return null;
    }
}
