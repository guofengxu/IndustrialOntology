package org.industrial.ontology.api.json;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Makes the REST API's JSON the same as the legacy JSON of the domain types (docs/01 §4): Spring Boot's object mapper
 * gets the Guava module, which the domain records' creators need for their immutable collections, and the OWL
 * serializers of the legacy {@code ObjectMapperProvider}, so that entities are written as
 * {@code {"type":"owl:Class","iri":…}} (07 4-6). Spring Boot already registers the Java time and JDK 8 modules and
 * ignores unknown properties, as the legacy mapper did.
 */
@Configuration(proxyBeanMethods = false)
public class JsonConfiguration {

    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }

    @Bean
    public Module owlModule() {
        return ObjectMapperProvider.createOwlModule(new OWLDataFactoryImpl());
    }
}
