package org.industrial.ontology.app.tag.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.tag.TagId;
import org.industrial.ontology.kernel.api.match.MatchingEngine;
import org.industrial.ontology.kernel.api.port.TagsManager;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * The kernel's {@link TagsManager} port for one project: {@code getTags} of the legacy {@code TagsManager}, with the
 * tags that the legacy {@code CriteriaBasedTagsManager} found by matching each tag's criteria against the entity.
 * The project's {@link MatchingEngine} comes from its kernel (see {@code ProjectPorts}).
 * <p>
 * The legacy manager kept the project's tags in memory until {@code setProjectTags} replaced them. Editing tags
 * belongs to the tag service (S8), so until then the tags are read from the database on every call, and a change made
 * elsewhere, by another instance or by hand, is seen at once.
 */
public class MongoTagsManager implements TagsManager {

    private final ProjectId projectId;

    private final TagRepository tagRepository;

    private final EntityTagsRepository entityTagsRepository;

    private final MatchingEngine matchingEngine;

    public MongoTagsManager(@Nonnull ProjectId projectId,
                            @Nonnull TagRepository tagRepository,
                            @Nonnull EntityTagsRepository entityTagsRepository,
                            @Nonnull MatchingEngine matchingEngine) {
        this.projectId = checkNotNull(projectId);
        this.tagRepository = checkNotNull(tagRepository);
        this.entityTagsRepository = checkNotNull(entityTagsRepository);
        this.matchingEngine = checkNotNull(matchingEngine);
    }

    /**
     * The tags assigned to the entity, then those whose criteria it matches, each once; assignments of tags that the
     * project no longer has are ignored.
     */
    @Override
    public Collection<Tag> getTags(@Nonnull OWLEntity entity) {
        checkNotNull(entity);
        var projectTags = tagRepository.findTags(projectId);
        Map<TagId, Tag> tagsById = projectTags.stream().collect(toMap(Tag::getTagId, Function.identity()));
        Stream<TagId> explicitTags = entityTagsRepository.findByEntity(projectId, entity)
                                                         .map(entityTags -> entityTags.getTags().stream())
                                                         .orElse(Stream.empty());
        Stream<TagId> criteriaBasedTags = projectTags.stream()
                                                     .filter(tag -> matchingEngine.matchesAny(entity,
                                                                                              tag.getCriteria()))
                                                     .map(Tag::getTagId);
        return Stream.concat(explicitTags, criteriaBasedTags)
                     .distinct()
                     .map(tagsById::get)
                     .filter(Objects::nonNull)
                     .collect(toList());
    }
}
