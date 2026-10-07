package org.industrial.ontology.kernel.mansyntax;



import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.Navigation;
import org.semanticweb.owlapi.util.OWLAxiomSearchFilter;
import javax.annotation.Nonnull;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import static com.google.common.base.MoreObjects.toStringHelper;

import static com.google.common.base.Preconditions.checkNotNull;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLPrimitive;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.mansyntax.ShellOwlOntology}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 *
 * The purpose of this class is to fool the Manchester syntax parser into thinking
 * that is has a reference to some {@link OWLOntology}.  In fact, the parser only
 * requires equals, hashCode and getOWLOntologyID.
 */
public class ShellOwlOntology implements OWLOntology {

    @Nonnull
    private final OWLOntologyID ontologyID;

    @Nonnull
    public static ShellOwlOntology get(@Nonnull OWLOntologyID ontologyId) {
        return new ShellOwlOntology(ontologyId);
    }

    public ShellOwlOntology(@Nonnull OWLOntologyID ontologyID) {
        this.ontologyID = checkNotNull(ontologyID);
    }

    @Nonnull
    @Override
    public OWLOntologyID getOntologyID() {
        return ontologyID;
    }

    @Override
    public int hashCode() {
        return ontologyID.hashCode();
    }


    @Override
    public String toString() {
        return toStringHelper("ShellOwlOntology")
                .addValue(ontologyID)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof OWLOntology)) {
            return false;
        }
        OWLOntology other = (OWLOntology) obj;
        return this.ontologyID.equals(other.getOntologyID());
    }

    @Override
    public void accept(@Nonnull OWLNamedObjectVisitor visitor) {

    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLNamedObjectVisitorEx<O> visitor) {
        throw new RuntimeException("Cannot accept visits");
    }

    @Nonnull
    @Override
    public OWLOntologyManager getOWLOntologyManager() {
        throw new RuntimeException("Cannot provide OWLOntologyManager");
    }

    @Override
    public void setOWLOntologyManager(OWLOntologyManager manager) {

    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLAnnotation> getAnnotations() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<IRI> getDirectImportsDocuments() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLOntology> getDirectImports() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLOntology> getImports() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLOntology> getImportsClosure() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLImportsDeclaration> getImportsDeclarations() {
        return Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getTBoxAxioms(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getABoxAxioms(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getRBoxAxioms(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassAxiom> getGeneralClassAxioms() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public boolean isDeclared(@Nonnull OWLEntity owlEntity) {
        return false;
    }

    @Override
    public boolean isDeclared(@Nonnull OWLEntity owlEntity, @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public void saveOntology() {

    }

    @Override
    public void saveOntology(@Nonnull IRI documentIRI) {

    }

    @Override
    public void saveOntology(@Nonnull OutputStream outputStream) {

    }

    @Override
    public void saveOntology(@Nonnull OWLDocumentFormat ontologyFormat) {

    }

    @Override
    public void saveOntology(@Nonnull OWLDocumentFormat ontologyFormat,
                             @Nonnull IRI documentIRI) {

    }

    @Override
    public void saveOntology(@Nonnull OWLDocumentFormat ontologyFormat,
                             @Nonnull OutputStream outputStream) {

    }

    @Override
    public void saveOntology(@Nonnull OWLOntologyDocumentTarget documentTarget) {

    }

    @Override
    public void saveOntology(@Nonnull OWLDocumentFormat ontologyFormat,
                             @Nonnull OWLOntologyDocumentTarget documentTarget) {

    }

    @Nonnull
    @Override
    public Set<OWLEntity> getEntitiesInSignature(@Nonnull IRI entityIRI) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getAxioms(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public int getAxiomCount(@Nonnull Imports includeImportsClosure) {
        return 0;
    }

    @Nonnull
    @Override
    public Set<OWLLogicalAxiom> getLogicalAxioms(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public int getLogicalAxiomCount(@Nonnull Imports includeImportsClosure) {
        return 0;
    }

    @Nonnull
    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(@Nonnull AxiomType<T> axiomType,
                                                 @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public <T extends OWLAxiom> int getAxiomCount(@Nonnull AxiomType<T> axiomType,
                                                  @Nonnull Imports includeImportsClosure) {
        return 0;
    }

    @Override
    public boolean containsAxiom(@Nonnull OWLAxiom axiom,
                                 @Nonnull Imports includeImportsClosure,
                                 @Nonnull AxiomAnnotations ignoreAnnotations) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getAxiomsIgnoreAnnotations(@Nonnull OWLAxiom axiom, @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getReferencingAxioms(@Nonnull OWLPrimitive owlEntity, @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassAxiom> getAxioms(@Nonnull OWLClass cls, @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectPropertyAxiom> getAxioms(@Nonnull OWLObjectPropertyExpression property,
                                                 @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataPropertyAxiom> getAxioms(@Nonnull OWLDataProperty property,
                                               @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLIndividualAxiom> getAxioms(@Nonnull OWLIndividual individual,
                                             @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationAxiom> getAxioms(@Nonnull OWLAnnotationProperty property,
                                             @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatypeDefinitionAxiom> getAxioms(@Nonnull OWLDatatype datatype,
                                                     @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getAxioms(boolean b) {
        return Collections.emptySet();
    }

    @Override
    public int getAxiomCount(boolean includeImportsClosure) {
        return 0;
    }

    @Nonnull
    @Override
    public Set<OWLLogicalAxiom> getLogicalAxioms(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public int getLogicalAxiomCount(boolean includeImportsClosure) {
        return 0;
    }

    @Nonnull
    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(@Nonnull AxiomType<T> axiomType, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public <T extends OWLAxiom> int getAxiomCount(@Nonnull AxiomType<T> axiomType, boolean includeImportsClosure) {
        return 0;
    }

    @Override
    public boolean containsAxiom(@Nonnull OWLAxiom axiom, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsAxiomIgnoreAnnotations(@Nonnull OWLAxiom axiom, boolean includeImportsClosure) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getAxiomsIgnoreAnnotations(@Nonnull OWLAxiom axiom, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getReferencingAxioms(@Nonnull OWLPrimitive owlEntity, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassAxiom> getAxioms(@Nonnull OWLClass cls, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectPropertyAxiom> getAxioms(@Nonnull OWLObjectPropertyExpression property,
                                                 boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataPropertyAxiom> getAxioms(@Nonnull OWLDataProperty property, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLIndividualAxiom> getAxioms(@Nonnull OWLIndividual individual, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationAxiom> getAxioms(@Nonnull OWLAnnotationProperty property, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatypeDefinitionAxiom> getAxioms(@Nonnull OWLDatatype datatype, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public int getAxiomCount() {
        return 0;
    }

    @Override
    public int getLogicalAxiomCount() {
        return 0;
    }

    @Override
    public <T extends OWLAxiom> int getAxiomCount(@Nonnull AxiomType<T> axiomType) {
        return 0;
    }

    @Override
    public boolean containsAxiomIgnoreAnnotations(@Nonnull OWLAxiom axiom) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getAxiomsIgnoreAnnotations(@Nonnull OWLAxiom axiom) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getReferencingAxioms(@Nonnull OWLPrimitive owlEntity) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassAxiom> getAxioms(@Nonnull OWLClass cls) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectPropertyAxiom> getAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataPropertyAxiom> getAxioms(@Nonnull OWLDataProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLIndividualAxiom> getAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationAxiom> getAxioms(@Nonnull OWLAnnotationProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatypeDefinitionAxiom> getAxioms(@Nonnull OWLDatatype datatype) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getAxioms() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(@Nonnull AxiomType<T> axiomType) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsAxiom(@Nonnull OWLAxiom axiom) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLLogicalAxiom> getLogicalAxioms() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(@Nonnull Class<T> type,
                                                 @Nonnull OWLObject entity,
                                                 @Nonnull Imports includeImports,
                                                 @Nonnull Navigation forSubPosition) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public <T extends OWLAxiom> Collection<T> filterAxioms(@Nonnull OWLAxiomSearchFilter filter,
                                                           @Nonnull Object key,
                                                           @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public boolean contains(@Nonnull OWLAxiomSearchFilter filter,
                            @Nonnull Object key,
                            @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Nonnull
    @Override
    public <T extends OWLAxiom> Set<T> getAxioms(@Nonnull Class<T> type,
                                                 @Nonnull Class<? extends OWLObject> explicitClass,
                                                 @Nonnull OWLObject entity,
                                                 @Nonnull Imports includeImports,
                                                 @Nonnull Navigation forSubPosition) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(@Nonnull OWLAnnotationProperty subProperty) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(@Nonnull OWLAnnotationProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(@Nonnull OWLAnnotationProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDeclarationAxiom> getDeclarationAxioms(@Nonnull OWLEntity subject) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(@Nonnull OWLAnnotationSubject entity) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(@Nonnull OWLClass cls) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(@Nonnull OWLClass cls) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(@Nonnull OWLClass cls) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(@Nonnull OWLClass cls) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(@Nonnull OWLClass owlClass) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLHasKeyAxiom> getHasKeyAxioms(@Nonnull OWLClass cls) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(@Nonnull OWLObjectPropertyExpression subProperty) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(@Nonnull OWLObjectPropertyExpression superProperty) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(@Nonnull OWLObjectPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(@Nonnull OWLDataProperty subProperty) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(@Nonnull OWLDataPropertyExpression superProperty) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(@Nonnull OWLDataProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(@Nonnull OWLDataProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(@Nonnull OWLDataProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(@Nonnull OWLDataProperty property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(@Nonnull OWLDataPropertyExpression property) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(@Nonnull OWLClassExpression ce) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(@Nonnull OWLIndividual individual) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(@Nonnull OWLDatatype datatype) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClassExpression> getNestedClassExpressions() {
        return Collections.emptySet();
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor visitor) {
        throw new RuntimeException("Cannot accept visits");
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> visitor) {
        throw new RuntimeException("Cannot accept visits");
    }

    @Override
    public boolean isTopEntity() {
        return false;
    }

    @Override
    public boolean isBottomEntity() {
        return false;
    }

    @Override
    public int compareTo(OWLObject o) {
        return 0;
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLClass> getClassesInSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatype> getDatatypesInSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity owlEntity, @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull IRI entityIRI, @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsClassInSignature(@Nonnull IRI owlClassIRI, @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsObjectPropertyInSignature(@Nonnull IRI owlObjectPropertyIRI,
                                                     @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsDataPropertyInSignature(@Nonnull IRI owlDataPropertyIRI,
                                                   @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsAnnotationPropertyInSignature(@Nonnull IRI owlAnnotationPropertyIRI,
                                                         @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsDatatypeInSignature(@Nonnull IRI owlDatatypeIRI, @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsIndividualInSignature(@Nonnull IRI owlIndividualIRI,
                                                 @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsDatatypeInSignature(@Nonnull IRI owlDatatypeIRI) {
        return false;
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull IRI entityIRI) {
        return false;
    }

    @Override
    public boolean containsClassInSignature(@Nonnull IRI owlClassIRI) {
        return false;
    }

    @Override
    public boolean containsObjectPropertyInSignature(@Nonnull IRI owlObjectPropertyIRI) {
        return false;
    }

    @Override
    public boolean containsDataPropertyInSignature(@Nonnull IRI owlDataPropertyIRI) {
        return false;
    }

    @Override
    public boolean containsAnnotationPropertyInSignature(@Nonnull IRI owlAnnotationPropertyIRI) {
        return false;
    }

    @Override
    public boolean containsIndividualInSignature(@Nonnull IRI owlIndividualIRI) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getEntitiesInSignature(@Nonnull IRI iri, @Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public Set<IRI> getPunnedIRIs(@Nonnull Imports includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsReference(@Nonnull OWLEntity entity, @Nonnull Imports includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsReference(@Nonnull OWLEntity entity) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLClass> getClassesInSignature(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatype> getDatatypesInSignature(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature(boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity owlEntity, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull IRI entityIRI, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsClassInSignature(@Nonnull IRI owlClassIRI, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsObjectPropertyInSignature(@Nonnull IRI owlObjectPropertyIRI, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsDataPropertyInSignature(@Nonnull IRI owlDataPropertyIRI, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsAnnotationPropertyInSignature(@Nonnull IRI owlAnnotationPropertyIRI,
                                                         boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsDatatypeInSignature(@Nonnull IRI owlDatatypeIRI, boolean includeImportsClosure) {
        return false;
    }

    @Override
    public boolean containsIndividualInSignature(@Nonnull IRI owlIndividualIRI, boolean includeImportsClosure) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getEntitiesInSignature(@Nonnull IRI iri, boolean includeImportsClosure) {
        return Collections.emptySet();
    }

    @Override
    public boolean containsReference(@Nonnull OWLEntity entity, boolean includeImportsClosure) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLClass> getClassesInSignature() {
        return Collections.emptySet();
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity owlEntity) {
        return false;
    }

    @Nonnull
    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLDatatype> getDatatypesInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
        return Collections.emptySet();
    }
}
