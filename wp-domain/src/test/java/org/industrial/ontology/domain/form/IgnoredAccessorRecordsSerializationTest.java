package org.industrial.ontology.domain.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.form.data.EntityNameControlData;
import org.industrial.ontology.domain.form.data.NumberControlData;
import org.industrial.ontology.domain.form.data.TextControlData;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;
import org.industrial.ontology.domain.form.field.FormControlDescriptor;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.industrial.ontology.domain.form.field.NumberControlRange;
import org.industrial.ontology.domain.form.field.NumberControlType;
import org.industrial.ontology.domain.form.field.Optionality;
import org.industrial.ontology.domain.form.field.OwlPropertyBinding;
import org.industrial.ontology.domain.form.field.Repeatability;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.industrial.ontology.domain.frame.RelationshipTranslationOptions;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.AnyRelationshipPropertyCriteria;
import org.industrial.ontology.domain.match.AnyRelationshipValueCriteria;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.industrial.ontology.domain.match.RelationshipCriteria;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Round trips for records that pair a {@code @JsonIgnore}d {@code getX()} with a {@code @JsonProperty("x")} accessor
 * or creator parameter. Converting the legacy AutoValue classes to records made their {@code *Internal} accessors
 * public, which can make Jackson treat the whole property as ignored and drop it on deserialization.
 */
public class IgnoredAccessorRecordsSerializationTest {

    private static final IRI TARGET_ONTOLOGY = IRI.create("http://example.org/target-ontology");

    @Test
    public void shouldRoundTripFormSubjectFactoryDescriptorWithParentAndTargetOntology() throws IOException {
        var descriptor = FormSubjectFactoryDescriptor.get(EntityType.CLASS,
                                                          DataFactory.getOWLClass("http://example.org/Parent"),
                                                          Optional.of(TARGET_ONTOLOGY));
        JsonSerializationTestUtil.testSerialization(descriptor, FormSubjectFactoryDescriptor.class);
    }

    @Test
    public void shouldRoundTripFormSubjectFactoryDescriptorWithoutTargetOntology() throws IOException {
        var descriptor = FormSubjectFactoryDescriptor.get(EntityType.CLASS, DataFactory.getOWLThing(), Optional.empty());
        JsonSerializationTestUtil.testSerialization(descriptor, FormSubjectFactoryDescriptor.class);
    }

    @Test
    public void shouldWriteFormSubjectFactoryDescriptorInLegacyShape() throws IOException {
        var descriptor = FormSubjectFactoryDescriptor.get(EntityType.CLASS, DataFactory.getOWLThing(),
                                                          Optional.of(TARGET_ONTOLOGY));
        var mapper = new ObjectMapperProvider().get();
        JsonNode json = mapper.valueToTree(descriptor);
        assertThat(fieldNames(json), is(Set.of("entityType", "parent", "targetOntologyIri")));
        assertThat(json.get("targetOntologyIri").asText(), is(TARGET_ONTOLOGY.toString()));
    }

    @Test
    public void shouldReadLegacyFormSubjectFactoryDescriptorJson() throws IOException {
        var json = """
                {"entityType":"owl:Class",
                 "parent":{"type":"owl:Class","iri":"http://example.org/Parent"},
                 "targetOntologyIri":"http://example.org/target-ontology"}""";
        var expected = FormSubjectFactoryDescriptor.get(EntityType.CLASS,
                                                        DataFactory.getOWLClass("http://example.org/Parent"),
                                                        Optional.of(TARGET_ONTOLOGY));
        JsonSerializationTestUtil.testDeserialization(json, expected, FormSubjectFactoryDescriptor.class);
    }

    @Test
    public void shouldRoundTripTextControlData() throws IOException {
        var data = TextControlData.get(TextControlDescriptor.getDefault(), DataFactory.getOWLLiteral("Hello"));
        JsonSerializationTestUtil.testSerialization(data, TextControlData.class);
    }

    @Test
    public void shouldRoundTripNumberControlData() throws IOException {
        var descriptor = new NumberControlDescriptor("#.#", NumberControlRange.all(), NumberControlType.PLAIN, 6,
                                                     LanguageMap.empty());
        var data = NumberControlData.get(descriptor, DataFactory.getOWLLiteral(3.5));
        JsonSerializationTestUtil.testSerialization(data, NumberControlData.class);
    }

    @Test
    public void shouldRoundTripEntityNameControlData() throws IOException {
        var data = EntityNameControlData.get(EntityNameControlDescriptor.getDefault(),
                                             DataFactory.getOWLClass("http://example.org/A"));
        JsonSerializationTestUtil.testSerialization(data, EntityNameControlData.class);
    }

    @Test
    public void shouldRoundTripGridControlDescriptor() throws IOException {
        var column = GridColumnDescriptor.get(GridColumnId.get("12345678-1234-1234-1234-123456789abc"),
                                              Optionality.OPTIONAL,
                                              Repeatability.REPEATABLE_VERTICALLY,
                                              OwlPropertyBinding.get(DataFactory.getOWLAnnotationProperty(
                                                      "http://example.org/prop")),
                                              LanguageMap.of("en", "Column"),
                                              TextControlDescriptor.getDefault());
        var subjectFactory = FormSubjectFactoryDescriptor.get(EntityType.NAMED_INDIVIDUAL,
                                                              DataFactory.getOWLClass("http://example.org/Row"),
                                                              Optional.empty());
        var grid = GridControlDescriptor.get(ImmutableList.of(column), subjectFactory);
        JsonSerializationTestUtil.testSerialization(grid, FormControlDescriptor.class);
    }

    @Test
    public void shouldRoundTripRelationshipTranslationOptions() throws IOException {
        var criteria = RelationshipCriteria.get(AnyRelationshipPropertyCriteria.get(),
                                                AnyRelationshipValueCriteria.get());
        var options = RelationshipTranslationOptions.get(
                criteria, criteria, RelationshipTranslationOptions.RelationshipMinification.MINIMIZED_RELATIONSHIPS);
        JsonSerializationTestUtil.testSerialization(options, RelationshipTranslationOptions.class);
    }

    private static Set<String> fieldNames(JsonNode json) {
        var names = new TreeSet<String>();
        json.fieldNames().forEachRemaining(names::add);
        return names;
    }
}
