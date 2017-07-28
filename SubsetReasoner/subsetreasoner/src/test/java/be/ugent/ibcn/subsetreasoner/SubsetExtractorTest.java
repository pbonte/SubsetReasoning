/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
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
public class SubsetExtractorTest {

	OWLOntology ontology;
	int depth;
	int actual_depth;
	@Before
	public void setUp() throws Exception{
		depth = 6;
		actual_depth = 10;
		Set<OWLAxiom> axioms = generateTree(actual_depth);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.createOntology();
		manager.addAxioms(ontology, axioms.stream());

		
	}
	@Test
	public void testSimpleSubset(){
		SubsetExtractor extractor = new SubsetExtractor(ontology, depth);
		
		Set<OWLAxiom> subset = extractor.extract(generateTree(0));
		Set<OWLAxiom> result = generateTree(depth);
		assertEquals(result, subset);
	}
	@Test
	public void testSubsetWithoutClassAssertion(){
		SubsetExtractor extractor = new SubsetExtractor(ontology, depth);
		Set<OWLAxiom> result = generateTree(depth);
		//select class assertion in result and remove it + remove from ontology
		Set<OWLAxiom> incomming = generateTree(0);
		OWLAxiom firstAssert = incomming.stream().filter(ax -> ax instanceof OWLClassAssertionAxiom).findFirst().get();
		OWLAxiom remove = result.stream().filter(ax -> ax instanceof OWLClassAssertionAxiom && !ax.equals(firstAssert)).findAny().get();
		ontology.getOWLOntologyManager().removeAxiom(ontology, remove);
		result.remove(remove);
		Set<OWLAxiom> subset = extractor.extract(generateTree(0));
		
		assertEquals(result, subset);
	}
	
	private Set<OWLAxiom> generateTree(int size){
		String iri = "http://be.ugent.ibcn.subsetreasoner/test.owl#";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory fact = manager.getOWLDataFactory();
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		int num = (int)Math.pow(2, size);
		int num_ind = 0;
		for(int i = 0; i <= size;i++){
			num_ind+=(int)Math.pow(2, i);
		}
		OWLClass[] clsses = new OWLClass[num];
		OWLObjectProperty[] props = new OWLObjectProperty[num];
		OWLDataProperty[] dataProps = new OWLDataProperty[num];

		OWLNamedIndividual[] inds = new OWLNamedIndividual[num_ind];
		for(int i = 0; i < num; i++){
			clsses[i] = fact.getOWLClass(IRI.create(iri+"Class"+i));
			props[i] = fact.getOWLObjectProperty(IRI.create(iri+"Prop"+i));
			dataProps[i] = fact.getOWLDataProperty(IRI.create(iri+"dataProp"+i));
		}
		for(int i = 0 ; i < num_ind; i++){
			inds[i]= fact.getOWLNamedIndividual(IRI.create(iri+"Ind"+i));
			result.add(fact.getOWLDeclarationAxiom(inds[i]));
		}
		int ind_counter = 0;
		for(int i = 0 ; i <= size; i++){
			for(int j = 0 ; j < (int)Math.pow(2, i);j++){
				result.add(fact.getOWLClassAssertionAxiom(clsses[j], inds[ind_counter]));
				result.add(fact.getOWLDataPropertyAssertionAxiom(dataProps[j], inds[ind_counter], ind_counter));

				if(i<size){
					result.add(fact.getOWLObjectPropertyAssertionAxiom(props[j], inds[ind_counter], inds[(ind_counter+1)*2-1]));
					result.add(fact.getOWLObjectPropertyAssertionAxiom(props[j], inds[ind_counter], inds[(ind_counter+1)*2]));
				}
				ind_counter++;
			}
		}
		return result;
	}
}
