package org.industrial.ontology.kernel.render;



import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.ShortFormProvider;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.OWLObjectRendererImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 02/06/15
 */
public class OWLObjectRendererImpl implements OWLObjectRenderer {

    private RenderingManager renderingManager;

    public OWLObjectRendererImpl(RenderingManager renderingManager) {
        this.renderingManager = renderingManager;
    }

    @Override
    public void setShortFormProvider(ShortFormProvider shortFormProvider) {

    }

    @Override
    public String render(OWLObject object) {
        return renderingManager.getBrowserText(object);
    }
}
