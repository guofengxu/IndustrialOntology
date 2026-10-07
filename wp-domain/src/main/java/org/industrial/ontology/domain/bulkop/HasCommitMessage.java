package org.industrial.ontology.domain.bulkop;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.bulkop.HasCommitMessage}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 1 Oct 2018
 */
public interface HasCommitMessage {

    @Nonnull
    String getCommitMessage();
}
