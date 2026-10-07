package org.industrial.ontology.kernel.mansyntax.render;



import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.render.ItemStyleProvider}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 25/02/2014
 */
public interface ItemStyleProvider {

    Optional<String> getItemStyle(Object item);
}
