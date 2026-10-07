package org.industrial.ontology.domain.match;




/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.SimpleStringCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
public interface SimpleStringCriteria extends LexicalValueCriteria {

    String IGNORE_CASE = "ignoreCase";

    String VALUE = "value";

    String getValue();

    boolean isIgnoreCase();
}
