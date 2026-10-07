package org.industrial.ontology.domain.match.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.color.Color;
import org.industrial.ontology.domain.match.EntityIsNotDeprecatedCriteria;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.tag.TagId;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.StringReader;
import java.io.StringWriter;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.tag.Tag_Serialization_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 21 Jun 2018
 */
public class Tag_SerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapperProvider().get();
    }

    @Test
    public void shouldSerializeTag() throws Exception {
        Tag tag = Tag.get(TagId.createTagId(), ProjectId.get("12345678-1234-1234-1234-123456789abc"), "The label", "The description", Color.getRGB(10, 20, 30), Color.getRGB(200, 210, 220), ImmutableList.of(EntityIsNotDeprecatedCriteria.get()));
        StringWriter sw = new StringWriter();
        objectMapper.writeValue(sw, tag);
        System.out.println(sw.toString());
        Tag readTag = objectMapper.readValue(new StringReader(sw.toString()), Tag.class);
        MatcherAssert.assertThat(readTag, is(tag));
    }

    /**
     * Test to handle loading of tags that did not have previous criteria.
     */
    @Test
    public void shouldDeserializeTagWithMissingCriteriaField() throws Exception {
        Tag tag = Tag.get(TagId.getId("605bc497-fd7f-4338-b7c9-81cc3559c470"), ProjectId.get("12345678-1234-1234-1234-123456789abc"), "The label", "The description", Color.getRGB(10, 20, 30), Color.getRGB(200, 210, 220), ImmutableList.of());
        String tagJson = "{\"_id\":\"605bc497-fd7f-4338-b7c9-81cc3559c470\",\"projectId\":\"12345678-1234-1234-1234-123456789abc\",\"label\":\"The label\",\"description\":\"The description\",\"color\":\"#0a141e\",\"backgroundColor\":\"#c8d2dc\"}";
        Tag readTag = objectMapper.readValue(new StringReader(tagJson), Tag.class);
        MatcherAssert.assertThat(readTag, is(tag));
    }
}
