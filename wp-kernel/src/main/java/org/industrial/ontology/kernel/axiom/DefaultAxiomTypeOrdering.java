package org.industrial.ontology.kernel.axiom;



import com.google.common.collect.ImmutableList;
import org.semanticweb.owlapi.model.AxiomType;

import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.ANNOTATION_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.CLASS_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.DATATYPE_DEFINITION;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.DECLARATION;
import static org.semanticweb.owlapi.model.AxiomType.DIFFERENT_INDIVIDUALS;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_UNION;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.HAS_KEY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.REFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SAME_INDIVIDUAL;
import static org.semanticweb.owlapi.model.AxiomType.SUBCLASS_OF;
import static org.semanticweb.owlapi.model.AxiomType.SUB_ANNOTATION_PROPERTY_OF;
import static org.semanticweb.owlapi.model.AxiomType.SUB_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_PROPERTY_CHAIN_OF;
import static org.semanticweb.owlapi.model.AxiomType.SWRL_RULE;
import static org.semanticweb.owlapi.model.AxiomType.SYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.TRANSITIVE_OBJECT_PROPERTY;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.axiom.DefaultAxiomTypeOrdering}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 03/02/15
 */
public class DefaultAxiomTypeOrdering {

    private static final ImmutableList<AxiomType<?>> DEFAULT_ORDERING;

    static {
        DEFAULT_ORDERING = ImmutableList.<AxiomType<?>>builder()
                .add(
                        DECLARATION,

                        ANNOTATION_ASSERTION,

                        // Class axioms
                        EQUIVALENT_CLASSES,
                        SUBCLASS_OF,
                        DISJOINT_CLASSES,
                        DISJOINT_UNION,
                        HAS_KEY,

                        // Datatype axioms,
                        DATATYPE_DEFINITION,

                        // Object property axioms
                        EQUIVALENT_OBJECT_PROPERTIES,
                        SUB_OBJECT_PROPERTY,
                        SUB_PROPERTY_CHAIN_OF,
                        INVERSE_OBJECT_PROPERTIES,
                        DISJOINT_OBJECT_PROPERTIES,
                        OBJECT_PROPERTY_DOMAIN,
                        OBJECT_PROPERTY_RANGE,
                        FUNCTIONAL_OBJECT_PROPERTY,
                        INVERSE_FUNCTIONAL_OBJECT_PROPERTY,
                        TRANSITIVE_OBJECT_PROPERTY,
                        SYMMETRIC_OBJECT_PROPERTY,
                        ASYMMETRIC_OBJECT_PROPERTY,
                        REFLEXIVE_OBJECT_PROPERTY,
                        IRREFLEXIVE_OBJECT_PROPERTY,

                        // Data property axioms
                        EQUIVALENT_DATA_PROPERTIES,
                        SUB_DATA_PROPERTY,
                        DISJOINT_DATA_PROPERTIES,
                        DATA_PROPERTY_DOMAIN,
                        DATA_PROPERTY_RANGE,
                        FUNCTIONAL_DATA_PROPERTY,

                        // ABox axioms
                        CLASS_ASSERTION,
                        OBJECT_PROPERTY_ASSERTION,
                        DATA_PROPERTY_ASSERTION,
                        SAME_INDIVIDUAL,
                        DIFFERENT_INDIVIDUALS,
                        NEGATIVE_OBJECT_PROPERTY_ASSERTION,
                        NEGATIVE_DATA_PROPERTY_ASSERTION,

                        // Annotation property axioms
                        SUB_ANNOTATION_PROPERTY_OF,
                        ANNOTATION_PROPERTY_DOMAIN,
                        ANNOTATION_PROPERTY_RANGE,

                        // Rules
                        SWRL_RULE


                ).build();
    }

    public static ImmutableList<AxiomType<?>> get() {
        return DEFAULT_ORDERING;
    }
}
