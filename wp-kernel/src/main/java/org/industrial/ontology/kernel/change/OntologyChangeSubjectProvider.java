package org.industrial.ontology.kernel.change;



import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.entity.SubjectClosureResolver;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.util.AxiomSubjectProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeVisitorEx;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.OntologyChangeSubjectProvider}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 18/02/2014
 */
public class OntologyChangeSubjectProvider implements HasGetChangeSubjects {

    private ChangeSubjectProvider changeSubjectProvider;

    private final SubjectClosureResolver closureResolver;

    public OntologyChangeSubjectProvider(EntitiesInProjectSignatureByIriIndex entitiesByIRI,
                                         SubjectClosureResolver closureResolver) {
        this.changeSubjectProvider = new ChangeSubjectProvider(new AxiomEntitySubjectProvider(entitiesByIRI));
        this.closureResolver = checkNotNull(closureResolver);
    }

    @Override
    public Set<OWLEntity> getChangeSubjects(OntologyChange change) {
        return change.accept(changeSubjectProvider)
                .stream()
                .flatMap(closureResolver::resolve)
                .collect(Collectors.toSet());
    }



    private static class ChangeSubjectProvider implements OntologyChangeVisitorEx<Set<OWLEntity>> {

        private AxiomEntitySubjectProvider subjectProvider;

        private ChangeSubjectProvider(AxiomEntitySubjectProvider subjectProvider) {
            this.subjectProvider = subjectProvider;
        }

        @Override
        public Set<OWLEntity> getDefaultReturnValue() {
            return Collections.emptySet();
        }

        @Override
        public Set<OWLEntity> visit(@Nonnull AddAxiomChange addAxiomChange) {
            return subjectProvider.getSubject(addAxiomChange.getAxiom());
        }

        @Override
        public Set<OWLEntity> visit(@Nonnull RemoveAxiomChange removeAxiomChange) {
            return subjectProvider.getSubject(removeAxiomChange.getAxiom());
        }
    }


    private static class AxiomEntitySubjectProvider {

        private EntitiesInProjectSignatureByIriIndex entitiesByIri;

        private AxiomEntitySubjectProvider(EntitiesInProjectSignatureByIriIndex entitiesByIri) {
            this.entitiesByIri = entitiesByIri;
        }

        public Set<OWLEntity> getSubject(OWLAxiom axiom) {
            OWLObject subject = new AxiomSubjectProvider().getSubject(axiom);
            if(subject instanceof OWLEntity) {
                return Collections.singleton((OWLEntity) subject);
            }
            else if(subject instanceof IRI) {
                return entitiesByIri.getEntitiesInSignature((IRI) subject).collect(Collectors.toSet());
            }
            else {
                return Collections.emptySet();
            }
        }
    }

}
