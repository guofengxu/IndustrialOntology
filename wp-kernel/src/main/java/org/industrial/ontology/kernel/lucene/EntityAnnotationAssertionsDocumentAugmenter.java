package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.kernel.api.index.ProjectAnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.domain.lang.AnnotationAssertionDictionaryLanguage;
import org.industrial.ontology.domain.lang.AnnotationAssertionPathDictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.DEPRECATED_FALSE;

import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.DEPRECATED_TRUE;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityAnnotationAssertionsDocumentAugmenter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-06
 */
public class EntityAnnotationAssertionsDocumentAugmenter implements EntityDocumentAugmenter {

    @Nonnull
    private final ProjectAnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    @Nonnull
    private final DictionaryLanguageFieldWriter fieldWriter;


    public EntityAnnotationAssertionsDocumentAugmenter(@Nonnull ProjectAnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex,
                                                       @Nonnull DictionaryLanguageFieldWriter fieldWriter) {
        this.annotationAssertionsIndex = annotationAssertionsIndex;
        this.fieldWriter = fieldWriter;
    }

    @Override
    public void augmentDocument(@Nonnull OWLEntity entity, @Nonnull Document document) {
        addAnnotationAssertions(entity, document);
    }

    private void addAnnotationAssertions(OWLEntity entity, Document document) {
        var entityIri = entity.getIRI();
        var deprecatedAssertions = new ArrayList<OWLAnnotationAssertionAxiom>();
        annotationAssertionsIndex.getAnnotationAssertionAxioms(entityIri)
                                 .forEach(ax -> {
                                     var path = new LinkedHashSet<OWLAnnotationProperty>();
                                     processAnnotationAssertionAxiom(ax, path, document);
                                     if(ax.isDeprecatedIRIAssertion()) {
                                         deprecatedAssertions.add(ax);
                                     }
                                 });

        // Special treatment for deprecated to allow search filtering

        var deprecated = !deprecatedAssertions.isEmpty();
        var deprecatedFieldValue = deprecated ? DEPRECATED_TRUE : DEPRECATED_FALSE;
        document.add(new StringField(EntityDocumentFieldNames.DEPRECATED, deprecatedFieldValue, Field.Store.NO));
    }

    /**
     * Process a path to an annotation value that is a literal.
     * @param ax The axiom
     * @param path The current path
     * @throws ClassCastException if the axiom does not have a subject that is a literal
     */
    private void processAnnotationAssertionAxiom(OWLAnnotationAssertionAxiom ax,
                                                 LinkedHashSet<OWLAnnotationProperty> path,
                                                 Document document) {
        var value = ax.getValue();
        if(value instanceof OWLLiteral) {
            processAnnotationAssertionPathToLiteralTerminal(ax, path, document);
        }
        else if(value instanceof OWLAnnotationSubject) {
            processAnnotationPathToAnnotationSubject(ax, path, document);
        }
    }

    /**
     * Process a path to an annotation subject (either an IRI or anonymous individual)
     * @param ax The annotation assertion axiom
     * @param path The current path
     * @param document The document to add fields to
     */
    private void processAnnotationPathToAnnotationSubject(@Nonnull OWLAnnotationAssertionAxiom ax,
                                                          @Nonnull LinkedHashSet<OWLAnnotationProperty> path,
                                                          @Nonnull Document document) {
        var subject = (OWLAnnotationSubject) ax.getValue();
        if(path.add(ax.getProperty())) {
            annotationAssertionsIndex.getAnnotationAssertionAxioms(subject)
                                     .forEach(nestedAx -> processAnnotationAssertionAxiom(nestedAx, path, document));
            path.remove(ax.getProperty());
        }
    }

    private void processAnnotationAssertionPathToLiteralTerminal(OWLAnnotationAssertionAxiom ax,
                                                                 LinkedHashSet<OWLAnnotationProperty> path,
                                                                 Document document) {
        var literal = (OWLLiteral) ax.getValue();
        // Push
        path.add(ax.getProperty());
        var dictionaryLanguage = getDictionaryLanguage(path, literal.getLang());
        // Remove last element
        path.remove(ax.getProperty());

        var lexicalValue = literal.getLiteral();
        fieldWriter.addFieldForDictionaryLanguage(document, dictionaryLanguage, lexicalValue);
    }

    private static DictionaryLanguage getDictionaryLanguage(LinkedHashSet<OWLAnnotationProperty> path,
                                                            String lang) {
        if(path.size() == 1) {
            var annotationPropertyIri = path.stream().findFirst().orElseThrow().getIRI();
            return AnnotationAssertionDictionaryLanguage.get(annotationPropertyIri, lang);
        }
        else {
            var iriPath = path.stream()
                              .map(OWLAnnotationProperty::getIRI)
                              .collect(toImmutableList());
            return AnnotationAssertionPathDictionaryLanguage.get(iriPath, lang);
        }
    }
}
