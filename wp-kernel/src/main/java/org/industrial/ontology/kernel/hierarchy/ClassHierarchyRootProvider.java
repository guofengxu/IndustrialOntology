package org.industrial.ontology.kernel.hierarchy;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.ClassHierarchyRootProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 02/06/15
 */
public class ClassHierarchyRootProvider implements Supplier<OWLClass> {

    private final OWLDataFactory dataFactory;

    public ClassHierarchyRootProvider(OWLDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    @Override
    public OWLClass get() {
        return dataFactory.getOWLThing();
    }
}
