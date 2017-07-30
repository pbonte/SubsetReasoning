package be.ugent.ibcn.subsetreasoner.util;

import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class OWLUtils {

	/**
	 * Finds the overlapping axioms in an axiom set and an ontology
	 * @param ontology
	 * @param newEvent
	 * @return
	 */
	public static Set<OWLAxiom> getOverlappingAxioms(OWLOntology ontology, Set<OWLAxiom> newEvent){
		// Find updated object and data properties.
		Set<OWLAxiom> overlappingAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax : newEvent) {
			if(ontology.containsAxiom(ax)){
				overlappingAxioms.add(ax);
			}
		}
		return overlappingAxioms;
	}
	/**
	 * Finds the axioms in an axiom set that are not present in an ontology
	 * @param ontology
	 * @param newEvent
	 * @return
	 */
	public static Set<OWLAxiom> getUniqueAxioms(OWLOntology ontology, Set<OWLAxiom> newEvent){
		// Find updated object and data properties.
		Set<OWLAxiom> uniqueAxioms = new HashSet<OWLAxiom>();
		for (OWLAxiom ax : newEvent) {
			if(!ontology.containsAxiom(ax)){
				uniqueAxioms.add(ax);
			}
		}
		return uniqueAxioms;
	}
}
