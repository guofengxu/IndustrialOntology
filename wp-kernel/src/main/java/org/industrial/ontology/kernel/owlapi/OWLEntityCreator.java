package org.industrial.ontology.kernel.owlapi;



import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.owlapi.OWLEntityCreator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 04/04/2012
 */
public class OWLEntityCreator<E extends OWLEntity> {

    private E entity;

    private List<OntologyChange> changes = new ArrayList<>();

    public OWLEntityCreator(E entity, List<OntologyChange> changes) {
        this.entity = entity;
        this.changes.addAll(changes);
    }


    public E getEntity() {
        return entity;
    }

    public List<OntologyChange> getChanges() {
        return new ArrayList<>(changes);
    }
}
