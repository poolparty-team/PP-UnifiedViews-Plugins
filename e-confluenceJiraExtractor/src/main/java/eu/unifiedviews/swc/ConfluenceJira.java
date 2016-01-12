package eu.unifiedviews.swc;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsExtractor
public class ConfluenceJira extends AbstractDpu<ConfluenceJiraConfig> {

    private static final Logger log = LoggerFactory.getLogger(ConfluenceJira.class);
		
	public ConfluenceJira() {
		super(ConfluenceJiraDialog.class, ConfigHistory.noHistory(ConfluenceJiraConfig.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {

        ContextUtils.sendShortInfo(ctx, "MyDpu.message");
        
    }
	
}
