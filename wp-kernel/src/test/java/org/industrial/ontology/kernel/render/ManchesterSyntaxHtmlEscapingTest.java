package org.industrial.ontology.kernel.render;

import org.industrial.ontology.kernel.mansyntax.render.DefaultHttpLinkRenderer;
import org.industrial.ontology.kernel.mansyntax.render.EntityIRIChecker;
import org.industrial.ontology.kernel.mansyntax.render.LiteralStyle;
import org.industrial.ontology.kernel.mansyntax.render.ManchesterSyntaxObjectRenderer;
import org.industrial.ontology.kernel.mansyntax.render.MarkdownLiteralRenderer;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The HTML Manchester syntax renderer escapes what the ontology's authors wrote: literals, whatever their datatype,
 * language tags, IRIs in attributes and links. The legacy renderer wrote them into the HTML as they were, so an editor
 * could put scripts into the descriptions that everyone who may view the project sees (S7 security review).
 */
class ManchesterSyntaxHtmlEscapingTest {

    private static final String SCRIPT = "<img src=x onerror=alert(1)>";

    private final OWLDataFactoryImpl dataFactory = new OWLDataFactoryImpl();

    private final ManchesterSyntaxObjectRenderer renderer = new ManchesterSyntaxObjectRenderer(
            new SimpleShortFormProvider(),
            new EntityIRIChecker() {
                @Override
                public boolean isEntityIRI(IRI iri) {
                    return false;
                }

                @Override
                public Collection<OWLEntity> getEntitiesWithIRI(IRI iri) {
                    return List.of();
                }
            },
            LiteralStyle.REGULAR,
            new DefaultHttpLinkRenderer(),
            new MarkdownLiteralRenderer());

    @Test
    void literalsShouldBeEscapedWhateverTheirDatatype() {
        assertThat(renderer.render(dataFactory.getOWLLiteral(SCRIPT, "en")))
                .contains("&lt;img src=x onerror=alert(1)&gt;")
                .doesNotContain("<img");
        var integer = dataFactory.getOWLLiteral("<b onclick=x>1</b>", OWL2Datatype.XSD_INTEGER.getDatatype(dataFactory));
        assertThat(renderer.render(integer)).doesNotContain("<b").contains("&lt;b");
    }

    @Test
    void languageTagsShouldBeEscaped() {
        var rendered = renderer.render(dataFactory.getOWLLiteral("Pizza", "en\"><script>x</script>"));
        assertThat(rendered).doesNotContain("<script>").contains("&lt;script&gt;");
    }

    @Test
    void entityIrisShouldBeEscapedInTheTitle() {
        var cls = dataFactory.getOWLClass(IRI.create("http://example.org/A\"onmouseover=\"alert(1)"));
        var rendered = renderer.render(cls);
        assertThat(rendered).doesNotContain("\"onmouseover=\"").contains("&quot;onmouseover=&quot;");
    }

    @Test
    void linksShouldBeEscapedAndOnlyHttp() {
        var link = renderer.render(dataFactory.getOWLLiteral("https://example.org/\"><script>alert(1)</script>"));
        assertThat(link).contains("<a target=\"_blank\" href=\"https://example.org/&quot;&gt;&lt;script&gt;")
                        .doesNotContain("<script>");
        var image = renderer.render(dataFactory.getOWLLiteral("https://example.org/x\"onerror=\"alert(1).png"));
        assertThat(image).contains("<img src=\"https://example.org/x&quot;onerror=&quot;alert(1).png\"/>")
                         .doesNotContain("\"onerror=\"");
        var notHttp = renderer.render(dataFactory.getOWLLiteral("httpjavascript:alert(1)"));
        assertThat(notHttp).doesNotContain("<a ").contains("ms-literal");
    }
}
