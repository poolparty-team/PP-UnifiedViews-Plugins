/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.unifiedviews.plugins.swc.poolparty.ppx;

import at.punkt.commons.openrdf.vocabulary.PPX;
import at.punkt.poolparty.extractor.ExtractionService;
import at.punkt.poolparty.extractor.PpxClient;
import at.punkt.poolparty.extractor.web.domain.ThesaurusConcept;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.repository.RepositoryConnection;

import java.util.List;

/**
 *
 * @author Kata
 */
@DPU.AsExtractor
public class PPXExtractor extends ConfigurableBase<PPXConfig> implements ConfigDialogProvider<PPXConfig> {

    private ValueFactory factory = new ValueFactoryImpl();
    private ExtractionService extractionService;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    public PPXExtractor() {
        super(PPXConfig.class);
    }

    @Override
    public void execute(DPUContext context) throws DPUException, InterruptedException {
        try {
            extractionService =  new PpxClient(config.getServer(), "apiuser", "password");
            List<ThesaurusConcept> response = extractionService.getConcepts(
                    config.getProjectId(),
                    config.getLanguage(), "",
                    config.getText());
            RepositoryConnection con = rdfOutput.getConnection();
            try {
                for (ThesaurusConcept concept : response) {
                    URI conceptUri = factory.createURI(concept.getUri());
                    Literal scoreLiteral = factory.createLiteral(concept.getScore());
                    con.add(factory.createStatement(conceptUri, PPX.SCORE, scoreLiteral));

                    Literal prefLabelLiteral = factory.createLiteral(concept.getPrefLabel());
                    con.add(factory.createStatement(conceptUri, SKOS.PREF_LABEL, prefLabelLiteral));
                }
            } finally {
                con.close();
            }
        }
        catch (Exception ex) {
            throw new DPUException(ex);
        }
    }

    @Override
    public String toString() {
        return "PPX [" + "text: " + config.getText() + "] [" + "numberOfConcepts: " + config.getNumberOfConcepts() + "...]";
    }

    @Override
    public AbstractConfigDialog<PPXConfig> getConfigurationDialog() {
        return new PPXConfigDialog();
    }

}
