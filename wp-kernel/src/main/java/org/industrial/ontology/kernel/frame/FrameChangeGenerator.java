package org.industrial.ontology.kernel.frame;



import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.kernel.render.RenderingManager;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.kernel.api.change.RemoveAxiomChange.of;
import static java.util.stream.Collectors.toList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;

import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.change.ReverseEngineeredChangeDescriptionGeneratorFactory;
import org.industrial.ontology.kernel.frame.translator.AnnotationPropertyFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.ClassFrame2FrameAxiomsTranslator;
import org.industrial.ontology.kernel.frame.translator.DataPropertyFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.NamedIndividualFrameTranslator;
import org.industrial.ontology.kernel.frame.translator.ObjectPropertyFrameTranslator;
import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import org.industrial.ontology.domain.frame.Frame;
import org.industrial.ontology.domain.frame.PlainAnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.industrial.ontology.domain.frame.PlainDataPropertyFrame;
import org.industrial.ontology.domain.frame.PlainNamedIndividualFrame;
import org.industrial.ontology.domain.frame.PlainObjectPropertyFrame;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.FrameChangeGenerator}.
 * <p>
 * Author: Matthew Horridge<br> Stanford University<br> Bio-Medical Informatics Research Group<br> Date: 14/01/2013
 */
public final class FrameChangeGenerator implements ChangeListGenerator<OWLEntity> {

    @Nonnull
    private final FrameUpdate frameUpdate;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final ReverseEngineeredChangeDescriptionGeneratorFactory factory;

    @Nonnull
    private final DefaultOntologyIdManager defaultOntologyIdManager;

    @Nonnull
    private final OntologyAxiomsIndex axiomsIndex;

    @Nonnull
    private final ObjectPropertyFrameTranslator objectPropertyFrameTranslator;

    @Nonnull
    private final DataPropertyFrameTranslator dataPropertyFrameTranslator;

    @Nonnull
    private final AnnotationPropertyFrameTranslator annotationPropertyFrameTranslator;

    @Nonnull
    private final NamedIndividualFrameTranslator namedIndividualFrameTranslator;

    @Nonnull
    private final RenderingManager renderingManager;

    @Nonnull
    private final ClassFrameProvider classFrameProvider;

    @Nonnull
    private ClassFrame2FrameAxiomsTranslator classFrame2FrameAxiomsTranslator;

    public FrameChangeGenerator(@Nonnull FrameUpdate frameUpdate,
                                @Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                @Nonnull ReverseEngineeredChangeDescriptionGeneratorFactory factory,
                                @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                                @Nonnull OntologyAxiomsIndex axiomsIndex,
                                @Nonnull ObjectPropertyFrameTranslator objectPropertyFrameTranslator,
                                @Nonnull DataPropertyFrameTranslator dataPropertyFrameTranslator,
                                @Nonnull AnnotationPropertyFrameTranslator annotationPropertyFrameTranslator,
                                @Nonnull NamedIndividualFrameTranslator namedIndividualFrameTranslator,
                                @Nonnull RenderingManager renderingManager,
                                @Nonnull ClassFrameProvider classFrameProvider,
                                @Nonnull ClassFrame2FrameAxiomsTranslator classFrame2FrameAxiomsTranslator) {
        this.frameUpdate = checkNotNull(frameUpdate);
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.factory = checkNotNull(factory);
        this.defaultOntologyIdManager = checkNotNull(defaultOntologyIdManager);
        this.axiomsIndex = checkNotNull(axiomsIndex);
        this.classFrameProvider = classFrameProvider;
        this.objectPropertyFrameTranslator = objectPropertyFrameTranslator;
        this.dataPropertyFrameTranslator = dataPropertyFrameTranslator;
        this.annotationPropertyFrameTranslator = annotationPropertyFrameTranslator;
        this.namedIndividualFrameTranslator = namedIndividualFrameTranslator;
        this.renderingManager = renderingManager;
        this.classFrame2FrameAxiomsTranslator = classFrame2FrameAxiomsTranslator;
    }

    @Override
    public OntologyChangeList<OWLEntity> generateChanges(ChangeGenerationContext context) {
        var builder = new OntologyChangeList.Builder<OWLEntity>();
        builder.addAll(createChanges());
        return builder.build(frameUpdate.getToFrame().getSubject());
    }

    private Set<OWLAxiom> getAxiomsForFrame(Frame<?> frame, Mode mode) {
        if(frame instanceof PlainClassFrame) {
            var classFrame = (PlainClassFrame) frame;
            return classFrame2FrameAxiomsTranslator.getAxioms(classFrame, mode);
        }
        else if(frame instanceof PlainObjectPropertyFrame) {
            var objectPropertyFrame = (PlainObjectPropertyFrame) frame;
            return objectPropertyFrameTranslator.getAxioms(objectPropertyFrame, mode);
        }
        else if(frame instanceof PlainDataPropertyFrame) {
            var dataPropertyFrame = (PlainDataPropertyFrame) frame;
            return dataPropertyFrameTranslator.getAxioms(dataPropertyFrame, mode);
        }
        else if(frame instanceof PlainAnnotationPropertyFrame) {
            var annotationPropertyFrame = (PlainAnnotationPropertyFrame) frame;
            return annotationPropertyFrameTranslator.getAxioms(annotationPropertyFrame, mode);

        }
        else if(frame instanceof PlainNamedIndividualFrame) {
            var namedIndividualFrame = (PlainNamedIndividualFrame) frame;
            return namedIndividualFrameTranslator.getAxioms(namedIndividualFrame, mode);

        }
        else {
            throw new RuntimeException("Cannot determine translator type");
        }
    }

    private Frame<?> getFrameForSubject(OWLEntity subject) {
        if(subject instanceof OWLClass) {
            return classFrameProvider.getFrame((OWLClass) subject, ClassFrameTranslationOptions.defaultOptions());
        }
        else if(subject instanceof OWLObjectProperty) {
            return objectPropertyFrameTranslator.getFrame((OWLObjectProperty) subject);
        }
        else if(subject instanceof OWLDataProperty) {
            return dataPropertyFrameTranslator.getFrame((OWLDataProperty) subject);
        }
        else if(subject instanceof OWLAnnotationProperty) {
            return annotationPropertyFrameTranslator.getFrame((OWLAnnotationProperty) subject);
        }
        else if(subject instanceof OWLNamedIndividual) {
            return namedIndividualFrameTranslator.getFrame((OWLNamedIndividual) subject);
        }
        else {
            throw new RuntimeException("Cannot determine translator type");
        }
    }

    public List<OntologyChange> createChanges() {
        // TODO: Consider axiom annotations!

        // TODO: Three way merge incase the frame has been modified "externally" and is different from the original
        // TODO: from frame


        // This looks like it is more complicated than necessary, however we need to consider the fact that
        // a frame may be generated from multiple axioms (note the minimal and maximal translation)

        var to = frameUpdate.getToFrame();
        var toSubject = to.getSubject();
        Frame serverFrame = getFrameForSubject(toSubject);
        if(serverFrame.equals(to)) {
            // Nothing to do
            return Collections.emptyList();
        }


        // Get the axioms that were consumed in the translation
        var fromAxioms = getAxiomsForFrame(frameUpdate.getFromFrame(), Mode.MAXIMAL);

        var ontologyIds = projectOntologiesIndex.getOntologyIds()
                                                .collect(toList());

        var toAxioms = getAxiomsForFrame(frameUpdate.getToFrame(), Mode.MINIMAL);


        var axiom2OntologyMap = LinkedHashMultimap.<OWLAxiom, OWLOntologyID>create();

        // Generate a map of existing axioms so we can ensure they stay in the correct place
        for(OWLOntologyID ontologyId : ontologyIds) {
            for(OWLAxiom fromAxiom : fromAxioms) {
                if(isContainedInOntology(fromAxiom, ontologyId)) {
                    axiom2OntologyMap.put(fromAxiom, ontologyId);
                }
            }
        }


        var mutatedOntologies = Sets.<OWLOntologyID>newLinkedHashSet();
        List<OntologyChange> changes = Lists.newArrayList();
        for(OWLAxiom fromAxiom : fromAxioms) {
            for(OWLOntologyID ontologyId : ontologyIds) {
                if(isContainedInOntology(fromAxiom, ontologyId) && !toAxioms.contains(fromAxiom)) {
                    mutatedOntologies.add(ontologyId);
                }
                // We need to add this here in case an axiom containing fresh entities has been added and then
                // removed (caused by a re-edit).  The fresh entities will get "grounded" and then the axiom added
                // hence, ontology.contains() won't work.
                changes.add(of(ontologyId, fromAxiom));
            }
        }


        for(OWLAxiom toAxiom : toAxioms) {
            Collection<OWLOntologyID> existingLocations = axiom2OntologyMap.get(toAxiom);
            if(existingLocations.isEmpty()) {
                // Fresh axiom to be placed somewhere
                if(mutatedOntologies.size() == 1) {
                    // Assume edit i.e. replacement of axiom in the same ontology.
                    changes.add(AddAxiomChange.of(mutatedOntologies.iterator()
                                                                              .next(), toAxiom));
                }
                else {
                    // Multiple ontologies were affected.  We now need to place the fresh axiom in the appropriate
                    // ontology
                    OWLOntologyID freshAxiomOntology = getFreshAxiomOntology(fromAxioms, ontologyIds);
                    changes.add(AddAxiomChange.of(freshAxiomOntology, toAxiom));
                }
            }
            else {
                // Ensure it is still in there
                for(OWLOntologyID ontId : existingLocations) {
                    changes.add(AddAxiomChange.of(ontId, toAxiom));
                }
            }
        }
        return changes;
    }

    private boolean isContainedInOntology(OWLAxiom axiom, OWLOntologyID ontologyId) {
        return axiomsIndex.containsAxiomIgnoreAnnotations(axiom, ontologyId);
    }

    private OWLOntologyID getFreshAxiomOntology(Set<OWLAxiom> fromAxioms, List<OWLOntologyID> importsClosure) {
        for(OWLOntologyID ontId : importsClosure) {
            for(OWLAxiom existingFrameAxiom : fromAxioms) {
                if(isContainedInOntology(existingFrameAxiom, ontId)) {
                    return ontId;
                }
            }
        }
        return defaultOntologyIdManager.getDefaultOntologyId();
    }

    @Override
    public OWLEntity getRenamedResult(OWLEntity result, RenameMap renameMap) {
        return renameMap.getRenamedEntity(result);
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<OWLEntity> result) {
        var rendering = renderingManager.getRendering(frameUpdate.getFromFrame().getSubject());
        return factory.get("Edited " + rendering.getBrowserText())
                      .generateChangeDescription(result);
    }
}
