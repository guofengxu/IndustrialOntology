/**
 * Per-project ontology kernel: indexes, {@code ChangeManager}, {@code RevisionStore}, Lucene
 * dictionary, hierarchy providers, frame translation and rendering.
 *
 * <p>Deliberately Spring-free: project-level objects are built by {@code ProjectContextFactory},
 * not by the container, so a project can be loaded, evicted and replayed in isolation.
 */
package org.industrial.ontology.kernel;
