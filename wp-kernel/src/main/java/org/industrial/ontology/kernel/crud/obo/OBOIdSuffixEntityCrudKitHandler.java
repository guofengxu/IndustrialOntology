package org.industrial.ontology.kernel.crud.obo;



import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.crud.EntityCrudContext;
import org.industrial.ontology.kernel.crud.EntityCrudKitHandler;
import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.domain.crud.EntityCrudKitId;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityShortForm;
import org.industrial.ontology.domain.crud.oboid.OBOIdSuffixKit;
import org.industrial.ontology.domain.crud.oboid.OboIdSuffixSettings;
import org.industrial.ontology.domain.crud.oboid.UserIdRange;
import org.industrial.ontology.domain.lang.AnnotationAssertionDictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageVisitor;
import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.EntityType;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.obo.OBOIdSuffixEntityCrudKitHandler}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class OBOIdSuffixEntityCrudKitHandler implements EntityCrudKitHandler<OboIdSuffixSettings, OBOIdSession> {


    private static final IRI CREATED_BY = IRI.create("http://www.geneontology.org/formats/oboInOwl#created_by");

    private static final IRI CREATION_DATE = IRI.create("http://www.geneontology.org/formats/oboInOwl#creation_date");

    private long currentId = 0;

    private EntityCrudKitPrefixSettings prefixSettings;

    private OboIdSuffixSettings suffixSettings;

    @Nonnull
    private final OWLDataFactory dataFactory;

    private final Map<UserId, Long> userId2CurrentIdMap = Maps.newHashMap();

    private final Map<UserId, UserIdRange> userId2RangeEndMap;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex projectSignatureIndex;

    @Nonnull
    private final EntityIriPrefixResolver entityIriPrefixResolver;

    public OBOIdSuffixEntityCrudKitHandler(@Nonnull EntityCrudKitPrefixSettings prefixSettings,
                                           @Nonnull OboIdSuffixSettings suffixSettings,
                                           @Nonnull OWLDataFactory dataFactory,
                                           @Nonnull EntitiesInProjectSignatureByIriIndex projectSignatureIndex,
                                           @Nonnull EntityIriPrefixResolver entityIriPrefixResolver) {
        this.prefixSettings = checkNotNull(prefixSettings);
        this.suffixSettings = checkNotNull(suffixSettings);
        this.dataFactory = dataFactory;
        this.projectSignatureIndex = projectSignatureIndex;
        this.entityIriPrefixResolver = entityIriPrefixResolver;

        ImmutableMap.Builder<UserId, UserIdRange> builder = ImmutableMap.builder();
        for(UserIdRange range : suffixSettings.getUserIdRanges()) {
            userId2CurrentIdMap.put(range.getUserId(), range.getStart());
            builder.put(range.getUserId(), range);
        }
        userId2RangeEndMap = builder.build();
    }

    @Override
    public EntityCrudKitId getKitId() {
        return OBOIdSuffixKit.getId();
    }


    @Override
    public EntityCrudKitPrefixSettings getPrefixSettings() {
        return prefixSettings;
    }

    @Override
    public OboIdSuffixSettings getSuffixSettings() {
        return suffixSettings;
    }

    @Override
    public OBOIdSession createChangeSetSession() {
        return new OBOIdSession();
    }

    @Override
    public EntityCrudKitSettings<OboIdSuffixSettings> getSettings() {
        return EntityCrudKitSettings.get(prefixSettings, suffixSettings);
    }

    @Override
    public <E extends OWLEntity> E create(@Nonnull OBOIdSession session,
                                          @Nonnull EntityType<E> entityType,
                                          @Nonnull EntityShortForm shortForm,
                                          @Nonnull Optional<String> langTag,
                                          @Nonnull ImmutableList<OWLEntity> parents,
                                          @Nonnull EntityCrudContext context,
                                          @Nonnull OntologyChangeList.Builder<E> builder) {
        var targetOntology = context.getTargetOntologyId();
        var iri = getNextIRI(session, context.getUserId(), parents);
        var entity = dataFactory.getOWLEntity(entityType, iri);
        var declarationAxiom = dataFactory.getOWLDeclarationAxiom(entity);
        builder.add(AddAxiomChange.of(targetOntology, declarationAxiom));
        DictionaryLanguage language = context.getDictionaryLanguage();
        if(!shortForm.getShortForm().isBlank()) {
            language.accept(new DictionaryLanguageVisitor<>() {
                @Override
                public Object visit(@Nonnull AnnotationAssertionDictionaryLanguage language) {
                    IRI annotationPropertyIri = language.getAnnotationPropertyIri();
                    final OWLLiteral labellingLiteral = getLabellingLiteral(shortForm, langTag, context);
                    var ax = dataFactory.getOWLAnnotationAssertionAxiom(dataFactory.getOWLAnnotationProperty(
                            annotationPropertyIri), entity.getIRI(), labellingLiteral);
                    builder.add(AddAxiomChange.of(targetOntology, ax));
                    return null;
                }
            });
        }
        OWLAnnotationAssertionAxiom createdByAx = dataFactory.getOWLAnnotationAssertionAxiom(
                dataFactory.getOWLAnnotationProperty(CREATED_BY),
                entity.getIRI(),
                dataFactory.getOWLLiteral(context.getUserId().getUserName())
        );
        builder.add(AddAxiomChange.of(targetOntology, createdByAx));
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        String formattedNow = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OWLAnnotationAssertionAxiom createdAtAx = dataFactory.getOWLAnnotationAssertionAxiom(
                dataFactory.getOWLAnnotationProperty(CREATION_DATE),
                entity.getIRI(),
                dataFactory.getOWLLiteral(formattedNow)
        );
        builder.add(AddAxiomChange.of(targetOntology, createdAtAx));
        return entity;
    }



    private synchronized IRI getNextIRI(OBOIdSession session, UserId userId, ImmutableList<OWLEntity> parents
    ) {
        StringBuilder formatStringBuilder = new StringBuilder();
        for (int i = 0; i < suffixSettings.getTotalDigits(); i++) {
            formatStringBuilder.append("0");
        }
        NumberFormat numberFormat = new DecimalFormat(formatStringBuilder.toString());
        long currentId = getCurrentId(userId);
        while (true) {
            currentId++;
            if(!session.isSessionId(currentId)) {
                String shortName = numberFormat.format(currentId);
                var iriPrefix = entityIriPrefixResolver.getIriPrefix(prefixSettings, parents);
                IRI iri = IRI.create(iriPrefix + shortName);
                if (projectSignatureIndex.getEntitiesInSignature(iri).limit(1).count() == 0) {
                    session.addSessionId(currentId);
                    setCurrentId(userId, currentId);
                    return iri;
                }
            }
        }
    }

    private long getCurrentId(UserId userId) {
        Long currentIdForUser = userId2CurrentIdMap.get(userId);
        return Objects.requireNonNullElse(currentIdForUser, currentId);
    }

    private void setCurrentId(UserId userId, long currentId) {
        if (userId2CurrentIdMap.containsKey(userId)) {
            UserIdRange userIdRange = userId2RangeEndMap.get(userId);
            long limitForUser = userIdRange.getEnd();
            if(currentId > limitForUser) {
                throw new CannotGenerateFreshEntityIdForUserException(userIdRange);
            }
            userId2CurrentIdMap.put(userId, currentId);
        }
        else {
            this.currentId = currentId;
        }
    }

    private OWLLiteral getLabellingLiteral(EntityShortForm shortForm,
                                                  Optional<String> langTag,
                                                  EntityCrudContext context) {
        DictionaryLanguage dictionaryLanguage = context.getDictionaryLanguage();
        return dataFactory.getOWLLiteral(shortForm.getShortForm(), langTag.orElse(dictionaryLanguage.getLang()));
    }
}
