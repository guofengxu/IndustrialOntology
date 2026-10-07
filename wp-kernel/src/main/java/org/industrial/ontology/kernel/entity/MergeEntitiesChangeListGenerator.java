package org.industrial.ontology.kernel.entity;



import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.domain.entity.MergedEntityTreatment;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.entity.MergedEntityTreatment.DEPRECATE_MERGED_ENTITY;
import static org.semanticweb.owlapi.vocab.SKOSVocabulary.ALTLABEL;

import static org.semanticweb.owlapi.vocab.SKOSVocabulary.PREFLABEL;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;

import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.entity.MergeEntitiesChangeListGenerator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 9 Mar 2018
 *
 * Performs a simple merge of one entity into another entity.  For a source entity, S, and a
 * target entity, T, S will be merged into T.  This involves the following:
 * 1) All usages of S will be replaced by T.
 * 2) Annotation assertion axioms that annotate S with an rdfs:label or a skos:prefLabel will
 *    be removed and replaced with annotation assertions that annotate T with the value of the
 *    annotations on S but with the property switched to skos:altLabel.  For example,
 *    AnnotationAssertion(rdfs:label S "Blah"@en) will be replaced with
 *    AnnotationAssertion(skos:altLabel S "Blah"@en).
 * 3) If the treatement is deprecation, then an annotation assertion is added to deprecate S
 *    and original annotations on S are preserved.  If the treatment is deletion then any
 *    discussion threads on S will be copied over to T.
 */
public class MergeEntitiesChangeListGenerator implements ChangeListGenerator<OWLEntity> {

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Nonnull
    private final ImmutableSet<OWLEntity> sourceEntities;

    @Nonnull
    private final OWLEntity targetEntity;

    @Nonnull
    private final MergedEntityTreatment treatment;

    @Nonnull
    private final EntityDiscussionThreadRepository discussionThreadRepository;

    @Nonnull
    private final String commitMessage;

    @Nonnull
    private final EntityRenamer entityRenamer;

    @Nonnull
    private final DefaultOntologyIdManager defaultOntologyIdManager;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologies;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertions;

    public MergeEntitiesChangeListGenerator(@Nonnull ImmutableSet<OWLEntity> sourceEntities,
                                            @Nonnull OWLEntity targetEntity,
                                            @Nonnull MergedEntityTreatment treatment,
                                            @Nonnull String commitMessage,
                                            @Nonnull ProjectId projectId,
                                            @Nonnull OWLDataFactory dataFactory,
                                            @Nonnull EntityDiscussionThreadRepository discussionThreadRepository,
                                            @Nonnull EntityRenamer entityRenamer,
                                            @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                                            @Nonnull ProjectOntologiesIndex projectOntologies,
                                            @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertions) {
        this.projectId = checkNotNull(projectId);
        this.dataFactory = checkNotNull(dataFactory);
        this.sourceEntities = checkNotNull(sourceEntities);
        this.targetEntity = checkNotNull(targetEntity);
        this.treatment = checkNotNull(treatment);
        this.discussionThreadRepository = checkNotNull(discussionThreadRepository);
        this.commitMessage = checkNotNull(commitMessage);
        this.entityRenamer = checkNotNull(entityRenamer);
        this.defaultOntologyIdManager = checkNotNull(defaultOntologyIdManager);
        this.projectOntologies = checkNotNull(projectOntologies);
        this.annotationAssertions = checkNotNull(annotationAssertions);
    }

    @Override
    public OntologyChangeList<OWLEntity> generateChanges(ChangeGenerationContext context) {

        // Generate changes to perform a merge.  The order of the generation of these changes
        // is important.  Usage changes must be generated first.
        OntologyChangeList.Builder<OWLEntity> builder = OntologyChangeList.builder();

        // Generate changes to replace usage of the entity.  This will essentially merge the
        // entity into the target entity
        replaceUsage(builder);

        // Avoid conflicts with labels.  The merged term must not duplicate preferred labels for
        // a given language.
        replaceLabels(builder);

        // Deprecated, if necessary
        if (treatment == DEPRECATE_MERGED_ENTITY) {
            deprecateSourceEntities(builder);
        }
        else {
            // Copy over discussion threads if the entity is being deleted so that these
            // will still be accessible.
            sourceEntities.forEach(sourceEntity -> discussionThreadRepository.replaceEntity(projectId, sourceEntity, targetEntity));

            // TODO:  Name Map Old IRI to new IRI - we don't have this functionality yet
        }

        return builder.build(targetEntity);

    }

    private void deprecateSourceEntities(OntologyChangeList.Builder<OWLEntity> builder) {
        sourceEntities.forEach(sourceEntity -> {
            // Add an annotation assertion to deprecate the source entity
            var sourceEntityIRI = sourceEntity.getIRI();
            var deprecatedAx = dataFactory.getDeprecatedOWLAnnotationAssertionAxiom(sourceEntityIRI);
            var ontologyId = defaultOntologyIdManager.getDefaultOntologyId();
            var addDeprecatedAxiom = AddAxiomChange.of(ontologyId, deprecatedAx);
            builder.add(addDeprecatedAxiom);

            // Preserve labels and other annotations on the source entity
            projectOntologies.getOntologyIds().forEach(ontId -> {
                annotationAssertions.getAxiomsForSubject(sourceEntityIRI, ontId)
                                    .map(ax -> AddAxiomChange.of(ontId, ax))
                                    .forEach(builder::add);
            });
        });
    }

    private void replaceUsage(OntologyChangeList.Builder<OWLEntity> builder) {
        sourceEntities.forEach(sourceEntity -> {
            var renameChanges = entityRenamer.generateChanges(ImmutableMap.of(sourceEntity, targetEntity.getIRI()));
            builder.addAll(renameChanges);
        });
    }

    private void replaceLabels(@Nonnull OntologyChangeList.Builder<OWLEntity> builder) {
        // Replace rdfs:label with skos:altLabel.
        // Replace skos:prefLabel with skos:altLabel.
        // In both cases, language tags are preserved.
        projectOntologies.getOntologyIds().forEach(ontId -> {
            sourceEntities.forEach(sourceEntity -> {
                var sourceEntityIRI = sourceEntity.getIRI();
                // Get the annotation assertions that were originally on the source entity
                annotationAssertions.getAxiomsForSubject(sourceEntityIRI, ontId)
                                    // Just deal with explicit rdfs:label and skos:prefLabel annotations
                                    .filter(ax -> isRdfsLabelAnnotation(ax) || isSkosPrefLabelAnnotation(ax))
                                    // Replace on the target entity with skos:altLabel as the property
                                    .forEach(ax -> replaceWithSkosAltLabel(ax, ontId, builder));
            });
        });
    }

    /**
     * Replaces the specified annotation assertion on the target entity with an annotation assertion whose
     * property is skos:altLabel.
     *
     * @param ax        The annotation assertion under consideration.  This is the original annotation
     *                  assertion on the source entity (not the target entity).
     * @param ontId        The ontology to make the changes in.
     * @param builder    The builder for adding changes to.
     */
    private void replaceWithSkosAltLabel(@Nonnull OWLAnnotationAssertionAxiom ax,
                                         @Nonnull OWLOntologyID ontId,
                                         @Nonnull OntologyChangeList.Builder<OWLEntity> builder) {
        // Remove the original one (that will be on the target entity by now)
        var origAx = dataFactory.getOWLAnnotationAssertionAxiom(
                targetEntity.getIRI(),
                ax.getAnnotation(),
                ax.getAnnotations());
        builder.add(RemoveAxiomChange.of(ontId, origAx));

        // Generate a new annotation with a property of skos:altLabel.
        // Preserve any annotations on the annotation.
        OWLAnnotation replAnno = dataFactory.getOWLAnnotation(getSkosAltLabel(),
                                                              ax.getAnnotation().getValue(),
                                                              ax.getAnnotation().getAnnotations());
        // Generate a new annotation assertion to replace the original one.
        // Preserve any annotations on the axiom.
        OWLAxiom replAx = dataFactory.getOWLAnnotationAssertionAxiom(
                targetEntity.getIRI(),
                replAnno,
                ax.getAnnotations());
        builder.add(AddAxiomChange.of(ontId, replAx));
    }

    /**
     * Determines if the given annotation assertion axiom provides an rdfs:label
     */
    private boolean isRdfsLabelAnnotation(@Nonnull OWLAnnotationAssertionAxiom ax) {
        return ax.getProperty().isLabel();
    }

    /**
     * Determines if the given annotation assertion axiom provides a skos:prefLabel
     */
    private boolean isSkosPrefLabelAnnotation(@Nonnull OWLAnnotationAssertionAxiom ax) {
        return PREFLABEL.getIRI().equals(ax.getProperty().getIRI());
    }

    @Nonnull
    private OWLAnnotationProperty getSkosAltLabel() {
        return dataFactory.getOWLAnnotationProperty(ALTLABEL.getIRI());
    }

    @Override
    public OWLEntity getRenamedResult(OWLEntity result, RenameMap renameMap) {
        return targetEntity;
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<OWLEntity> result) {
        return commitMessage;
    }
}
