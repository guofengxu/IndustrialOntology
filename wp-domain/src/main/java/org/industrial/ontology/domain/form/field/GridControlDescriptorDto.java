package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;
import static com.google.common.collect.ImmutableList.toImmutableList;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.GridControlDescriptorDto}.
 */
public record GridControlDescriptorDto(@Nonnull ImmutableList<GridColumnDescriptorDto> columns, @Nullable FormSubjectFactoryDescriptor subjectFactoryDescriptorInternal) implements FormControlDescriptorDto {

    public GridControlDescriptorDto {
        Objects.requireNonNull(columns, "Null columns");
    }

    @Nonnull
    public static GridControlDescriptorDto get(@Nonnull ImmutableList<GridColumnDescriptorDto> columns, @Nullable FormSubjectFactoryDescriptor formSubjectFactoryDescriptor) {
        return new GridControlDescriptorDto(columns, formSubjectFactoryDescriptor);
    }

    @Nonnull
    public Optional<FormSubjectFactoryDescriptor> getSubjectFactoryDescriptor() {
        return Optional.ofNullable(getSubjectFactoryDescriptorInternal());
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public GridControlDescriptor toFormControlDescriptor() {
        return GridControlDescriptor.get(getColumns().stream().map(GridColumnDescriptorDto::toGridColumnDescriptor).collect(toImmutableList()), getSubjectFactoryDescriptorInternal());
    }

    public int getNestedColumnCount() {
        int count = 0;
        for (GridColumnDescriptorDto columnDescriptor : getColumns()) {
            count += columnDescriptor.getNestedColumnCount();
        }
        return count;
    }

    @JsonIgnore
    @Nonnull
    public Stream<GridColumnDescriptorDto> getLeafColumns() {
        return getColumns().stream().flatMap(GridColumnDescriptorDto::getLeafColumnDescriptors);
    }

    /**
     * Returns a map of leaf column Ids to top level columns in this grid.  If a column does not
     * have nested columns then the column will map to itself.
     */
    @JsonIgnore
    public ImmutableMap<GridColumnId, GridColumnId> getLeafColumnToTopLevelColumnMap() {
        ImmutableMap.Builder<GridColumnId, GridColumnId> builder = ImmutableMap.builder();
        getColumns().forEach(topLevelColumn -> {
            topLevelColumn.getLeafColumnDescriptors().map(GridColumnDescriptorDto::getId).forEach(leafColumnId -> {
                builder.put(leafColumnId, topLevelColumn.getId());
            });
        });
        return builder.build();
    }

    /**
     * Gets the index of the specified columnId.
     * @param columnId The {@link GridColumnId}
     * @return The column index of the specified column Id.  A value of -1 is returned if the
     * {@link GridColumnId} does not identify a column in this grid.
     */
    @JsonIgnore
    public int getColumnIndex(GridColumnId columnId) {
        ImmutableList<GridColumnDescriptorDto> columns = getColumns();
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getId().equals(columnId)) {
                return i;
            }
        }
        return -1;
    }

    @Nonnull
    public ImmutableList<GridColumnDescriptorDto> getColumns() {
        return columns;
    }

    @Nullable
    public FormSubjectFactoryDescriptor getSubjectFactoryDescriptorInternal() {
        return subjectFactoryDescriptorInternal;
    }
}
