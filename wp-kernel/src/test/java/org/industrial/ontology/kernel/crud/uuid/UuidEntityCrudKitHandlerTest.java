package org.industrial.ontology.kernel.crud.uuid;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.crud.ChangeSetEntityCrudSession;
import org.industrial.ontology.kernel.crud.EntityCrudContext;
import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.industrial.ontology.kernel.crud.PrefixedNameExpander;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityShortForm;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.lang.AnnotationAssertionDictionaryLanguage;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.industrial.ontology.kernel.OWLDeclarationAxiomMatcher.declarationFor;
import static org.industrial.ontology.kernel.OWLEntityMatcher.hasIRI;
import static org.industrial.ontology.kernel.OWLEntityMatcher.owlThing;
import static org.industrial.ontology.kernel.RdfsLabelWithLexicalValueAndLang.rdfsLabelWithLexicalValueAndLang;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.EntityType;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.uuid.UuidEntityCrudKitHandlerTestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 16/04/2014
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UuidEntityCrudKitHandlerTest {

    public static final String PREFIX = "http://stuff/";

    @Mock
    protected EntityCrudKitPrefixSettings prefixSettings;

    @Mock
    protected UuidSuffixSettings suffixSettings;

    @Mock
    protected EntityCrudContext crudContext;

    @Mock
    protected EntityShortForm entityShortForm;

    @Mock
    protected OWLOntology ontology;

    @Mock
    protected OntologyChangeList.Builder<OWLClass> builder;

    @Mock
    protected ChangeSetEntityCrudSession session;

    protected AnnotationAssertionDictionaryLanguage dictionaryLanguage;

    protected IRI annotationPropertyIri = OWLRDFVocabulary.RDFS_LABEL.getIRI();

    private UuidEntityCrudKitHandler handler;

    private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    @Mock
    private EntitiesInProjectSignatureByIriIndex entitiesInSignature;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private EntityIriPrefixResolver entityIriPrefixResolver;

    @BeforeEach
    public void setUp() throws Exception {
        dictionaryLanguage = AnnotationAssertionDictionaryLanguage.get(annotationPropertyIri, "en");
        when(crudContext.getTargetOntologyId()).thenReturn(ontologyId);
        when(crudContext.getDictionaryLanguage()).thenReturn(dictionaryLanguage);
        when(crudContext.getPrefixedNameExpander()).thenReturn(PrefixedNameExpander.builder().withNamespaces(Namespaces.values()).build());
        when(entityIriPrefixResolver.getIriPrefix(prefixSettings, ImmutableList.of())).thenReturn(PREFIX);
        handler = new UuidEntityCrudKitHandler(prefixSettings, suffixSettings, dataFactory, entitiesInSignature, entityIriPrefixResolver);
        when(entitiesInSignature.getEntitiesInSignature(any())).thenAnswer(invocation -> Stream.empty());
    }

    @Test
    public void shouldGetSuppliedPrefixSettings() {
        assertThat(handler.getPrefixSettings(), is(prefixSettings));
    }

    @Test
    public void shouldGetSuppliedSuffixSettings() {
        assertThat(handler.getSuffixSettings(), is(suffixSettings));
    }

    @Test
    public void shouldGetSettings() {
        var settings = handler.getSettings();
        assertThat(settings.getPrefixSettings(), is(prefixSettings));
        assertThat(settings.getSuffixSettings(), is(suffixSettings));
    }

    @Test
    public void shouldGetKitId() {
        var kitId = handler.getKitId();
        assertThat(kitId, is(suffixSettings.getKitId()));
    }

    @Test
    public void shouldCreateSession() {
        var session = handler.createChangeSetSession();
        assertThat(session, is(not(nullValue())));
    }

    @Test
    public void shouldAddDeclaration() {
        when(entityShortForm.getShortForm()).thenReturn("A");
        OWLClass cls = handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), ImmutableList.of(), crudContext, builder);
        var ontologyChangeCaptor = ArgumentCaptor.forClass(OntologyChange.class);
        verify(builder, atLeast(1)).add(ontologyChangeCaptor.capture());
        var addedAxioms = ontologyChangeCaptor.getAllValues().stream().map(OntologyChange::getAxiomOrThrow).filter(ax -> ax instanceof OWLDeclarationAxiom).map(ax -> (OWLDeclarationAxiom) ax).collect(Collectors.toList());
        assertThat(addedAxioms, hasItem(Matchers.is(declarationFor(cls))));
    }

    @Test
    public void shouldAddLabelEqualToSuppliedName() {
        String suppliedName = "MyLabel";
        when(entityShortForm.getShortForm()).thenReturn(suppliedName);
        handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), ImmutableList.of(), crudContext, builder);
        verifyHasLabelEqualTo(suppliedName, "en");
    }

    @Test
    public void shouldCreatedExpandedPrefixName() {
        when(entityShortForm.getShortForm()).thenReturn("owl:Thing");
        OWLClass cls = handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), ImmutableList.of(), crudContext, builder);
        assertThat(cls, is(owlThing()));
    }

    @Test
    public void shouldAddLabelEqualToPrefixedName() {
        String suppliedName = "owl:Thing";
        when(entityShortForm.getShortForm()).thenReturn(suppliedName);
        handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), ImmutableList.of(), crudContext, builder);
        verifyHasLabelEqualTo(suppliedName, "en");
    }

    @Test
    public void shouldCreateEntityWithAbsoluteIriIfSpecified() {
        String expectedIRI = "http://stuff.com/A";
        String shortForm = "<" + expectedIRI + ">";
        when(entityShortForm.getShortForm()).thenReturn(shortForm);
        OWLClass cls = handler.create(session, EntityType.CLASS, entityShortForm, Optional.of("en"), ImmutableList.of(), crudContext, builder);
        assertThat(cls, hasIRI(expectedIRI));
        verifyHasLabelEqualTo(expectedIRI, "en");
    }

    private void verifyHasLabelEqualTo(String label, String langTag) {
        var addAxiomCaptor = ArgumentCaptor.forClass(OntologyChange.class);
        verify(builder, atLeast(1)).add(addAxiomCaptor.capture());
        List<OWLAxiom> addedAxioms = addAxiomCaptor.getAllValues().stream().map(OntologyChange::getAxiomOrThrow).collect(Collectors.toList());
        assertThat(addedAxioms, hasItem(rdfsLabelWithLexicalValueAndLang(label, langTag)));
    }
}
