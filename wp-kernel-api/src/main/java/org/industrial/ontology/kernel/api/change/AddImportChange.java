package org.industrial.ontology.kernel.api.change;

import org.semanticweb.owlapi.change.AddImportData;
import org.semanticweb.owlapi.change.OWLOntologyChangeRecord;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.AddImportChange}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-27
 */
public record AddImportChange(@Nonnull OWLOntologyID ontologyId, @Nonnull OWLImportsDeclaration importsDeclaration) implements OntologyImportChange {

    public AddImportChange {
        Objects.requireNonNull(ontologyId, "Null ontologyId");
        Objects.requireNonNull(importsDeclaration, "Null importsDeclaration");
    }

    public static AddImportChange of(@Nonnull OWLOntologyID ontologyID, @Nonnull OWLImportsDeclaration importsDeclaration) {
        return new AddImportChange(ontologyID, importsDeclaration);
    }

    @Nonnull
    @Override
    public OWLOntologyChangeRecord toOwlOntologyChangeRecord() {
        return new OWLOntologyChangeRecord(getOntologyId(), new AddImportData(getImportsDeclaration()));
    }

    @Nonnull
    @Override
    public AddImportChange replaceOntologyId(@Nonnull OWLOntologyID ontologyId) {
        if (getOntologyId().equals(ontologyId)) {
            return this;
        } else {
            return AddImportChange.of(ontologyId, getImportsDeclaration());
        }
    }

    @Override
    public void accept(@Nonnull OntologyChangeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull OntologyChangeVisitorEx<R> visitorEx) {
        return visitorEx.visit(this);
    }

    @Nonnull
    @Override
    public RemoveImportChange getInverseChange() {
        return RemoveImportChange.of(getOntologyId(), getImportsDeclaration());
    }

    @Override
    @Nonnull
    public OWLOntologyID getOntologyId() {
        return ontologyId;
    }

    @Override
    @Nonnull
    public OWLImportsDeclaration getImportsDeclaration() {
        return importsDeclaration;
    }
}
