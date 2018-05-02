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
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		String ontologyIri = "normal-0-wards.owl";
		String eventIri = "3SetUpPersonalRelation.owl";
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		OWLOntology ontology =  manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(ontologyIri));
		OWLOntology event = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(eventIri));
		Set<OWLAxiom> startAx = event.getAxioms();
		Configuration c = new Configuration();
		c.ignoreUnsupportedDatatypes=true;
		Reasoner reasoner = new Reasoner(c, event);
		System.out.println("Before: " +event.getAxiomCount());
		int axCount = event.getAxiomCount();
		System.out.println(startAx);
		OWLOntology result = Materializer.materialize(event, reasoner);
		Set<OWLAxiom> resultAx = result.getAxioms();
		startAx.stream().forEach(System.out::println);
		System.out.println("After: " +result.getAxiomCount());
		resultAx.stream().forEach(System.out::println);
		assertEquals(result.getAxiomCount()>axCount, true);
	}
	
}
