package org.industrial.ontology.kernel.crud.uuid;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.util.IdUtil;
import org.industrial.ontology.domain.crud.uuid.UuidFormat;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.lang.AnnotationAssertionDictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageVisitor;
import javax.annotation.Nonnull;
import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.crud.ChangeSetEntityCrudSession;
import org.industrial.ontology.kernel.crud.EmptyChangeSetEntityCrudSession;

import org.industrial.ontology.kernel.crud.EntityCrudContext;
import org.industrial.ontology.kernel.crud.EntityCrudKitHandler;
import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;

import org.industrial.ontology.kernel.crud.IRIParser;
import org.industrial.ontology.kernel.crud.PrefixedNameExpander;
import org.industrial.ontology.domain.crud.EntityCrudKitId;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityShortForm;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.EntityType;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.uuid.UuidEntityCrudKitHandler}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/08/2013
 */
public class UuidEntityCrudKitHandler implements EntityCrudKitHandler<UuidSuffixSettings, ChangeSetEntityCrudSession> {

    private final EntityCrudKitPrefixSettings prefixSettings;

    private final UuidSuffixSettings suffixSettings;

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitiesInSignature;

    @Nonnull
    private final EntityIriPrefixResolver entityIriPrefixResolver;

    public UuidEntityCrudKitHandler(@Nonnull EntityCrudKitPrefixSettings prefixSettings,
                                    @Nonnull UuidSuffixSettings uuidSuffixKitSettings,
                                    OWLDataFactory dataFactory,
                                    @Nonnull EntitiesInProjectSignatureByIriIndex entitiesInSignature,
                                    @Nonnull EntityIriPrefixResolver entityIriPrefixResolver) {
        this.prefixSettings = checkNotNull(prefixSettings);
        this.suffixSettings = checkNotNull(uuidSuffixKitSettings);
        this.dataFactory = checkNotNull(dataFactory);
        this.entitiesInSignature = checkNotNull(entitiesInSignature);
        this.entityIriPrefixResolver = checkNotNull(entityIriPrefixResolver);
    }

    @Override
    public EntityCrudKitId getKitId() {
        return suffixSettings.getKitId();
    }

    @Override
    public ChangeSetEntityCrudSession createChangeSetSession() {
        return EmptyChangeSetEntityCrudSession.get();
    }

    @Override
    public EntityCrudKitPrefixSettings getPrefixSettings() {
        return prefixSettings;
    }

    @Override
    public UuidSuffixSettings getSuffixSettings() {
        return suffixSettings;
    }

    @Override
    public EntityCrudKitSettings<UuidSuffixSettings> getSettings() {
        return EntityCrudKitSettings.get(prefixSettings, suffixSettings);
    }

    @Override
    public <E extends OWLEntity> E create(@Nonnull ChangeSetEntityCrudSession session,
                                          @Nonnull EntityType<E> entityType,
                                          @Nonnull final EntityShortForm shortForm,
                                          @Nonnull Optional<String> langTag,
                                          @Nonnull ImmutableList<OWLEntity> parents,
                                          @Nonnull final EntityCrudContext context,
                                          @Nonnull final OntologyChangeList.Builder<E> builder) {
        var targetOntology = context.getTargetOntologyId();
        var suppliedName = shortForm.getShortForm();
        var parsedIRI = new IRIParser().parseIRI(suppliedName);
        final IRI entityIRI;
        final OWLLiteral labellingLiteral;
        var dictionaryLanguage = context.getDictionaryLanguage();
        if(parsedIRI.isPresent()) {
            entityIRI = parsedIRI.get();
            labellingLiteral = getLabellingLiteral(entityIRI.toString(), langTag, dictionaryLanguage);
        }
        else {
            var prefixedNameExpander = context.getPrefixedNameExpander();
            var iriPrefix = entityIriPrefixResolver.getIriPrefix(prefixSettings, parents);
            entityIRI = getIRI(iriPrefix, suppliedName, prefixedNameExpander);
            labellingLiteral = getLabellingLiteral(suppliedName, langTag, dictionaryLanguage);
        }
        var entity = dataFactory.getOWLEntity(entityType, entityIRI);
        builder.add(AddAxiomChange.of(targetOntology, dataFactory.getOWLDeclarationAxiom(entity)));

        if(!suppliedName.isBlank()) {
            dictionaryLanguage.accept(new DictionaryLanguageVisitor<Object>() {
                @Override
                public Object visit(@Nonnull AnnotationAssertionDictionaryLanguage language) {
                    var annotationPropertyIri = language.getAnnotationPropertyIri();
                    var ax = dataFactory.getOWLAnnotationAssertionAxiom(dataFactory.getOWLAnnotationProperty(annotationPropertyIri), entity.getIRI(), labellingLiteral);
                    builder.add(AddAxiomChange.of(targetOntology, ax));
                    return null;
                }
            });

        }
        return entity;
    }

    private IRI getIRI(String prefix, String suppliedName, PrefixedNameExpander prefixedNameExpander) {
        var expandedPrefixName = prefixedNameExpander.getExpandedPrefixName(suppliedName);
        return expandedPrefixName.orElseGet(() -> createIRI(prefix));
    }


    private IRI createIRI(String base) {
        while (true) {
            var suffix = getUuid();
            var iri = IRI.create(base + suffixSettings.getIdPrefix() + suffix);
            var inSig = entitiesInSignature.getEntitiesInSignature(iri).limit(1).count() == 1;
            if(!inSig) {
                return iri;
            }
        }
    }

    private String getUuid() {
        if(suffixSettings.getUuidFormat() == UuidFormat.BASE62) {
            return IdUtil.getBase62UUID();
        }
        else {
            return IdUtil.getUUID();
        }
    }


    private OWLLiteral getLabellingLiteral(String suppliedName, Optional<String> langTag, DictionaryLanguage dictionaryLanguage) {
        return dataFactory.getOWLLiteral(suppliedName, langTag.orElse(dictionaryLanguage.getLang()));
    }

}
