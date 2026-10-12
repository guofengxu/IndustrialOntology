package org.industrial.ontology.api.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.industrial.ontology.domain.frame.AnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.ClassFrame;
import org.industrial.ontology.domain.frame.DataPropertyFrame;
import org.industrial.ontology.domain.frame.EntityFrame;
import org.industrial.ontology.domain.frame.NamedIndividualFrame;
import org.industrial.ontology.domain.frame.ObjectPropertyFrame;
import org.industrial.ontology.domain.frame.PropertyValue;
import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.search.SearchResultMatch;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * The JSON of the ontology editing and search endpoints (docs/02 §4). OWL entities, literals, plain frames, criteria
 * and dictionary languages keep the domain JSON of the legacy {@code ObjectMapperProvider} (07 4-6); rendered data,
 * whose domain classes carry no JSON mapping, is flattened here into display names.
 */
public final class OntologyJson {

    private OntologyJson() {
    }

    /**
     * An entity with its display name.
     */
    public record EntityDataDto(@Nonnull OWLEntity entity, @Nonnull String displayName, boolean deprecated) {

        @Nonnull
        public static EntityDataDto of(@Nonnull OWLEntityData data) {
            return new EntityDataDto(data.getEntity(), data.getBrowserText(), data.isDeprecated());
        }

        @Nonnull
        static List<EntityDataDto> sorted(@Nonnull Collection<? extends OWLEntityData> data) {
            return data.stream()
                       .map(EntityDataDto::of)
                       .sorted(Comparator.comparing(EntityDataDto::displayName, String.CASE_INSENSITIVE_ORDER))
                       .toList();
        }
    }

    public record TagDto(@Nonnull String tagId,
                         @Nonnull String label,
                         @Nonnull String color,
                         @Nonnull String backgroundColor) {

        static TagDto of(Tag tag) {
            return new TagDto(tag.getTagId().getId(), tag.getLabel(), tag.getColor().getHex(),
                              tag.getBackgroundColor().getHex());
        }
    }

    /**
     * An entity as lists show it: display name, deprecation, tags and open comments.
     */
    public record EntityNodeDto(@Nonnull OWLEntity entity,
                                @Nonnull String displayName,
                                boolean deprecated,
                                @Nonnull List<TagDto> tags,
                                int openCommentCount) {

        @Nonnull
        public static EntityNodeDto of(@Nonnull EntityNode node) {
            return new EntityNodeDto(node.getEntity(),
                                     node.getBrowserText(),
                                     node.isDeprecated(),
                                     node.getTags().stream().map(TagDto::of).toList(),
                                     node.getOpenCommentCount());
        }
    }

    /**
     * A hierarchy node: an entity node that says whether it has children (docs/02 §4.2 named a child count; the
     * hierarchy only knows whether there are any).
     */
    public record GraphNodeDto(@Nonnull OWLEntity entity,
                               @Nonnull String displayName,
                               boolean deprecated,
                               @Nonnull List<TagDto> tags,
                               int openCommentCount,
                               boolean leaf) {

        @Nonnull
        public static GraphNodeDto of(@Nonnull GraphNode<EntityNode> node) {
            var entity = EntityNodeDto.of(node.userObject());
            return new GraphNodeDto(entity.entity(), entity.displayName(), entity.deprecated(), entity.tags(),
                                    entity.openCommentCount(), node.sink());
        }
    }

    /**
     * A page of results (docs/02 §1); page numbers start at 1.
     */
    public record PageDto<T>(int pageNumber, int pageCount, int pageSize, long totalElements,
                             @Nonnull List<T> pageElements) {

        @Nonnull
        public static <S, T> PageDto<T> of(@Nonnull Page<S> page, @Nonnull Function<? super S, ? extends T> mapper) {
            return new PageDto<>(page.getPageNumber(),
                                 page.getPageCount(),
                                 page.getPageSize(),
                                 page.getTotalElements(),
                                 page.getPageElements().stream().<T>map(mapper).toList());
        }
    }

    /**
     * A rendered value of a frame: an entity, a literal or an IRI that names no entity.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PrimitiveDto(@Nonnull String kind,
                               @Nullable OWLEntity entity,
                               @Nullable OWLLiteral literal,
                               @Nullable String iri,
                               @Nonnull String displayName,
                               boolean deprecated) {

        @Nonnull
        public static PrimitiveDto of(@Nonnull OWLPrimitiveData data) {
            var entity = data.asEntity();
            if (entity.isPresent()) {
                return new PrimitiveDto("entity", entity.get(), null, null, data.getBrowserText(),
                                        data.isDeprecated());
            }
            var literal = data.asLiteral();
            if (literal.isPresent()) {
                return new PrimitiveDto("literal", null, literal.get(), null, data.getBrowserText(), false);
            }
            return new PrimitiveDto("iri", null, null, data.asIRI().map(Object::toString).orElse(""),
                                    data.getBrowserText(), false);
        }
    }

    public record PropertyValueDto(@Nonnull PrimitiveDto property, @Nonnull PrimitiveDto value,
                                   @Nonnull String state) {

        @Nonnull
        public static PropertyValueDto of(@Nonnull PropertyValue value) {
            return new PropertyValueDto(PrimitiveDto.of(value.getProperty()), PrimitiveDto.of(value.getValue()),
                                        value.getState().name());
        }

        static List<PropertyValueDto> list(Collection<? extends PropertyValue> values) {
            return values.stream().map(PropertyValueDto::of).toList();
        }
    }

    /**
     * A rendered frame (docs/02 §4.3), with the sections of its kind; {@code kind} is the type name of the plain
     * frame. The parents of an individual are its types.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record RenderedFrameDto(@Nonnull String kind,
                                   @Nonnull EntityDataDto subject,
                                   @Nullable List<EntityDataDto> parents,
                                   @Nonnull List<PropertyValueDto> propertyValues,
                                   @Nullable List<EntityDataDto> domains,
                                   @Nullable List<EntityDataDto> ranges,
                                   @Nullable List<String> characteristics,
                                   @Nullable List<EntityDataDto> inverses,
                                   @Nullable Boolean functional,
                                   @Nullable List<EntityDataDto> sameIndividuals) {

        @Nonnull
        public static RenderedFrameDto of(@Nonnull EntityFrame<?> frame) {
            if (frame instanceof ClassFrame classFrame) {
                return new RenderedFrameDto("ClassFrame", EntityDataDto.of(classFrame.subject()),
                                            EntityDataDto.sorted(classFrame.classEntries()),
                                            PropertyValueDto.list(classFrame.propertyValues()),
                                            null, null, null, null, null, null);
            } else if (frame instanceof ObjectPropertyFrame property) {
                return new RenderedFrameDto("ObjectPropertyFrame", EntityDataDto.of(property.subject()), null,
                                            PropertyValueDto.list(property.annotationPropertyValues()),
                                            EntityDataDto.sorted(property.domains()),
                                            EntityDataDto.sorted(property.ranges()),
                                            property.characteristics().stream().map(Enum::name).sorted().toList(),
                                            EntityDataDto.sorted(property.inverseProperties()),
                                            null, null);
            } else if (frame instanceof DataPropertyFrame property) {
                return new RenderedFrameDto("DataPropertyFrame", EntityDataDto.of(property.subject()), null,
                                            PropertyValueDto.list(property.propertyValues()),
                                            EntityDataDto.sorted(property.domains()),
                                            EntityDataDto.sorted(property.ranges()),
                                            null, null, property.functional(), null);
            } else if (frame instanceof AnnotationPropertyFrame property) {
                return new RenderedFrameDto("AnnotationPropertyFrame", EntityDataDto.of(property.subject()), null,
                                            PropertyValueDto.list(property.propertyValues()),
                                            EntityDataDto.sorted(property.domains()),
                                            EntityDataDto.sorted(property.ranges()),
                                            null, null, null, null);
            } else if (frame instanceof NamedIndividualFrame individual) {
                return new RenderedFrameDto("NamedIndividualFrame", EntityDataDto.of(individual.subject()),
                                            EntityDataDto.sorted(individual.classes()),
                                            PropertyValueDto.list(individual.propertyValues()),
                                            null, null, null, null, null,
                                            EntityDataDto.sorted(individual.sameIndividuals()));
            }
            throw new IllegalArgumentException("Unknown frame " + frame.getClass().getName());
        }
    }

    public record PositionDto(int start, int end) {
    }

    /**
     * Where a name matched a search.
     */
    public record MatchDto(@Nonnull DictionaryLanguage language, @Nonnull String value,
                           @Nonnull List<PositionDto> positions) {

        @Nonnull
        public static MatchDto of(@Nonnull SearchResultMatch match) {
            return new MatchDto(match.language(),
                                match.value(),
                                match.positions()
                                     .stream()
                                     .map(position -> new PositionDto(position.start(), position.end()))
                                     .toList());
        }
    }

    /**
     * The revision that a change was recorded in.
     *
     * @param timestamp ISO-8601, UTC
     */
    public record RevisionDto(long number, @Nonnull String timestamp, @Nonnull String userId) {

        @Nonnull
        public static RevisionDto of(@Nonnull Revision revision) {
            return new RevisionDto(revision.getRevisionNumber().getValue(),
                                   Instant.ofEpochMilli(revision.getTimestamp()).toString(),
                                   revision.getUserId().getUserName());
        }
    }

    /**
     * What a change returns (docs/02 §1): the result, the revision ({@code null} when nothing changed) and the
     * project's event tag after the change, which the event stream of S8 counts from.
     */
    public record ChangeOutcomeDto<T>(@Nonnull T result, @Nullable RevisionDto revision, int eventTag) {

        @Nonnull
        public static <S, T> ChangeOutcomeDto<T> of(@Nonnull ChangeOutcome<S> outcome,
                                                   @Nonnull Function<? super S, ? extends T> mapper) {
            return new ChangeOutcomeDto<>(mapper.apply(outcome.result()),
                                          outcome.revision().map(RevisionDto::of).orElse(null),
                                          outcome.eventTag().getOrdinal());
        }
    }
}
