/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;

/**
 * @author pbonte
 *
 */
public class Materializer {
	
	public static OWLOntology materialize(OWLOntology ontology, Reasoner reasoner){
		reasoner.flush();
		List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
	    // generators.add(new InferredSubClassAxiomGenerator());
	    generators.add(new InferredClassAssertionAxiomGenerator());
		InferredOntologyGenerator infGen = new InferredOntologyGenerator(reasoner,generators);
		infGen.fillOntology(ontology.getOWLOntologyManager().getOWLDataFactory(), ontology);
		return ontology;		
	}

}
