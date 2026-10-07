package org.industrial.ontology.kernel.hierarchy;




import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.stream.Collectors;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.semanticweb.owlapi.model.AxiomType.SUB_OBJECT_PROPERTY;

import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.OntologySignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

import org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.ObjectPropertyHierarchyProviderImpl}.
 * <p>
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 23-Jan-2007<br><br>
 */
public class ObjectPropertyHierarchyProvider extends AbstractOWLPropertyHierarchyProvider<OWLObjectProperty> implements org.industrial.ontology.kernel.api.hierarchy.ObjectPropertyHierarchyProvider {

    @Nonnull
    private final EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex;

    @Nonnull
    private final SubObjectPropertyAxiomsBySubPropertyIndex subObjectPropertyAxiomsBySubPropertyIndex;

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    public ObjectPropertyHierarchyProvider(@Nonnull ProjectId projectId,
                                               @ObjectPropertyHierarchyRoot OWLObjectProperty root,
                                               @Nonnull EntitiesInProjectSignatureIndex entitiesInProjectSignatureIndex,
                                               @Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                               @Nonnull OntologySignatureByTypeIndex ontologySignatureByTypeIndex,
                                               @Nonnull SubObjectPropertyAxiomsBySubPropertyIndex subObjectPropertyAxiomsBySubPropertyIndex,
                                               @Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        super(projectId, root, entitiesInProjectSignatureIndex, EntityType.OBJECT_PROPERTY, projectOntologiesIndex,
              ontologySignatureByTypeIndex);
        this.entitiesInProjectSignatureIndex = entitiesInProjectSignatureIndex;
        this.subObjectPropertyAxiomsBySubPropertyIndex = checkNotNull(subObjectPropertyAxiomsBySubPropertyIndex);
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
    }

    @Override
    protected String getHierarchyName() {
        return "ObjectProperty Hierarchy";
    }

    @Override
    public boolean containsReference(OWLObjectProperty object) {
        return entitiesInProjectSignatureIndex.containsEntityInSignature(object);
    }

    @Override
    public Collection<OWLObjectProperty> getChildren(OWLObjectProperty object) {
        rebuildIfNecessary();
        if(getRoot().equals(object)) {
            return getChildrenOfRoot();
        }
        return projectOntologiesIndex.getOntologyIds()
                                     .flatMap(ontId -> axiomsByTypeIndex.getAxiomsByType(SUB_OBJECT_PROPERTY,
                                                                                         ontId))
                                     .filter(ax -> ax.getSuperProperty()
                                                     .equals(object))
                                     .map(OWLSubObjectPropertyOfAxiom::getSubProperty)
                                     .filter(OWLObjectPropertyExpression::isNamed)
                                     .map(OWLObjectPropertyExpression::asOWLObjectProperty)
                                     .collect(Collectors.toSet());
    }

    @Override
    public Collection<OWLObjectProperty> getParents(OWLObjectProperty property) {
        rebuildIfNecessary();
        return projectOntologiesIndex.getOntologyIds()
                                     .flatMap(ontId -> subObjectPropertyAxiomsBySubPropertyIndex.getSubPropertyOfAxioms(
                                             property,
                                             ontId))
                                     .map(OWLSubPropertyAxiom::getSuperProperty)
                                     .filter(OWLObjectPropertyExpression::isNamed)
                                     .map(OWLObjectPropertyExpression::asOWLObjectProperty)
                                     .collect(Collectors.toSet());
    }

}
