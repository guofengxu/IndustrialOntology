package org.industrial.ontology.app.form.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.form.EntityFormSelector;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.domain.match.CompositeRootCriteria;
import org.industrial.ontology.domain.match.EntityIsDeprecatedCriteria;
import org.industrial.ontology.domain.match.MultiMatchType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormPersistenceIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static final FormId A = FormId.get("aaaaaaaa-0000-4000-8000-000000000001");

    private static final FormId B = FormId.get("aaaaaaaa-0000-4000-8000-000000000002");

    private static final FormId C = FormId.get("aaaaaaaa-0000-4000-8000-000000000003");

    private static MongoPersistenceTestContext context;

    private MongoEntityFormRepository forms;

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
        forms = context.bean(MongoEntityFormRepository.class);
    }

    @Test
    void shouldListAProjectsFormsInTheOrderTheyWereSet() {
        forms.setProjectFormDescriptors(PROJECT, List.of(form(C), form(A), form(B)));
        forms.setProjectFormDescriptors(OTHER_PROJECT, List.of(form(A)));

        assertThat(forms.findFormDescriptors(PROJECT)).extracting(FormDescriptor::getFormId).containsExactly(C, A, B);
        assertThat(forms.findFormDescriptors(ImmutableSet.of(A, B), PROJECT)).extracting(FormDescriptor::getFormId)
                                                                            .containsExactly(A, B);

        forms.setProjectFormDescriptors(PROJECT, List.of(form(B)));
        assertThat(forms.findFormDescriptors(PROJECT)).extracting(FormDescriptor::getFormId).containsExactly(B);
        assertThat(forms.findFormDescriptors(OTHER_PROJECT)).hasSize(1);
    }

    @Test
    void shouldKeepTheOrdinalOfASavedFormAndAppendNewOnes() {
        forms.setProjectFormDescriptors(PROJECT, List.of(form(A), form(B)));

        forms.saveFormDescriptor(PROJECT, form(C));
        forms.saveFormDescriptor(PROJECT, form(A));

        assertThat(forms.findFormDescriptors(PROJECT)).extracting(FormDescriptor::getFormId).containsExactly(A, B, C);
        assertThat(context.database().getCollection(MongoEntityFormRepository.COLLECTION).countDocuments())
                .isEqualTo(3);
    }

    @Test
    void shouldFindAndDeleteOneForm() {
        forms.setProjectFormDescriptors(PROJECT, List.of(form(A), form(B)));

        assertThat(forms.findFormDescriptor(PROJECT, B)).map(FormDescriptor::getFormId).contains(B);
        assertThat(forms.findFormDescriptor(OTHER_PROJECT, B)).isEmpty();

        forms.deleteFormDescriptor(PROJECT, B);
        assertThat(forms.findFormDescriptor(PROJECT, B)).isEmpty();
        assertThat(forms.findFormDescriptor(PROJECT, A)).isPresent();
    }

    @Test
    void shouldKeepOneSelectorPerForm() {
        var selectors = context.bean(MongoEntityFormSelectorRepository.class);
        var all = CompositeRootCriteria.get(ImmutableList.of(EntityIsDeprecatedCriteria.get()), MultiMatchType.ALL);
        var any = CompositeRootCriteria.get(ImmutableList.of(EntityIsDeprecatedCriteria.get()), MultiMatchType.ANY);

        selectors.save(EntityFormSelector.get(PROJECT, all, A));
        selectors.save(EntityFormSelector.get(PROJECT, any, A));
        selectors.save(EntityFormSelector.get(PROJECT, all, B));
        selectors.save(EntityFormSelector.get(OTHER_PROJECT, all, A));

        assertThat(selectors.findFormSelectors(PROJECT)).containsExactlyInAnyOrder(
                EntityFormSelector.get(PROJECT, any, A), EntityFormSelector.get(PROJECT, all, B));
    }

    private static FormDescriptor form(FormId formId) {
        return FormDescriptor.empty(formId);
    }
}
