package org.industrial.ontology.kernel.index;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLAxiom;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ProjectOntologiesIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-06
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectOntologiesIndexTest {

    @Mock
    private OWLOntologyID rootOntologyId;

    private ProjectOntologiesIndex impl;

    @Mock
    private OWLAxiom axiom;

    @BeforeEach
    public void setUp() {
        impl = new ProjectOntologiesIndex();
        impl.applyChanges(ImmutableList.of(AddAxiomChange.of(rootOntologyId, axiom)));
    }

    @Test
    public void shouldReturnStreamOfReferencedOntologyId() {
        var ontologyIdStream = impl.getOntologyIds();
        var ontologyIds = ontologyIdStream.collect(Collectors.toSet());
        assertThat(ontologyIds, contains(rootOntologyId));
    }

    @Test
    public void shouldOnlyContainReferencedOntologies() {
        impl.applyChanges(ImmutableList.of(RemoveAxiomChange.of(rootOntologyId, axiom)));
        assertThat(impl.getOntologyIds().count(), is(0L));
    }
}
