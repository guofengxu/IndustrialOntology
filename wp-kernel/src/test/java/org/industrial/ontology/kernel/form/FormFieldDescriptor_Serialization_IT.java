package org.industrial.ontology.kernel.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.form.ExpansionState;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;
import java.io.IOException;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.industrial.ontology.domain.form.field.FieldRun;
import org.industrial.ontology.domain.form.field.FormFieldDescriptor;
import org.industrial.ontology.domain.form.field.FormFieldId;
import org.industrial.ontology.domain.form.field.LineMode;
import org.industrial.ontology.domain.form.field.Optionality;
import org.industrial.ontology.domain.form.field.OwlClassBinding;
import org.industrial.ontology.domain.form.field.OwlPropertyBinding;
import org.industrial.ontology.domain.form.field.Repeatability;
import org.industrial.ontology.domain.form.field.StringType;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormFieldDescriptor_Serialization_IT}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-25
 */
public class FormFieldDescriptor_Serialization_IT {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapperProvider().get();
    }

    @Test
    public void shouldSerializeElementWithoutOwlBinding() throws IOException {
        var formElementDescriptor = FormFieldDescriptor.get(FormFieldId.get(UUID.randomUUID().toString()), null, LanguageMap.empty(), FieldRun.START, new TextControlDescriptor(LanguageMap.empty(), StringType.SIMPLE_STRING, LineMode.SINGLE_LINE, "", LanguageMap.empty()), Repeatability.NON_REPEATABLE, Optionality.REQUIRED, true, ExpansionState.COLLAPSED, LanguageMap.empty());
        var serialized = objectMapper.writeValueAsString(formElementDescriptor);
        var deserialized = objectMapper.readerFor(FormFieldDescriptor.class).readValue(serialized);
        assertThat(formElementDescriptor, is(deserialized));
    }

    @Test
    public void shouldSerializeElementWithOwlPropertyBinding() throws IOException {
        var formElementDescriptor = FormFieldDescriptor.get(FormFieldId.get(UUID.randomUUID().toString()), OwlPropertyBinding.get(new OWLObjectPropertyImpl(IRI.create("http://example.org/prop")), null), LanguageMap.empty(), FieldRun.START, new TextControlDescriptor(LanguageMap.empty(), StringType.SIMPLE_STRING, LineMode.SINGLE_LINE, "", LanguageMap.empty()), Repeatability.NON_REPEATABLE, Optionality.REQUIRED, true, ExpansionState.COLLAPSED, LanguageMap.empty());
        var serialized = objectMapper.writeValueAsString(formElementDescriptor);
        System.out.println(serialized);
        var deserialized = objectMapper.readerFor(FormFieldDescriptor.class).readValue(serialized);
        assertThat(deserialized, is(formElementDescriptor));
    }

    @Test
    public void shouldSerializeElementWithOwlClassBinding() throws IOException {
        var formElementDescriptor = FormFieldDescriptor.get(FormFieldId.get(UUID.randomUUID().toString()), OwlClassBinding.get(), LanguageMap.empty(), FieldRun.START, new TextControlDescriptor(LanguageMap.empty(), StringType.SIMPLE_STRING, LineMode.SINGLE_LINE, "", LanguageMap.empty()), Repeatability.NON_REPEATABLE, Optionality.REQUIRED, true, ExpansionState.COLLAPSED, LanguageMap.empty());
        var serialized = objectMapper.writeValueAsString(formElementDescriptor);
        System.out.println(serialized);
        var deserialized = objectMapper.readerFor(FormFieldDescriptor.class).readValue(serialized);
        assertThat(deserialized, is(formElementDescriptor));
    }

    @Test
    public void shouldParseWithNoOwlBinding() throws IOException {
        var serializedForm = "{\"id\":\"12345678-1234-1234-1234-123456789abc\",\"label\":{},\"elementRun\":\"START\",\"formControlDescriptor\":{\"type\":\"TEXT\",\"placeholder\":{},\"stringType\":\"SIMPLE_STRING\",\"lineMode\":\"SINGLE_LINE\",\"patternViolationErrorMessage\":{}},\"repeatability\":\"NON_REPEATABLE\",\"optionality\":\"REQUIRED\",\"help\":{}}";
        FormFieldDescriptor deserializedForm = objectMapper.readerFor(FormFieldDescriptor.class).readValue(serializedForm);
        assertThat(deserializedForm.getOwlBinding().isEmpty(), is(true));
    }
}
