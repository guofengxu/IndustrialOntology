package org.industrial.ontology.kernel.render;

import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.kernel.mansyntax.render.DeprecatedEntityChecker;
import org.industrial.ontology.kernel.mansyntax.render.HasGetRendering;
import org.industrial.ontology.kernel.mansyntax.render.HighlightedEntityChecker;
import org.industrial.ontology.kernel.mansyntax.render.ManchesterSyntaxObjectRenderer;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.renderer.HasHtmlBrowserText;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import javax.annotation.Nonnull;
import java.util.Set;
import org.industrial.ontology.domain.entity.IRIData;
import org.industrial.ontology.domain.entity.OWLAnnotationPropertyData;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.entity.OWLLiteralData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import org.industrial.ontology.domain.entity.OWLPrimitiveData;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.RenderingManager}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 08/03/2012
 */
public class RenderingManager implements HasGetRendering, HasHtmlBrowserText {

    private final DictionaryManager dictionaryManager;

    private final DeprecatedEntityChecker deprecatedEntityChecker;

    private final ManchesterSyntaxObjectRenderer htmlManchesterSyntaxRenderer;

    private final OWLObjectRenderer owlObjectRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

    public RenderingManager(DictionaryManager dictionaryManager, DeprecatedEntityChecker deprecatedChecker, ManchesterSyntaxObjectRenderer objectRenderer) {
        this.dictionaryManager = dictionaryManager;
        this.htmlManchesterSyntaxRenderer = objectRenderer;
        this.deprecatedEntityChecker = deprecatedChecker;
        owlObjectRenderer.setShortFormProvider(new ShortFormAdapter(dictionaryManager));
    }

    /**
     * Gets the short for for the specified entity.
     * @param entity The entity.
     * @return The entity short form. Not null.
     */
    @Nonnull
    public String getShortForm(OWLEntity entity) {
        return dictionaryManager.getShortForm(entity);
    }

    /**
     * Gets the browser text for a given OWLObject.
     * @param object The object.
     * @return The browser text for the object.
     */
    @Deprecated
    public String getBrowserText(OWLObject object) {
        return owlObjectRenderer.render(object);
    }

    private String getHTMLBrowserText(OWLObject object) {
        return getHTMLBrowserText(object, entity -> false);
    }

    @Override
    public String getHtmlBrowserText(OWLObject object) {
        return new HtmlBuilder().appendHtmlConstant(getHTMLBrowserText(object)).toSafeHtml();
    }

    public String getHTMLBrowserText(OWLObject object, final Set<String> highlightedPhrases) {
        return getHTMLBrowserText(object, entity -> highlightedPhrases.contains(dictionaryManager.getShortForm(entity)));
    }

    private String getHTMLBrowserText(OWLObject object, HighlightedEntityChecker highlightChecker) {
        return htmlManchesterSyntaxRenderer.render(object, highlightChecker, deprecatedEntityChecker);
    }

    public OWLEntityData getRendering(OWLEntity entity) {
        var deprecated = deprecatedEntityChecker.isDeprecated(entity);
        return DataFactory.getOWLEntityData(entity, dictionaryManager.getShortForms(entity), deprecated);
    }

    public OWLPrimitiveData getRendering(OWLAnnotationValue value) {
        if (value instanceof IRI) {
            return IRIData.get((IRI) value, ImmutableMap.of());
        } else if (value instanceof OWLLiteral) {
            return OWLLiteralData.get((OWLLiteral) value);
        } else {
            throw new RuntimeException("Unsupported");
        }
    }

    public OWLClassData getClassData(OWLClass cls) {
        var deprecated = deprecatedEntityChecker.isDeprecated(cls);
        return OWLClassData.get(cls, dictionaryManager.getShortForms(cls), deprecated);
    }

    public OWLObjectPropertyData getObjectPropertyData(OWLObjectProperty property) {
        var deprecated = deprecatedEntityChecker.isDeprecated(property);
        return OWLObjectPropertyData.get(property, dictionaryManager.getShortForms(property), deprecated);
    }

    public OWLDataPropertyData getDataPropertyData(OWLDataProperty property) {
        var deprecated = deprecatedEntityChecker.isDeprecated(property);
        return OWLDataPropertyData.get(property, dictionaryManager.getShortForms(property), deprecated);
    }

    public OWLAnnotationPropertyData getAnnotationPropertyData(OWLAnnotationProperty property) {
        var deprecated = deprecatedEntityChecker.isDeprecated(property);
        return OWLAnnotationPropertyData.get(property, dictionaryManager.getShortForms(property), deprecated);
    }

    public OWLNamedIndividualData getIndividualData(OWLNamedIndividual individual) {
        var deprecated = deprecatedEntityChecker.isDeprecated(individual);
        return OWLNamedIndividualData.get(individual, dictionaryManager.getShortForms(individual), deprecated);
    }

    public OWLDatatypeData getDatatypeData(OWLDatatype datatype) {
        var deprecated = deprecatedEntityChecker.isDeprecated(datatype);
        return OWLDatatypeData.get(datatype, dictionaryManager.getShortForms(datatype), deprecated);
    }

    public void dispose() {
    }
}
