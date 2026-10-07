package org.industrial.ontology.kernel.form;

import org.industrial.ontology.domain.frame.FrameComponentRenderer;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.match.MatchingEngine;
import org.industrial.ontology.kernel.form.data.EntityNameControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.FormControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.FormDataDtoComparator;
import org.industrial.ontology.kernel.form.data.GridCellDataDtoComparator;
import org.industrial.ontology.kernel.form.data.GridControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.GridRowDataDtoComparatorFactory;
import org.industrial.ontology.kernel.form.data.ImageControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.MultiChoiceControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.NumberControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.SingleChoiceControlDataDtoComparator;
import org.industrial.ontology.kernel.form.data.TextControlDataDtoComparator;
import org.industrial.ontology.kernel.frame.FrameComponentSessionRenderer;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds the object graph behind an {@link EntityFrameFormDataDtoBuilder} for one request.
 * <p>
 * Replaces the legacy Dagger subcomponent {@code EntityFrameFormDataComponent} and its
 * {@code EntityFrameFormDataModule} (docs/01 §3.3). The classes marked {@link FormDataBuilderSession} hold
 * per-request state (rendering and choice caches, the request's ordering, paging and filters), so every call to
 * {@link #create} builds a fresh graph with exactly one instance of each of them. Only the collaborators passed to
 * the constructor are shared between requests.
 */
public class EntityFrameFormDataDtoBuilderFactory {

    @Nonnull
    private final FrameComponentRenderer frameComponentRenderer;

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex;

    @Nonnull
    private final MatchingEngine matchingEngine;

    @Nonnull
    private final FormFilterMatcherFactory formFilterMatcherFactory;

    public EntityFrameFormDataDtoBuilderFactory(@Nonnull FrameComponentRenderer frameComponentRenderer,
                                                @Nonnull BindingValuesExtractor bindingValuesExtractor,
                                                @Nonnull EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex,
                                                @Nonnull MatchingEngine matchingEngine,
                                                @Nonnull FormFilterMatcherFactory formFilterMatcherFactory) {
        this.frameComponentRenderer = checkNotNull(frameComponentRenderer);
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.entitiesInProjectSignatureByIriIndex = checkNotNull(entitiesInProjectSignatureByIriIndex);
        this.matchingEngine = checkNotNull(matchingEngine);
        this.formFilterMatcherFactory = checkNotNull(formFilterMatcherFactory);
    }

    @Nonnull
    public EntityFrameFormDataDtoBuilder create(@Nonnull FormRegionOrderingIndex formRegionOrderingIndex,
                                                @Nonnull LangTagFilter langTagFilter,
                                                @Nonnull FormPageRequestIndex formPageRequestIndex,
                                                @Nonnull FormRegionFilterIndex formRegionFilterIndex) {
        checkNotNull(formRegionOrderingIndex);
        checkNotNull(langTagFilter);
        checkNotNull(formPageRequestIndex);
        checkNotNull(formRegionFilterIndex);

        var sessionRenderer = new FormDataBuilderSessionRenderer(
                new FrameComponentSessionRenderer(frameComponentRenderer));
        var primitiveRenderer = new PrimitiveFormControlDataDtoRenderer(entitiesInProjectSignatureByIriIndex,
                                                                        sessionRenderer);
        var choiceDescriptorCache = new ChoiceDescriptorCache(new ChoiceDescriptorDtoSupplier(
                new FixedListChoiceDescriptorDtoSupplier(primitiveRenderer),
                new DynamicListChoiceDescriptorDtoSupplier(matchingEngine, sessionRenderer),
                sessionRenderer));

        // Grid comparators and the grid/sub-form builders refer back to the graph; Dagger broke these cycles with
        // Providers, which become suppliers of references filled in below.
        var gridControlDataDtoComparator = new AtomicReference<GridControlDataDtoComparator>();
        var formControlDataDtoComparator = new FormControlDataDtoComparator(gridControlDataDtoComparator::get,
                                                                            new EntityNameControlDataDtoComparator(),
                                                                            new ImageControlDataDtoComparator(),
                                                                            new MultiChoiceControlDataDtoComparator(),
                                                                            new SingleChoiceControlDataDtoComparator(),
                                                                            FormDataDtoComparator::new,
                                                                            new NumberControlDataDtoComparator(),
                                                                            new TextControlDataDtoComparator());
        var gridRowDataDtoComparatorFactory = new GridRowDataDtoComparatorFactory(
                new GridCellDataDtoComparator(formControlDataDtoComparator), formRegionOrderingIndex);
        gridControlDataDtoComparator.set(new GridControlDataDtoComparator(gridRowDataDtoComparatorFactory));

        var formDataDtoBuilder = new AtomicReference<EntityFrameFormDataDtoBuilder>();
        var gridControlValuesBuilder = new GridControlValuesBuilder(
                bindingValuesExtractor,
                formDataDtoBuilder::get,
                sessionRenderer,
                formRegionOrderingIndex,
                langTagFilter,
                formPageRequestIndex,
                gridRowDataDtoComparatorFactory,
                formControlDataDtoComparator,
                formRegionFilterIndex,
                formFilterMatcherFactory,
                new FormRegionFilterPredicateManager(formFilterMatcherFactory, formRegionFilterIndex));
        formDataDtoBuilder.set(new EntityFrameFormDataDtoBuilder(
                sessionRenderer,
                new TextControlValuesBuilder(bindingValuesExtractor, new TextControlDataDtoComparator()),
                new NumberControlValuesBuilder(bindingValuesExtractor, new NumberControlDataDtoComparator()),
                new MultiChoiceControlValueBuilder(bindingValuesExtractor, primitiveRenderer),
                new SingleChoiceControlValuesBuilder(bindingValuesExtractor, primitiveRenderer, langTagFilter),
                new EntityNameControlValuesBuilder(bindingValuesExtractor,
                                                   entitiesInProjectSignatureByIriIndex,
                                                   sessionRenderer,
                                                   new EntityNameControlDataDtoComparator()),
                new ImageControlValuesBuilder(bindingValuesExtractor, new ImageControlDataDtoComparator()),
                gridControlValuesBuilder,
                new SubFormControlValuesBuilder(bindingValuesExtractor, formDataDtoBuilder::get),
                langTagFilter,
                formPageRequestIndex,
                formRegionFilterIndex,
                new FormDescriptorDtoTranslator(choiceDescriptorCache)));
        return formDataDtoBuilder.get();
    }
}
