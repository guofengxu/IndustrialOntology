package org.industrial.ontology.domain.form.field;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormRegionOrderingDirection}.
 */
public enum FormRegionOrderingDirection {

    @JsonProperty("asc")
    ASC(1),

    @JsonProperty("desc")
    DESC(-1);

    int dir;

    FormRegionOrderingDirection(int dir) {
        this.dir = dir;
    }

    public int getDir() {
        return dir;
    }

    public boolean isAscending() {
        return this == ASC;
    }
}
