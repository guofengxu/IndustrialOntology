package org.industrial.ontology.kernel.form.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.kernel.form.FormRegionOrderingIndex;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptor;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import org.industrial.ontology.domain.form.data.GridRowDataDto;
import org.industrial.ontology.domain.form.field.FormControlDescriptor;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;
import org.industrial.ontology.domain.form.field.FormRegionOrderingDirection;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.form.field.Optionality;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.Repeatability;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.GridRowDataDtoComparatorFactory_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GridRowDataDtoComparatorFactoryTest {

    public static final Optionality REQUIRED = Optionality.REQUIRED;

    public static final Repeatability NON_REPEATABLE = Repeatability.NON_REPEATABLE;

    @Mock
    public LanguageMap columnLabel = LanguageMap.empty();

    @Mock
    public OwlBinding owlBinding;

    @Mock
    public FormSubjectFactoryDescriptor subjectFactoryDescriptor;

    private GridRowDataDtoComparatorFactory comparatorFactory;

    @Mock
    private GridCellDataDtoComparator gridCellDataDtoComparator;

    @Mock
    private FormRegionOrderingIndex orderingIndex;

    @BeforeEach
    public void setUp() throws Exception {
        comparatorFactory = new GridRowDataDtoComparatorFactory(gridCellDataDtoComparator, orderingIndex);
    }

    @Test
    public void shouldCreateComparatorForFirstColumn() {
        var columnId = generateColumnId();
        var textControlDescriptor = TextControlDescriptor.getDefault();
        var desc = createGridControlDescriptorWithColumns(ImmutableMap.of(columnId, textControlDescriptor));
        var comparator = (GridRowDtoByColumnIndexComparator) createComparatorOrderByColumnIds(desc, columnId);
        var columnIndex = comparator.getColumnIndex();
        assertThat(columnIndex, equalTo(0));
    }

    @Test
    public void shouldCreateComparatorForSecondColumn() {
        var column0Id = generateColumnId();
        var column1Id = generateColumnId();
        var textControlDescriptor = TextControlDescriptor.getDefault();
        var gridControlDescriptor = createGridControlDescriptorWithColumns(ImmutableMap.of(column0Id, textControlDescriptor, column1Id, textControlDescriptor));
        var comparator = (GridRowDtoByColumnIndexComparator) createComparatorOrderByColumnIds(gridControlDescriptor, column1Id);
        var columnIndex = comparator.getColumnIndex();
        assertThat(columnIndex, equalTo(1));
    }

    @Test
    public void shouldCreateComparatorForFirstColumnThenSecondColumn() {
        var column0Id = generateColumnId();
        var nestedColumn0Id = generateColumnId();
        var nestedColumn1Id = generateColumnId();
        var controlDescriptor = createGridControlDescriptorWithColumns(Map.of(nestedColumn0Id, TextControlDescriptor.getDefault(), nestedColumn1Id, TextControlDescriptor.getDefault()));
        var gridControlDescriptor = createGridControlDescriptorWithColumns(ImmutableMap.of(column0Id, controlDescriptor));
        var comparator = (GridRowDtoByColumnIndexComparator) createComparatorOrderByColumnIds(gridControlDescriptor, nestedColumn1Id);
        var columnIndex = comparator.getColumnIndex();
        assertThat(columnIndex, equalTo(0));
    }

    public Comparator<GridRowDataDto> createComparatorOrderByColumnIds(GridControlDescriptor gridControlDescriptor, GridColumnId... columnIds) {
        var orderBys = Stream.of(columnIds).map(columnId -> FormRegionOrdering.get(columnId, FormRegionOrderingDirection.ASC)).collect(toImmutableSet());
        when(orderingIndex.getOrderings()).thenReturn(orderBys);
        return comparatorFactory.get(gridControlDescriptor, Optional.empty());
    }

    private GridControlDescriptor createGridControlDescriptorWithColumns(Map<GridColumnId, FormControlDescriptor> colId2ControlDescriptor) {
        var columnDescriptors = colId2ControlDescriptor.entrySet().stream().map(k -> createGridColumnDescriptor(k.getValue(), k.getKey())).collect(toImmutableList());
        var descriptors = ImmutableList.copyOf(columnDescriptors);
        return createGridControlDescriptor(descriptors);
    }

    public GridControlDescriptor createGridControlDescriptor(ImmutableList<GridColumnDescriptor> columnDescriptors) {
        return GridControlDescriptor.get(columnDescriptors, subjectFactoryDescriptor);
    }

    private GridColumnDescriptor createGridColumnDescriptor(FormControlDescriptor formControlDescriptor, GridColumnId columnId) {
        return GridColumnDescriptor.get(columnId, REQUIRED, NON_REPEATABLE, owlBinding, columnLabel, formControlDescriptor);
    }

    public static GridColumnId generateColumnId() {
        return GridColumnId.get(UUID.randomUUID().toString());
    }
}
