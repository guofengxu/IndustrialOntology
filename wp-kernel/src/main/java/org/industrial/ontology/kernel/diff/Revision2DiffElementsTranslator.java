package org.industrial.ontology.kernel.diff;



import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.domain.diff.DiffElement;
import org.industrial.ontology.domain.diff.DiffOperation;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;

import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeVisitorEx;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveImportChange;
import org.industrial.ontology.kernel.api.change.RemoveOntologyAnnotationChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.diff.Revision2DiffElementsTranslator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 26/02/15
 */
public class Revision2DiffElementsTranslator {

    private final OntologyChangeVisitorEx<DiffOperation> changeOperationVisitor;

    private final OntologyIRIShortFormProvider ontologyIRIShortFormProvider;

    @Nonnull
    private final DefaultOntologyIdManager defaultOntologyIdManager;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    public Revision2DiffElementsTranslator(@Nonnull OntologyIRIShortFormProvider ontologyIRIShortFormProvider,
                                           @Nonnull DefaultOntologyIdManager defaultOntologyIdManager,
                                           @Nonnull ProjectOntologiesIndex projectOntologiesIndex) {
        this.ontologyIRIShortFormProvider = checkNotNull(ontologyIRIShortFormProvider);
        this.defaultOntologyIdManager = checkNotNull(defaultOntologyIdManager);
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        changeOperationVisitor = new OntologyChangeVisitorEx<DiffOperation>() {
            @Nonnull
            @Override
            public DiffOperation visit(@Nonnull AddAxiomChange change) {
                return DiffOperation.ADD;
            }

            @Override
            public DiffOperation visit(@Nonnull RemoveAxiomChange change) {
                return DiffOperation.REMOVE;
            }

            @Override
            public DiffOperation visit(@Nonnull AddOntologyAnnotationChange change) {
                return DiffOperation.ADD;
            }

            @Override
            public DiffOperation visit(@Nonnull RemoveOntologyAnnotationChange change) {
                return DiffOperation.REMOVE;
            }

            @Override
            public DiffOperation visit(@Nonnull AddImportChange change) {
                return DiffOperation.ADD;
            }

            @Override
            public DiffOperation visit(@Nonnull RemoveImportChange change) {
                return DiffOperation.REMOVE;
            }

            @Override
            public DiffOperation getDefaultReturnValue() {
                return DiffOperation.ADD;
            }
        };
    }

    public List<DiffElement<String, OntologyChange>> getDiffElementsFromRevision(List<OntologyChange> revision) {
        final List<DiffElement<String, OntologyChange>> changeRecordElements = new ArrayList<>();
        for (final OntologyChange change : revision) {
            changeRecordElements.add(toElement(change));
        }
        return changeRecordElements;
    }

    private DiffElement<String, OntologyChange> toElement(OntologyChange changeRecord) {
        var ontologyID = changeRecord.getOntologyId();
        final String ontologyIRIShortForm;
        if(isRootOntologySingleton(ontologyID)) {
            ontologyIRIShortForm = "";
        }
        else if (ontologyID.isAnonymous()) {
            // This used to be possible, but isn't anymore.  Some projects may have anonymous ontologies, so we
            // still need to support this.
            ontologyIRIShortForm = "";
        }
        else {
            var ontologyIRI = ontologyID.getOntologyIRI();
            if (ontologyIRI.isPresent()) {
                ontologyIRIShortForm = ontologyIRIShortFormProvider.getShortForm(ontologyIRI.get());
            }
            else {
                ontologyIRIShortForm = "Anonymous Ontology";
            }

        }
        return new DiffElement<>(
                getDiffOperation(changeRecord),
                ontologyIRIShortForm,
                changeRecord);
    }

    private boolean isRootOntologySingleton(@Nonnull OWLOntologyID ontologyId) {
        return defaultOntologyIdManager.getDefaultOntologyId().equals(ontologyId)
                || projectOntologiesIndex.getOntologyIds().count() == 1;
    }

    private DiffOperation getDiffOperation(OntologyChange changeRecord) {
        return changeRecord.accept(changeOperationVisitor);
    }
}
