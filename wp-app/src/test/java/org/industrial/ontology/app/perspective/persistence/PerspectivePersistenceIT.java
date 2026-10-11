package org.industrial.ontology.app.perspective.persistence;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.perspective.PerspectiveDescriptor;
import org.industrial.ontology.domain.perspective.PerspectiveId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PerspectivePersistenceIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final PerspectiveId CLASSES = PerspectiveId.get("aaaaaaaa-0000-4000-8000-000000000001");

    private static final PerspectiveId HISTORY = PerspectiveId.get("aaaaaaaa-0000-4000-8000-000000000002");

    private static MongoPersistenceTestContext context;

    private PerspectiveDescriptorRepository descriptors;

    private PerspectiveLayoutRepository layouts;

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
        descriptors = context.bean(PerspectiveDescriptorRepository.class);
        layouts = context.bean(PerspectiveLayoutRepository.class);
    }

    @Test
    void shouldKeepOneDescriptorListPerScope() {
        var system = PerspectiveDescriptorsDocument.get(ImmutableList.of(descriptor(CLASSES), descriptor(HISTORY)));
        var project = PerspectiveDescriptorsDocument.get(PROJECT, ImmutableList.of(descriptor(CLASSES)));
        var alice = PerspectiveDescriptorsDocument.get(PROJECT, ALICE, ImmutableList.of(descriptor(HISTORY)));
        var otherProject = PerspectiveDescriptorsDocument.get(OTHER_PROJECT, ImmutableList.of(descriptor(HISTORY)));
        List.of(system, project, alice, otherProject).forEach(descriptors::saveDescriptors);
        var aliceReordered = PerspectiveDescriptorsDocument.get(PROJECT, ALICE, ImmutableList.of(descriptor(CLASSES),
                                                                                                 descriptor(HISTORY)));
        descriptors.saveDescriptors(aliceReordered);

        assertThat(descriptors.findDescriptors()).contains(system);
        assertThat(descriptors.findDescriptors(PROJECT)).contains(project);
        assertThat(descriptors.findDescriptors(PROJECT, ALICE)).contains(aliceReordered);
        assertThat(descriptors.findProjectAndSystemDescriptors(PROJECT)).containsExactlyInAnyOrder(system, project);
        assertThat(context.database().getCollection(PerspectiveDescriptorRepository.COLLECTION).countDocuments())
                .isEqualTo(4);

        descriptors.dropAllDescriptors(PROJECT, ALICE);
        assertThat(descriptors.findDescriptors(PROJECT, ALICE)).isEmpty();
        assertThat(descriptors.findDescriptors(PROJECT)).contains(project);
    }

    @Test
    void shouldKeepOneLayoutPerScopeAndPerspective() {
        var nodes = JsonNodeFactory.instance;
        var builtIn = new PerspectiveLayoutDocument(null, null, CLASSES, nodes.objectNode().put("@type", "LeafNode"));
        var project = new PerspectiveLayoutDocument(PROJECT, null, CLASSES, null);
        var alice = new PerspectiveLayoutDocument(PROJECT, ALICE, CLASSES,
                                                  nodes.objectNode().put("@type", "ParentNode")
                                                       .put("direction", "column"));
        var aliceHistory = new PerspectiveLayoutDocument(PROJECT, ALICE, HISTORY, null);
        layouts.saveLayouts(List.of(builtIn, project, alice, aliceHistory));
        layouts.saveLayout(new PerspectiveLayoutDocument(PROJECT, ALICE, CLASSES, null));

        assertThat(layouts.findLayout(CLASSES)).contains(builtIn);
        assertThat(layouts.findLayout(PROJECT, CLASSES)).contains(project);
        assertThat(layouts.findLayout(PROJECT, ALICE, CLASSES))
                .hasValueSatisfying(layout -> assertThat(layout.layout()).isNull());

        layouts.dropLayout(PROJECT, ALICE, CLASSES);
        assertThat(layouts.findLayout(PROJECT, ALICE, CLASSES)).isEmpty();
        assertThat(layouts.findLayout(PROJECT, ALICE, HISTORY)).isPresent();

        layouts.dropAllLayouts(PROJECT, ALICE);
        assertThat(layouts.findLayout(PROJECT, ALICE, HISTORY)).isEmpty();
        assertThat(layouts.findLayout(PROJECT, CLASSES)).contains(project);
    }

    private static PerspectiveDescriptor descriptor(PerspectiveId perspectiveId) {
        return PerspectiveDescriptor.get(perspectiveId, LanguageMap.of("en", perspectiveId.getId()), false);
    }
}
