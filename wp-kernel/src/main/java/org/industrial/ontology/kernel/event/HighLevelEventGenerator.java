package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.kernel.api.revision.HasGetRevisionSummary;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.util.AxiomSubjectProvider;
import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;

import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeVisitor;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;
import org.industrial.ontology.domain.event.AnnotationPropertyFrameChangedEvent;
import org.industrial.ontology.domain.event.ClassFrameChangedEvent;
import org.industrial.ontology.domain.event.DataPropertyFrameChangedEvent;
import org.industrial.ontology.domain.event.DatatypeFrameChangedEvent;
import org.industrial.ontology.domain.event.NamedIndividualFrameChangedEvent;
import org.industrial.ontology.domain.event.ObjectPropertyFrameChangedEvent;
import org.industrial.ontology.domain.event.OntologyFrameChangedEvent;
import org.industrial.ontology.domain.event.ProjectChangedEvent;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Collection;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.HighLevelEventGenerator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 19/03/2013
 */
public class HighLevelEventGenerator implements EventTranslator {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final RenderingManager renderingManager;

    @Nonnull
    private final HasGetRevisionSummary hasGetRevisionSummary;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitiesByIri;

    @Nonnull
    private final EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex;

    public HighLevelEventGenerator(@Nonnull ProjectId projectId,
                                   @Nonnull RenderingManager renderingManager,
                                   @Nonnull EntitiesInProjectSignatureByIriIndex entitiesByIri,
                                   @Nonnull HasGetRevisionSummary hasGetRevisionSummary,
                                   @Nonnull EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex) {
        this.projectId = checkNotNull(projectId);
        this.renderingManager = checkNotNull(renderingManager);
        this.entitiesByIri = checkNotNull(entitiesByIri);
        this.hasGetRevisionSummary = checkNotNull(hasGetRevisionSummary);
        this.entitiesInProjectSignatureIndex = checkNotNull(entitiesInProjectSignatureIndex);
    }

    @Override
    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {
    }

    @Override
    public void translateOntologyChanges(Revision revision,
                                         ChangeApplicationResult<?> changes,
                                         final List<ProjectEvent> projectEventList) {
        var changedEntities = new HashSet<OWLEntity>();
        var changedOntologies = new HashSet<OWLOntologyID>();
        changes.getChangeList()
               .forEach(change -> change.accept(new OntologyChangeVisitor() {
                   @Override
                   public void visit(@Nonnull AddAxiomChange addAxiomChange) {
                        handleAxiomChange(addAxiomChange.getAxiom());
                   }

                   @Override
                   public void visit(@Nonnull RemoveAxiomChange removeAxiomChange) {
                        handleAxiomChange(removeAxiomChange.getAxiom());
                   }

                   @Override
                   public void visit(@Nonnull AddOntologyAnnotationChange addOntologyAnnotationChange) {
                        handleOntologyFrameChange(addOntologyAnnotationChange);
                   }

                   @Override
                   public void visit(@Nonnull RemoveOntologyAnnotationChange removeOntologyAnnotationChange) {
                        handleOntologyFrameChange(removeOntologyAnnotationChange);
                   }

                   @Override
                   public void visit(@Nonnull AddImportChange addImportChange) {
                        handleOntologyFrameChange(addImportChange);
                   }

                   @Override
                   public void visit(@Nonnull RemoveImportChange removeImportChange) {
                        handleOntologyFrameChange(removeImportChange);
                   }

                   private void handleAxiomChange(OWLAxiom axiom) {
                       var axiomSubjectProvider = new AxiomSubjectProvider();
                       var subject = axiomSubjectProvider.getSubject(axiom);
                       var entities = getEntitiesForSubject(subject);
                       entities.stream()
                               .filter(e -> !changedEntities.add(e))
                               .map(entity -> toFrameChangedEvent(entity, revision))
                               .forEach(projectEventList::add);
                   }

                   private void handleOntologyFrameChange(OntologyChange change) {
                       var ontologyId = change.getOntologyId();
                       if(!changedOntologies.add(ontologyId)) {
                           return;
                       }
                       var event = new OntologyFrameChangedEvent(ontologyId, projectId);
                       projectEventList.add(event);
                   }
               }));


        var changedEntitiesData = new HashSet<OWLEntityData>();
        var subject = changes.getSubject();
        if(subject instanceof OWLEntity) {
            var entity = (OWLEntity) subject;
            if(entitiesInProjectSignatureIndex.containsEntityInSignature(entity)) {
                changedEntitiesData.add(renderingManager.getRendering(entity));
            }
        }
        else if(subject instanceof OWLEntityData) {
            var entityData = (OWLEntityData) subject;
            if(entitiesInProjectSignatureIndex.containsEntityInSignature(entityData.getEntity())) {
                changedEntitiesData.add(entityData);
            }
        }
        else if(subject instanceof Collection) {
            var collection = (Collection<?>) subject;
            collection.stream()
                      .filter(element -> element instanceof OWLEntity)
                      .map(element -> (OWLEntity) element)
                      .filter(entitiesInProjectSignatureIndex::containsEntityInSignature)
                      .forEach(entity -> changedEntitiesData.add(renderingManager.getRendering(entity)));

        }
        var revisionSummary = hasGetRevisionSummary.getRevisionSummary(revision.getRevisionNumber());
        if(revisionSummary.isPresent()) {
            var event = new ProjectChangedEvent(projectId, revisionSummary.get(), changedEntitiesData);
            projectEventList.add(event);
        }
    }


    private Set<OWLEntity> getEntitiesForSubject(OWLObject subject) {
        Set<OWLEntity> entities;
        if(subject instanceof IRI) {
            entities = entitiesByIri.getEntitiesInSignature((IRI) subject)
                                    .collect(Collectors.toSet());
        }
        else if(subject instanceof OWLEntity) {
            entities = Collections.singleton((OWLEntity) subject);
        }
        else {
            entities = Collections.emptySet();
        }
        return entities;
    }

    private ProjectEvent toFrameChangedEvent(OWLEntity e, Revision revision) {
        return e.accept(new OWLEntityVisitorEx<>() {
            @Nonnull
            @Override
            public ProjectEvent visit(@Nonnull OWLClass cls) {
                return new ClassFrameChangedEvent(cls, projectId, revision.getUserId());
            }

            @Nonnull
            @Override
            public ProjectEvent visit(@Nonnull OWLObjectProperty property) {
                return new ObjectPropertyFrameChangedEvent(property,
                                                           projectId,
                                                           revision.getUserId());
            }

            @Nonnull
            @Override
            public ProjectEvent visit(@Nonnull OWLDataProperty property) {
                return new DataPropertyFrameChangedEvent(property,
                                                         projectId,
                                                         revision.getUserId());
            }

            @Nonnull
            @Override
            public ProjectEvent visit(@Nonnull OWLNamedIndividual individual) {
                return new NamedIndividualFrameChangedEvent(individual,
                                                            projectId,
                                                            revision.getUserId());
            }

            @Nonnull
            @Override
            public ProjectEvent visit(@Nonnull OWLDatatype datatype) {
                return new DatatypeFrameChangedEvent(datatype, projectId, revision.getUserId());
            }

            @Nonnull
            @Override
            public ProjectEvent visit(@Nonnull OWLAnnotationProperty property) {
                return new AnnotationPropertyFrameChangedEvent(property,
                                                               projectId,
                                                               revision.getUserId());
            }
        });
    }
}
