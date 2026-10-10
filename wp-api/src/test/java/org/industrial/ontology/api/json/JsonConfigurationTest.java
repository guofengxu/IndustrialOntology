package org.industrial.ontology.api.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings;
import org.industrial.ontology.domain.crud.supplied.WhiteSpaceTreatment;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The REST API's object mapper reads and writes the domain types as the legacy {@code ObjectMapperProvider} did.
 */
class JsonConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(JsonConfiguration.class);

    @Test
    void entitiesShouldBeWrittenAsTheLegacyMapperWroteThem() {
        runner.run(context -> {
            var mapper = context.getBean(ObjectMapper.class);
            var pizza = new OWLDataFactoryImpl().getOWLClass(IRI.create("http://example.org/Pizza"));

            assertThat(mapper.writeValueAsString(pizza)).isEqualTo("{\"type\":\"owl:Class\","
                                                                           + "\"iri\":\"http://example.org/Pizza\"}");
        });
    }

    @Test
    void newEntitySettingsShouldRoundTrip() {
        runner.run(context -> {
            var mapper = context.getBean(ObjectMapper.class);
            var settings = EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get("http://example.org/m#",
                                                                                     ImmutableList.of()),
                                                     SuppliedNameSuffixSettings.get(
                                                             WhiteSpaceTreatment.TRANSFORM_TO_CAMEL_CASE));

            var json = mapper.writeValueAsString(settings);

            assertThat(json).contains("\"_class\":\"SuppliedName\"");
            assertThat(mapper.readValue(json, EntityCrudKitSettings.class)).isEqualTo(settings);
        });
    }

    @Test
    void displayNameSettingsShouldRoundTrip() {
        runner.run(context -> {
            var mapper = context.getBean(ObjectMapper.class);
            var settings = DisplayNameSettings.get(ImmutableList.of(DictionaryLanguage.rdfsLabel("zh")),
                                                   ImmutableList.of(DictionaryLanguage.localName()));

            assertThat(mapper.readValue(mapper.writeValueAsString(settings), DisplayNameSettings.class))
                    .isEqualTo(settings);
        });
    }
}
