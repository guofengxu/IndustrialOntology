package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.form.ExpansionState;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import java.io.IOException;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import java.util.UUID;
import java.util.Optional;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.SubFormControlDescriptor_IT}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-09
 */
public class SubFormControlDescriptor_IT {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapperProvider().get();
    }

    @Test
    public void shouldSerializeAndDeserialize() throws IOException {
        var formDescriptor = new FormDescriptor(FormId.get("12345678-1234-1234-1234-123456789abc"), LanguageMap.of("en", "The sub form"), singletonList(FormFieldDescriptor.get(FormFieldId.get(UUID.randomUUID().toString()), OwlPropertyBinding.get(new OWLObjectPropertyImpl(OWLRDFVocabulary.RDFS_LABEL.getIRI()), null), LanguageMap.of("en", "The Label"), FieldRun.START, new TextControlDescriptor(LanguageMap.empty(), StringType.SIMPLE_STRING, LineMode.SINGLE_LINE, "Pattern", LanguageMap.empty()), Repeatability.NON_REPEATABLE, Optionality.REQUIRED, true, ExpansionState.COLLAPSED, LanguageMap.empty())), Optional.empty());
        SubFormControlDescriptor descriptor = new SubFormControlDescriptor(formDescriptor);
        var serialized = objectMapper.writeValueAsString(descriptor);
        System.out.println(serialized);
        var deserialized = objectMapper.readerFor(SubFormControlDescriptor.class).readValue(serialized);
        assertThat(deserialized, is(descriptor));
    }
}
