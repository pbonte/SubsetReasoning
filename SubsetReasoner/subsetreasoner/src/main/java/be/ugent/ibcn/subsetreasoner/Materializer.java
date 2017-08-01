/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.InferredAxiomGenerator;
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator;
import org.semanticweb.owlapi.util.InferredInverseObjectPropertiesAxiomGenerator;
import org.semanticweb.owlapi.util.InferredObjectPropertyAxiomGenerator;
import org.semanticweb.owlapi.util.InferredOntologyGenerator;
import org.semanticweb.owlapi.util.InferredSubObjectPropertyAxiomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pbonte
 *
 */
public class Materializer {
	private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	final static Logger logger = LoggerFactory.getLogger(Materializer.class);

	public static OWLOntology materialize(OWLOntology ontology, Reasoner reasoner){
		reasoner.flush();
		List<InferredAxiomGenerator<? extends OWLAxiom>> generators = new ArrayList<InferredAxiomGenerator<? extends OWLAxiom>>();
	    // generators.add(new InferredSubClassAxiomGenerator());
		generators.add(new  InferredInverseObjectPropertiesAxiomGenerator());
	    generators.add(new InferredClassAssertionAxiomGenerator());
	    generators.add(new InferredSubObjectPropertyAxiomGenerator());
		InferredOntologyGenerator infGen = new InferredOntologyGenerator(reasoner,generators);
		infGen.fillOntology(ontology.getOWLOntologyManager().getOWLDataFactory(), ontology);
		return ontology;		
	}
	
	public static OWLOntology materialize(Set<OWLAxiom> axioms, Reasoner reasoner ){
		OWLOntology temp = null;
		try{
			temp = manager.createOntology();
			manager.addAxioms(temp, axioms);
			temp = materialize(temp, reasoner);
		}catch(Exception e){
			logger.error("Unable to create ontology",e);
		}
		return temp;
	}

}
