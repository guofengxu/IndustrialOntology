package org.industrial.ontology.kernel.change.description;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.RemovedAnnotation}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class RemovedAnnotation extends AbstractAnnotationChange {


    public static RemovedAnnotation get(@Nonnull IRI subject,
                                      @Nonnull OWLAnnotationProperty property,
                                      @Nonnull OWLAnnotationValue value) {
        return new AutoValue_RemovedAnnotation(subject,
                                             property,
                                             value);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "RemovedAnnotation";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Removed annotation (%s  %s) from %s",
                                      getProperty(),
                                      getValue(),
                                      getSubject());
    }
}
