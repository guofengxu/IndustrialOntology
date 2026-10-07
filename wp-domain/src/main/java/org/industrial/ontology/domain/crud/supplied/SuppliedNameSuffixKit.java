package org.industrial.ontology.domain.crud.supplied;



import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitId;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.semanticweb.owlapi.model.IRI;
import java.util.Optional;
import org.industrial.ontology.domain.util.UriEncoding;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.supplied.SuppliedNameSuffixKit}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/08/2013
 */
public class SuppliedNameSuffixKit extends EntityCrudKit<SuppliedNameSuffixSettings> {

    private static final EntityCrudKitId ID = EntityCrudKitId.get("SuppliedNameSuffix");

    public SuppliedNameSuffixKit() {
        super(ID, "Supplied name");
    }

    public static EntityCrudKitId getId() {
        return ID;
    }

    @Override
    public EntityCrudKitPrefixSettings getDefaultPrefixSettings() {
        return EntityCrudKitPrefixSettings.get();
    }

    @Override
    public SuppliedNameSuffixSettings getDefaultSuffixSettings() {
        return SuppliedNameSuffixSettings.get();
    }

    @Override
    public Optional<String> getPrefixValidationMessage(String prefix) {
        if(!(prefix.endsWith("#") || prefix.endsWith("/"))) {
            return Optional.of("It is recommended that your prefix ends with a forward slash i.e. <b>/</b> (or a #)");
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public IRI generateExample(EntityCrudKitPrefixSettings prefixSettings, SuppliedNameSuffixSettings suffixSettings) {
        return IRI.create(UriEncoding.encodeUri(prefixSettings.getIRIPrefix()), "Person");
    }
}
