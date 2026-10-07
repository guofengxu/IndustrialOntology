package org.industrial.ontology.domain.match;



import com.fasterxml.jackson.annotation.JsonTypeName;
import org.junit.jupiter.api.Test;
import java.io.IOException;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyRelationshipValueCriteria_IT}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-04
 */
@JsonTypeName("AnyValue")
public class AnyRelationshipValueCriteria_IT {

    @Test
    public void shouldSerialize_AnyRelationshipPropertyCriteria() throws IOException {
        testSerialization(AnyRelationshipValueCriteria.get());
    }

    private static <V extends RelationshipValueCriteria> void testSerialization(V value) throws IOException {
        JsonSerializationTestUtil.testSerialization(value, RelationshipValueCriteria.class);
    }
}
