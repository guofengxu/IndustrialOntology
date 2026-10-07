package org.industrial.ontology.kernel.api.revision;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionType}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 06/06/2012
 */
public enum RevisionType {

    /**
     * Changes are not applied by a user during an edit.  They are the very first set of changes that must be applied
     * to an empty ontology document to bring it up to the baseline revision.
     */
    BASELINE,

    /**
     * Changes are made by a user during an edit.  These changes are applied after the baseline.
     */
    EDIT
}
