package org.industrial.ontology.kernel.change.description;



import com.google.auto.value.AutoValue;
import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedAnnotation}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
@AutoValue
public abstract class AddedAnnotation extends AbstractAnnotationChange {

    public static AddedAnnotation get(@Nonnull IRI subject,
                                      @Nonnull OWLAnnotationProperty property,
                                      @Nonnull OWLAnnotationValue value) {
        return new AutoValue_AddedAnnotation(subject,
                                             property,
                                             value);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedAnnotation";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added annotation (%s  %s) to %s",
                                      getProperty(),
                                      getValue(),
                                      getSubject());
    }
}
