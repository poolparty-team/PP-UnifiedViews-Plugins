package eu.unifiedviews.plugins.swc.transformer.conceptextractor;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * Vaadin configuration dialog for ConceptExtractor.
 *
 * @author Unknown
 */
public class ConceptExtractorVaadinDialog extends AbstractDialog<ConceptExtractorConfig_V1> {

    public ConceptExtractorVaadinDialog() {
        super(ConceptExtractor.class);
    }

    @Override
    public void setConfiguration(ConceptExtractorConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public ConceptExtractorConfig_V1 getConfiguration() throws DPUConfigException {
        final ConceptExtractorConfig_V1 c = new ConceptExtractorConfig_V1();

        return c;
    }

    @Override
    public void buildDialogLayout() {
        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(true);

        mainLayout.addComponent(new Label(ctx.tr("Tabular.dialog.label")));

        setCompositionRoot(mainLayout);
    }
}
