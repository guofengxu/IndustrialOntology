package org.industrial.ontology.kernel.hierarchy;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.ObjectPropertyHierarchyRootProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 02/06/15
 */
public class ObjectPropertyHierarchyRootProvider implements Supplier<OWLObjectProperty> {

    private final OWLDataFactory dataFactory;

    public ObjectPropertyHierarchyRootProvider(OWLDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    @Override
    public OWLObjectProperty get() {
        return dataFactory.getOWLTopObjectProperty();
    }
}
