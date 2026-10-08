package org.industrial.ontology.app.tag.persistence;

import com.google.common.collect.ImmutableList;
import com.mongodb.MongoWriteException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.color.Color;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.tag.TagId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagPersistenceIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final TagId RED = TagId.getId("aaaaaaaa-0000-4000-8000-000000000001");

    private static final TagId BLUE = TagId.getId("aaaaaaaa-0000-4000-8000-000000000002");

    private static final OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl();

    private static final OWLClass PIZZA = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Pizza"));

    private static final OWLClass TOPPING = dataFactory.getOWLClass(IRI.create("http://example.org/pizza#Topping"));

    private static MongoPersistenceTestContext context;

    private TagRepository tags;

    private EntityTagsRepository entityTags;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        tags = context.bean(TagRepository.class);
        entityTags = context.bean(EntityTagsRepository.class);
    }

    @Test
    void shouldStoreTagsByIdWithUniqueLabelsPerProject() {
        tags.saveTags(List.of(tag(RED, PROJECT, "Red"), tag(BLUE, PROJECT, "Blue")));
        tags.saveTag(tag(RED, PROJECT, "Crimson"));

        assertThat(tags.findTags(PROJECT)).extracting(Tag::getLabel).containsExactlyInAnyOrder("Crimson", "Blue");
        assertThat(tags.findTagByTagId(RED)).map(Tag::getLabel).contains("Crimson");
        assertThat(tags.findTags(OTHER_PROJECT)).isEmpty();
        assertThatThrownBy(() -> tags.saveTag(tag(TagId.createTagId(), PROJECT, "Blue")))
                .isInstanceOf(MongoWriteException.class);

        tags.deleteTag(BLUE);
        assertThat(tags.findTagByTagId(BLUE)).isEmpty();
    }

    @Test
    void shouldReplaceTheTagsOfAnEntity() {
        entityTags.save(EntityTagsDocument.of(PROJECT, PIZZA, List.of(RED)));
        var id = entityTags.findByEntity(PROJECT, PIZZA).orElseThrow().id();

        entityTags.save(EntityTagsDocument.of(PROJECT, PIZZA, List.of(BLUE)));

        assertThat(entityTags.findByEntity(PROJECT, PIZZA)).hasValueSatisfying(stored -> {
            assertThat(stored.id()).isEqualTo(id);
            assertThat(stored.getTags()).containsExactly(BLUE);
        });
    }

    @Test
    void shouldAddAndRemoveTagsOnlyOnEntitiesThatHaveTags() {
        entityTags.save(EntityTagsDocument.of(PROJECT, PIZZA, List.of(RED)));

        entityTags.addTag(PROJECT, PIZZA, BLUE);
        entityTags.addTag(PROJECT, PIZZA, BLUE);
        // As in the legacy repository, adding a tag does not create the entity's tags.
        entityTags.addTag(PROJECT, TOPPING, BLUE);

        assertThat(entityTags.findByEntity(PROJECT, PIZZA).orElseThrow().getTags()).containsExactly(RED, BLUE);
        assertThat(entityTags.findByEntity(PROJECT, TOPPING)).isEmpty();

        entityTags.removeTag(PROJECT, PIZZA, RED);
        assertThat(entityTags.findByEntity(PROJECT, PIZZA).orElseThrow().getTags()).containsExactly(BLUE);
    }

    @Test
    void shouldRemoveATagFromEveryEntityOfTheProject() {
        entityTags.save(EntityTagsDocument.of(PROJECT, PIZZA, List.of(RED, BLUE)));
        entityTags.save(EntityTagsDocument.of(PROJECT, TOPPING, List.of(RED)));
        entityTags.save(EntityTagsDocument.of(OTHER_PROJECT, PIZZA, List.of(RED)));

        assertThat(entityTags.findByTagId(RED)).hasSize(3);
        entityTags.removeTag(PROJECT, RED);

        assertThat(entityTags.findAll(PROJECT)).containsOnlyKeys(PIZZA, TOPPING);
        assertThat(entityTags.findAll(PROJECT).get(PIZZA).getTags()).containsExactly(BLUE);
        assertThat(entityTags.findAll(PROJECT).get(TOPPING).getTags()).isEmpty();
        assertThat(entityTags.findByTagId(RED)).extracting(EntityTagsDocument::getProjectId)
                                               .containsExactly(OTHER_PROJECT);
    }

    private static Tag tag(TagId tagId, ProjectId projectId, String label) {
        return Tag.get(tagId, projectId, label, "", Color.getHex("#000000"), Color.getHex("#ffffff"),
                       ImmutableList.of());
    }
}
