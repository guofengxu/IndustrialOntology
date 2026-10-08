/**
 * Merging uploaded ontologies into a project.
 *
 * <p>Ported as-is from the legacy packages {@code edu.stanford.bmir.protege.web.server.merge}
 * (diff an uploaded project against the project ontologies and patch the differences in) and
 * {@code edu.stanford.bmir.protege.web.server.merge_add} (add the axioms and annotations of selected
 * ontologies to an existing or a new ontology). docs/01 §3.3: it is only wired in P4.
 *
 * <p>The GWT dispatch handlers of those packages ({@code ComputeProjectMergeActionHandler},
 * {@code MergeUploadedProjectActionHandler}, {@code ExistingOntologyMergeAddActionHandler},
 * {@code GetAllOntologiesActionHandler} and {@code NewOntologyMergeAddActionHandler}) were not ported,
 * because the dispatch layer is not ported (docs/01 §4); their logic will be replaced by {@code wp-app}
 * services in P4. The legacy dispatch {@code ExecutionContext}, of which the patchers only used
 * {@code getUserId()}, is replaced by the {@link org.industrial.ontology.domain.core.UserId} itself.
 */
package org.industrial.ontology.kernel.io.merge;
