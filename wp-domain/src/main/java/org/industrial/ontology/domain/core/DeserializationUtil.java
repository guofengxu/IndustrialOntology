package org.industrial.ontology.domain.core;



import javax.annotation.Nullable;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.DeserializationUtil}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-20
 */
public class DeserializationUtil {

    public static String nonNull(@Nullable String value) {
        if(value == null) {
            return "";
        }
        else {
            return value;
        }
    }
}
