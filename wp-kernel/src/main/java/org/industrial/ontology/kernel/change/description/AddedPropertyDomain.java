package org.industrial.ontology.kernel.change.description;

import org.industrial.ontology.kernel.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.description.AddedPropertyDomain}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2018-12-10
 */
public record AddedPropertyDomain(@Nonnull OWLProperty property, @Nonnull OWLObject domain) implements StructuredChangeDescription {

    public AddedPropertyDomain {
        Objects.requireNonNull(property, "Null property");
        Objects.requireNonNull(domain, "Null domain");
    }

    public static AddedPropertyDomain get(@Nonnull OWLProperty property, @Nonnull OWLObject range) {
        return new AddedPropertyDomain(property, range);
    }

    @Nonnull
    @Override
    public String getTypeName() {
        return "AddedPropertyDomain";
    }

    @Nonnull
    @Override
    public String formatDescription(@Nonnull OWLObjectStringFormatter formatter) {
        return formatter.formatString("Added %s to the domain of %s", getDomain(), getProperty());
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
