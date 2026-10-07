package org.industrial.ontology.domain.axiom;



import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.axiom.OWLAxiomData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/01/15
 */
public class OWLAxiomData implements Serializable {

    private String htmlRendering;

    private OWLAxiomData() {
    }

    public OWLAxiomData(String htmlRendering) {
        this.htmlRendering = checkNotNull(htmlRendering);
    }

    public String getHtmlRendering() {
        return htmlRendering;
    }

}
