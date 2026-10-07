package org.industrial.ontology.domain.bulkop;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.bulkop.Operation}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 27 Sep 2018
 */
public enum Operation {

    REPLACE("Replaced"),

    DELETE("Deleted"),

    AUGMENT("Added");

    private String printName;

    Operation(String printName) {
        this.printName = printName;
    }

    public String getPrintName() {
        return printName;
    }
}
