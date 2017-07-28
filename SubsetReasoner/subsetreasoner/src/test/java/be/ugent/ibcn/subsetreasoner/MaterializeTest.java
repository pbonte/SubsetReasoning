/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * @author pbonte
 *
 */
public class MaterializeTest {

	@Test
	public void testSimpleSubset() throws Exception{
		String ontologyIri = "http://massif.streaming/ontologies/rsplab/citybenchPlus.owl";
		String eventIri = "file:///home/pbonte/Github/SubsetReasoning/SubsetReasoner/subsetreasoner/resources/test.owl";
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(ontologyIri));
		OWLOntology event = manager.loadOntologyFromOntologyDocument(IRI.create(eventIri));
		Configuration c = new Configuration();
		c.ignoreUnsupportedDatatypes=true;
		Reasoner reasoner = new Reasoner(c, event);
		System.out.println("Before: " +event.getAxiomCount());
		OWLOntology result = Materializer.materialize(event, reasoner);
		System.out.println("After: " +result.getAxiomCount());
		System.out.println(result.getAxioms());
	}
	
}
