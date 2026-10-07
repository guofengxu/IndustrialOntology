package org.industrial.ontology.kernel.form;



import org.industrial.ontology.kernel.frame.FrameComponentSessionRenderer;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collection;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormDataBuilderSessionRenderer}.
 */
@FormDataBuilderSession
public class FormDataBuilderSessionRenderer {

    @Nonnull
    private final FrameComponentSessionRenderer frameComponentSessionRenderer;

    public FormDataBuilderSessionRenderer(@Nonnull FrameComponentSessionRenderer frameComponentSessionRenderer) {
        this.frameComponentSessionRenderer = frameComponentSessionRenderer;
    }

    @Nonnull
    public OWLEntityData getEntityRendering(OWLEntity subject) {
        return frameComponentSessionRenderer.getEntityRendering(subject);
    }

    @Nonnull
    public Collection<OWLEntityData> getRendering(IRI iri) {
        return frameComponentSessionRenderer.getRendering(iri);
    }
}
