package org.industrial.ontology.kernel.mansyntax.render;



import java.util.Optional;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.DefaultItemStyleProvider}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 25/02/2014
 */
public class DefaultItemStyleProvider implements ItemStyleProvider {

    public DefaultItemStyleProvider() {
    }

    @Override
    public Optional<String> getItemStyle(Object item) {
        if(item instanceof OWLClassExpression) {
            return Optional.of("ms-item-ce");
        }
        if(item instanceof OWLAnnotationProperty) {
            return Optional.of("ms-item-anno");
        }
        if(item instanceof OWLProperty) {
            return Optional.of("ms-item-prop");
        }
        if(item instanceof OWLIndividual) {
            return Optional.of("ms-item-ind");
        }
        if(item instanceof OWLDataRange) {
            return Optional.of("ms-item-dr");
        }
        return Optional.empty();

    }
}
