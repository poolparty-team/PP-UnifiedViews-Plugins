package eu.unifiedviews.plugins.swc.poolparty.load;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

@DPU.AsLoader
public class ThesaurusImportLoader extends
        ConfigurableBase<ThesaurusImportConfig> implements
        ConfigDialogProvider<ThesaurusImportConfig>
{
    public ThesaurusImportLoader(Class<ThesaurusImportConfig> configClass) {
        super(configClass);
    }

    @Override
    public AbstractConfigDialog<ThesaurusImportConfig> getConfigurationDialog() {
        return new ThesaurusImportDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, InterruptedException {

    }
}
