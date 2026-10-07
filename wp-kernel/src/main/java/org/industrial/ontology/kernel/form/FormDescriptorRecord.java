package org.industrial.ontology.kernel.form;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormDescriptorRecord}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-04
 */
public record FormDescriptorRecord(@JsonProperty(FormDescriptorRecord.PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(FormDescriptorRecord.FORM_DESCRIPTOR) @Nonnull FormDescriptor formDescriptor, @JsonProperty(FormDescriptorRecord.ORDINAL) @Nonnull Integer ordinal) implements Comparable<FormDescriptorRecord> {

    public FormDescriptorRecord {
        Objects.requireNonNull(projectId, "Null projectId");
        Objects.requireNonNull(formDescriptor, "Null formDescriptor");
        Objects.requireNonNull(ordinal, "Null ordinal");
    }

    private static final Comparator<FormDescriptorRecord> comparingByOrdinal = Comparator.comparing(FormDescriptorRecord::getOrdinal);

    public static final String PROJECT_ID = "projectId";

    public static final String FORM_DESCRIPTOR = "formDescriptor";

    public static final String ORDINAL = "ordinal";

    @JsonCreator
    public static FormDescriptorRecord get(@JsonProperty(PROJECT_ID) @Nonnull ProjectId projectId, @JsonProperty(FORM_DESCRIPTOR) FormDescriptor formDescriptor, @JsonProperty(ORDINAL) Integer ordinal) {
        return new FormDescriptorRecord(projectId, formDescriptor == null ? FormDescriptor.empty(FormId.generate()) : formDescriptor, ordinal == null ? 0 : ordinal);
    }

    @Override
    public int compareTo(@Nonnull FormDescriptorRecord o) {
        return comparingByOrdinal.compare(this, o);
    }

    @JsonProperty(PROJECT_ID)
    @Nonnull
    public ProjectId getProjectId() {
        return projectId;
    }

    @JsonProperty(FORM_DESCRIPTOR)
    @Nonnull
    public FormDescriptor getFormDescriptor() {
        return formDescriptor;
    }

    @JsonProperty(ORDINAL)
    @Nonnull
    public Integer getOrdinal() {
        return ordinal;
    }
}
