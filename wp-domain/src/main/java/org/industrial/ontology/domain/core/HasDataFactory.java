package org.industrial.ontology.domain.core;



import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasDataFactory}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/08/2013
 */
public interface HasDataFactory {

    OWLDataFactory getDataFactory();

}
