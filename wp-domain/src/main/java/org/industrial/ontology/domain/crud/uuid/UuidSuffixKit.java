package org.industrial.ontology.domain.crud.uuid;



import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitId;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.semanticweb.owlapi.model.IRI;
import java.util.Optional;
import org.industrial.ontology.domain.util.UriEncoding;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.uuid.UuidSuffixKit}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/08/2013
 */
public class UuidSuffixKit extends EntityCrudKit<UuidSuffixSettings> {

    public static final String EXAMPLE_SUFFIX = "RtvBaCCEyk09YwGRQljc2z";

    private static EntityCrudKitId ID = EntityCrudKitId.get("UUID");

    public UuidSuffixKit() {
        super(ID, "Auto-generated Universally Unique Id (UUID)");
    }

    public static EntityCrudKitId getId() {
        return ID;
    }

    @Override
    public EntityCrudKitPrefixSettings getDefaultPrefixSettings() {
        return EntityCrudKitPrefixSettings.get();
    }

    @Override
    public UuidSuffixSettings getDefaultSuffixSettings() {
        return UuidSuffixSettings.get();
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
    public IRI generateExample(EntityCrudKitPrefixSettings prefixSettings, UuidSuffixSettings suffixSettings) {
        return IRI.create(UriEncoding.encodeUri(prefixSettings.getIRIPrefix()), EXAMPLE_SUFFIX);
    }
}
