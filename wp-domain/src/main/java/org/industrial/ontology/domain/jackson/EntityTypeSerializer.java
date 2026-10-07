package org.industrial.ontology.domain.jackson;



import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.semanticweb.owlapi.model.EntityType;

import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.jackson.EntityTypeSerializer}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Jun 2018
 */
public class EntityTypeSerializer extends StdSerializer<EntityType> {

    public EntityTypeSerializer() {
        super(EntityType.class);
    }

    @Override
    public void serialize(EntityType entityType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(entityType.getPrefixedName());
    }
}
