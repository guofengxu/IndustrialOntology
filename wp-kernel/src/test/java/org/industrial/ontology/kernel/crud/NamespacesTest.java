package org.industrial.ontology.kernel.crud;



import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.vocab.Namespaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.NamespacesTestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 16/04/2014
 */
public class NamespacesTest {

    /**
     * A test to ensure that Namespaces is compatible over different versions of the OWL API.  By default,
     * each prefix name does not end with a colon!
     */
    @Test
    public void shouldNotEndWithColon() {
        for(Namespaces ns : Namespaces.values()) {
            assertThat(ns.getPrefixName().endsWith(":"), is(false));
        }
    }


}
