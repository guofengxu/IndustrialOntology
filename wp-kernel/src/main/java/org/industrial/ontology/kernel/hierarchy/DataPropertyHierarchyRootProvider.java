package org.industrial.ontology.kernel.hierarchy;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.DataPropertyHierarchyRootProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 02/06/15
 */
public class DataPropertyHierarchyRootProvider implements Supplier<OWLDataProperty> {

    private final OWLDataFactory dataFactory;

    public DataPropertyHierarchyRootProvider(OWLDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    @Override
    public OWLDataProperty get() {
        return dataFactory.getOWLTopDataProperty();
    }
}
