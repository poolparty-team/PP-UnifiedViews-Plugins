package eu.unifiedviews.plugins.swc.confluenceJira;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;

@DPU.AsExtractor
public class ConfluenceJiraExtractor extends AbstractDpu<ConfluenceJiraExtractorConfig> {

    public ConfluenceJiraExtractor() {
        super(ConfluenceJiraExtractorDialog.class, ConfigHistory.noHistory(ConfluenceJiraExtractorConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException {

    }
}
