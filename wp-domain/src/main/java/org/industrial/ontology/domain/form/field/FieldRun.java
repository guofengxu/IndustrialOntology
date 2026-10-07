package org.industrial.ontology.domain.form.field;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FieldRun}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-07
 */
public enum FieldRun {

    START,

    CONTINUE;

    public boolean isStart() {
        return this == START;
    }
}
