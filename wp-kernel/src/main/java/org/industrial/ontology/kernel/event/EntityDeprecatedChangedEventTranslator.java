package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.mansyntax.render.DeprecatedEntityChecker;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.domain.event.EntityDeprecatedChangedEvent;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;

import java.util.List;
import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_ASSERTION;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.EntityDeprecatedChangedEventTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22/05/15
 */
public class EntityDeprecatedChangedEventTranslator implements EventTranslator {


    private ProjectId projectId;

    private DeprecatedEntityChecker deprecatedEntityChecker;

    private EntitiesInProjectSignatureByIriIndex entitiesByIri;

    public EntityDeprecatedChangedEventTranslator(ProjectId projectId,
                                                  DeprecatedEntityChecker deprecatedEntityChecker,
                                                  EntitiesInProjectSignatureByIriIndex entitiesByIri) {
        this.projectId = projectId;
        this.deprecatedEntityChecker = deprecatedEntityChecker;
        this.entitiesByIri = entitiesByIri;
    }


    @Override
    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {

    }

    @Override
    public void translateOntologyChanges(Revision revision,
                                         ChangeApplicationResult<?> changes,
                                         List<ProjectEvent> projectEventList) {
        for(OntologyChange change : changes.getChangeList()) {
            if(change.isChangeFor(ANNOTATION_ASSERTION)) {
                var annotationAssertion = (OWLAnnotationAssertionAxiom) change.getAxiomOrThrow();
                if(annotationAssertion.getProperty()
                        .isDeprecated()) {
                    if(annotationAssertion.getSubject() instanceof IRI) {
                        IRI subject = (IRI) annotationAssertion.getSubject();
                        entitiesByIri.getEntitiesInSignature(subject)
                                     .map(entity -> {
                                         var deprecated = deprecatedEntityChecker.isDeprecated(entity);
                                         return new EntityDeprecatedChangedEvent(projectId, entity, deprecated);
                                     })
                                     .forEach(projectEventList::add);
                    }
                }
            }
        }
    }
}
