package org.industrial.ontology.kernel.form;



import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormDataBuilderSession}.
 * <p>
 * Formerly a Dagger scope; now documents that {@link EntityFrameFormDataDtoBuilderFactory} creates one instance of
 * the annotated class per request.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FormDataBuilderSession {
}
