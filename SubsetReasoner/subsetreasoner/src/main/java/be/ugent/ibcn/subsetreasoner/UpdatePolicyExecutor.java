/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * @author pbonte
 *
 */
public class UpdatePolicyExecutor {

	public enum UpdatePolicy{
		LATEST, COMBINE, UPDATE
	}
	
	public static OWLOntology update(OWLOntology ont1, OWLOntology ont2, UpdatePolicy updatePol){
		switch(updatePol){
		case LATEST:
			return ont2;
		case COMBINE:
			ont2.getOWLOntologyManager().addAxioms(ont2, ont1.axioms());
			return ont2;
		case UPDATE:
			//find overlapping axioms
			Set<OWLAxiom> overlap = findOverlap(ont1, ont2.getAxioms());
			//remove overlap
			ont1.getOWLOntologyManager().removeAxioms(ont1, overlap.stream());
			//add new axioms
			ont1.getOWLOntologyManager().addAxioms(ont1, ont2.axioms());
			return ont1;
		default:
			return ont2;
		
		}
		
	}
	
	private static Set<OWLAxiom> findOverlap(OWLOntology ontology, Set<OWLAxiom> newAxes){
		Set<OWLAxiom> doRemove = new HashSet<OWLAxiom>();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		for (OWLAxiom ax : newAxes) {
			if (ax instanceof OWLObjectPropertyAssertionAxiom) {
				OWLObjectPropertyAssertionAxiom objAx = (OWLObjectPropertyAssertionAxiom) ax;
				// remove the object if equal subject and property

				for (OWLIndividual object : EntitySearcher.getObjectPropertyValues(objAx.getSubject(), objAx.getProperty(), ontology).collect(Collectors.toSet())) {
					OWLObjectPropertyAssertionAxiom rmAx = manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objAx.getProperty(), objAx.getSubject(), object);
					doRemove.add(rmAx);

				}
			}
			if (ax instanceof OWLDataPropertyAssertionAxiom) {
				OWLDataPropertyAssertionAxiom dataAx = (OWLDataPropertyAssertionAxiom) ax;
				for (OWLLiteral lit : EntitySearcher.getDataPropertyValues(dataAx.getSubject(), dataAx.getProperty(), ontology).collect(Collectors.toSet())) {
					OWLDataPropertyAssertionAxiom rmAx = manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dataAx.getProperty(), dataAx.getSubject(), lit);
					doRemove.add(rmAx);

				}
			}
		}
		return doRemove;
	}
}
