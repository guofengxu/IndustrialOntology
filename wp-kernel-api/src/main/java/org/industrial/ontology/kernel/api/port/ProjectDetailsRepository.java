package org.industrial.ontology.kernel.api.port;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.project.ProjectDetails;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.ProjectDetailsRepository}.
 * <p>
 * Kernel port: the legacy class read from MongoDB inside the kernel. Here the kernel only sees the methods it
 * calls; wp-app provides the Mongo-backed implementation (docs/01 §1, §5.3).
 */
public interface ProjectDetailsRepository {

    Optional<ProjectDetails> findOne(@Nonnull ProjectId projectId);

    /** The languages used to render entity display names, most preferred first. */
    ImmutableList<DictionaryLanguage> getDisplayNameLanguages(@Nonnull ProjectId projectId);
}
