package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.RemovedPropertyDomain}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record RemovedPropertyDomain(@Nonnull OWLProperty property, @Nonnull OWLObject domain) implements StructuredChangeDescription {

    public RemovedPropertyDomain {
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(domain, "Null domain");
    }

    public static RemovedPropertyDomain get(@Nonnull OWLProperty property, @Nonnull OWLObject range) {
        return new RemovedPropertyDomain(property, range);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "RemovedPropertyDomain";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Removed %s from the domain of %s", getDomain(), getProperty());
    }

    @Nonnull
    public OWLProperty getProperty() {
        return property;
    }

    @Nonnull
    public OWLObject getDomain() {
        return domain;
    }
}
