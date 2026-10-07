package org.industrial.ontology.domain.match;



import org.junit.jupiter.api.Test;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyRelationshipPropertyCriteria_IT}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-04
 */
public class AnyRelationshipPropertyCriteria_IT {


        @Test
        public void shouldSerialize_AnyRelationshipPropertyCriteria() throws IOException {
            testSerialization(AnyRelationshipPropertyCriteria.get());
        }

    private static <V extends RelationshipPropertyCriteria> void testSerialization(V value) throws IOException {
        JsonSerializationTestUtil.testSerialization(value, RelationshipPropertyCriteria.class);
    }

}
