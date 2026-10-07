package org.industrial.ontology.kernel.crud;



import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.HasPlugins}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public interface HasPlugins<T> {

    List<T> getPlugins();
}
