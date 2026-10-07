package org.industrial.ontology.kernel.crud.uuid;

import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixKit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.uuid.UuidEntityCrudKitPluginTestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UuidEntityCrudKitPluginTest {

    private UuidEntityCrudKitPlugin plugin;

    @Mock
    private UuidSuffixKit suffixKit;

    private UuidEntityCrudKitHandlerFactory handlerFactory;

    @Mock
    private EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureIndex;

    @Mock
    private EntityIriPrefixResolver entityIriPrefixResolver;

    @BeforeEach
    public void setUp() {
        handlerFactory = new UuidEntityCrudKitHandlerFactory(OWLDataFactoryImpl::new, () -> entitiesInProjectSignatureIndex, () -> entityIriPrefixResolver);
        plugin = new UuidEntityCrudKitPlugin(suffixKit, handlerFactory);
    }

    @Test
    public void shouldGetEntityCrudKit() {
        var crudKit = plugin.getEntityCrudKit();
        assertThat(crudKit, is(suffixKit));
    }

    @Test
    public void shouldGetEntityCrudKitHandler() {
        var crudKitHandler = plugin.getEntityCrudKitHandler();
        assertThat(crudKitHandler, is(not(nullValue())));
    }

    @Test
    public void shouldGetDefaultSettings() {
        var defaultSettings = plugin.getDefaultSettings();
        assertThat(defaultSettings, is(not(nullValue())));
    }
}
