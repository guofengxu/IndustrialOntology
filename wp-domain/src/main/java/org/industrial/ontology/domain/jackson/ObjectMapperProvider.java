package org.industrial.ontology.domain.jackson;



import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLDatatype;

import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.jackson.ObjectMapperProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Jun 2018
 */
public class ObjectMapperProvider implements Supplier<ObjectMapper> {

    @Nonnull
    private final OWLDataFactory dataFactory;

    public ObjectMapperProvider() {
        this.dataFactory = new OWLDataFactoryImpl();
    }

    @Override
    public ObjectMapper get() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new GuavaModule());
        SimpleModule module = new SimpleModule();
        module.addSerializer(OWLEntity.class, new OWLEntitySerializer());
        module.addSerializer(OWLProperty.class, new OWLEntitySerializer());
        module.addSerializer(new EntityTypeSerializer());
        module.addDeserializer(EntityType.class, new EntityTypeDeserializer());
        module.addDeserializer(OWLEntity.class, new OWLEntityDeserializer<>(dataFactory, OWLEntity.class));
        module.addDeserializer(OWLNamedIndividual.class, new OWLEntityDeserializer<>(dataFactory, OWLNamedIndividual.class));
        module.addDeserializer(OWLProperty.class, new OWLPropertyDeserializer<>(dataFactory, OWLProperty.class));
        module.addDeserializer(OWLObjectProperty.class, new OWLPropertyDeserializer<>(dataFactory, OWLObjectProperty.class));
        module.addDeserializer(OWLDataProperty.class, new OWLPropertyDeserializer<>(dataFactory, OWLDataProperty.class));
        module.addDeserializer(OWLAnnotationProperty.class, new OWLPropertyDeserializer<>(dataFactory, OWLAnnotationProperty.class));
        module.addDeserializer(OWLDatatype.class, new OWLEntityDeserializer<>(dataFactory, OWLDatatype.class));
        module.addDeserializer(OWLClass.class, new OWLClassDeserializer(dataFactory));
        module.addDeserializer(IRI.class, new IriDeserializer());
        module.addDeserializer(OWLAnnotationValue.class, new OWLAnnotationValueDeserializer(new OWLLiteralDeserializer(dataFactory),
                                                                                            new IriDeserializer()));
        module.addSerializer(OWLLiteral.class, new OWLLiteralSerializer());
        module.addDeserializer(OWLLiteral.class, new OWLLiteralDeserializer(dataFactory));
        module.addDeserializer(PrimitiveFormControlData.class, new FormControlValueDeserializer(dataFactory));
        module.addSerializer(IRI.class, new IriSerializer());
        mapper.registerModule(module);

        return mapper;
    }
}
