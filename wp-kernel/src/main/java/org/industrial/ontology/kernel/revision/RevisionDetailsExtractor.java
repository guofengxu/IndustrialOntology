package org.industrial.ontology.kernel.revision;



import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.axiom.AxiomSubjectProvider;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeVisitorEx;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;

import java.util.Objects;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.revision.RevisionDetails;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionDetailsExtractor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23 Apr 2018
 */
public class RevisionDetailsExtractor {

    private final AxiomSubjectProvider axiomSubjectProvider;

    public RevisionDetailsExtractor(AxiomSubjectProvider axiomSubjectProvider) {
        this.axiomSubjectProvider = axiomSubjectProvider;
    }

    public RevisionDetails extractRevisionDetails(Revision revision) {
        ImmutableList.Builder<RevisionDetails.ChangeDetails> builder = ImmutableList.builder();
        ImmutableSet.Builder<IRI> subjectsBuilder = ImmutableSet.builder();
        revision.getChanges().stream()
                .filter(OntologyChange::isAxiomChange)
                .peek(chg -> {
                    axiomSubjectProvider.getSubject(chg.getAxiomOrThrow())
                    .ifPresent(subject -> {
                        if(subject instanceof IRI) {
                            subjectsBuilder.add((IRI) subject);
                        }
                        else if(subject instanceof HasIRI) {
                            subjectsBuilder.add(((HasIRI) subject).getIRI());
                        }
                    });
                })
                .map(RevisionDetailsExtractor::toChangeDetails)
                .filter(Objects::nonNull)
                .forEach(builder::add);

        return new RevisionDetails(revision.getRevisionNumber().getValueAsInt(),
                                   revision.getTimestamp(),
                                   revision.getUserId(),
                                   revision.getHighLevelDescription(),
                                   subjectsBuilder.build(),
                                   builder.build());
    }


    private static RevisionDetails.ChangeDetails toChangeDetails(@Nonnull OntologyChange change) {
        return change.accept(MAPPING_VISITOR);
    }

    private static final OntologyChangeVisitorEx<RevisionDetails.ChangeDetails> MAPPING_VISITOR = new OntologyChangeVisitorEx<>() {

        @Override
        public RevisionDetails.ChangeDetails getDefaultReturnValue() {
            return null;
        }

        @Nonnull
        @Override
        public RevisionDetails.ChangeDetails visit(@Nonnull AddAxiomChange change) {
            return new RevisionDetails.ChangeDetails(RevisionDetails.ChangeOperation.ADD, change.getAxiom());
        }

        @Override
        public RevisionDetails.ChangeDetails visit(@Nonnull RemoveAxiomChange change) {
            return new RevisionDetails.ChangeDetails(RevisionDetails.ChangeOperation.REMOVE, change.getAxiom());
        }
    };

}
