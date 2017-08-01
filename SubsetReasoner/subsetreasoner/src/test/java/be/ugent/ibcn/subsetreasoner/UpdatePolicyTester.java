/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.ibcn.subsetreasoner.UpdatePolicyExecutor.UpdatePolicy;

/**
 * @author pbonte
 *
 */
public class UpdatePolicyTester {

	@Test
	public void testLatest() throws Exception {
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		Set<OWLAxiom> first = generateTree(3, true);
		OWLOntology ont1 = manager.createOntology(first, IRI.create("http://first"));
		Set<OWLAxiom> second = generateTree(3, false);
		OWLOntology ont2 = manager.createOntology(second, IRI.create("http://second"));

		OWLOntology result = UpdatePolicyExecutor.update(ont1, ont2, UpdatePolicy.LATEST);
		assertEquals(second, result.getAxioms());
		assertNotEquals(first, result.getAxioms());
	}

	@Test
	public void testCombine() throws Exception {
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		Set<OWLAxiom> first = generateTree(3, true);
		OWLOntology ont1 = manager.createOntology(first, IRI.create("http://first"));
		Set<OWLAxiom> second = generateTree(3, false);
		Set<OWLAxiom> combine = new HashSet<OWLAxiom>(first);
		combine.addAll(second);
		OWLOntology ont2 = manager.createOntology(second, IRI.create("http://second"));

		OWLOntology result = UpdatePolicyExecutor.update(ont1, ont2, UpdatePolicy.COMBINE);
		assertNotEquals(second, result.getAxioms());
		assertNotEquals(first, result.getAxioms());
		assertEquals(combine, result.getAxioms());
	}

	@Test
	public void testUpdate() throws Exception {
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		Set<OWLAxiom> first = generateTree(3, true);
		OWLOntology ont1 = manager.createOntology(first, IRI.create("http://first"));
		Set<OWLAxiom> second = generateTree(3, false);
		OWLOntology ont2 = manager.createOntology(second, IRI.create("http://second"));
		Set<OWLAxiom> addition = genSimpleAxiom(2);
		manager.addAxioms(ont1, addition);
		OWLOntology result = UpdatePolicyExecutor.update(ont1, ont2, UpdatePolicy.UPDATE);
		assertNotEquals(second, result.getAxioms());
		assertNotEquals(first, result.getAxioms());
		second.addAll(addition);
		assertEquals(second, result.getAxioms());
	}

	private Set<OWLAxiom> genSimpleAxiom(int num){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory fact = manager.getOWLDataFactory();
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		String iri = "http://be.ugent.ibcn.subsetreasoner/test.owl#";
		Random r = new Random();
		OWLClass[] clsses = new OWLClass[num];
		OWLObjectProperty[] props = new OWLObjectProperty[num];

		OWLNamedIndividual[] inds = new OWLNamedIndividual[num];
		for (int i = 0; i < num; i++) {
			clsses[i] = fact.getOWLClass(IRI.create(iri + "Class" + i));
			props[i] = fact.getOWLObjectProperty(IRI.create(iri + "Prop" + i+r.nextInt()));
		}
		for (int i = 0; i < num; i++) {
			inds[i] = fact.getOWLNamedIndividual(IRI.create(iri + "Ind" + i));
			result.add(fact.getOWLDeclarationAxiom(inds[i]));
		}
		int ind_counter = 0;
		for (int i = 0; i < num; i++) {
				result.add(fact.getOWLClassAssertionAxiom(clsses[i], inds[i]));

						result.add(fact.getOWLObjectPropertyAssertionAxiom(props[i], inds[i],
								inds[i%inds.length]));
					
				}
		return result;

	}

	private Set<OWLAxiom> generateTree(int size, boolean first) {
		String iri = "http://be.ugent.ibcn.subsetreasoner/test.owl#";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory fact = manager.getOWLDataFactory();
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		int num = (int) Math.pow(2, size);
		int num_ind = 0;
		for (int i = 0; i <= size; i++) {
			num_ind += (int) Math.pow(2, i);
		}
		OWLClass[] clsses = new OWLClass[num];
		OWLObjectProperty[] props = new OWLObjectProperty[num];
		OWLDataProperty[] dataProps = new OWLDataProperty[num];

		OWLNamedIndividual[] inds = new OWLNamedIndividual[num_ind];
		for (int i = 0; i < num; i++) {
			clsses[i] = fact.getOWLClass(IRI.create(iri + "Class" + i));
			props[i] = fact.getOWLObjectProperty(IRI.create(iri + "Prop" + i));
			dataProps[i] = fact.getOWLDataProperty(IRI.create(iri + "dataProp" + i));
		}
		for (int i = 0; i < num_ind; i++) {
			inds[i] = fact.getOWLNamedIndividual(IRI.create(iri + "Ind" + i));
			result.add(fact.getOWLDeclarationAxiom(inds[i]));
		}
		int ind_counter = 0;
		for (int i = 0; i <= size; i++) {
			for (int j = 0; j < (int) Math.pow(2, i); j++) {
				result.add(fact.getOWLClassAssertionAxiom(clsses[j], inds[ind_counter]));
				result.add(fact.getOWLDataPropertyAssertionAxiom(dataProps[j], inds[ind_counter], ind_counter));

				if (i < size) {
					if (first == (j % 2 == 0)) {
						result.add(fact.getOWLObjectPropertyAssertionAxiom(props[j], inds[ind_counter],
								inds[(ind_counter + 1) * 2 - 1]));
					} else {
						result.add(fact.getOWLObjectPropertyAssertionAxiom(props[j], inds[ind_counter],
								inds[(ind_counter + 1) * 2]));
					}
				}
				ind_counter++;
			}
		}
		return result;
	}

	private Set<OWLAxiom> generateTree2(int size) {
		String iri = "http://be.ugent.ibcn.subsetreasoner/test.owl#";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory fact = manager.getOWLDataFactory();
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		int num = (int) Math.pow(2, size);
		int num_ind = 0;
		for (int i = 0; i <= size; i++) {
			num_ind += (int) Math.pow(2, i);
		}
		OWLClass[] clsses = new OWLClass[num];
		OWLObjectProperty[] props = new OWLObjectProperty[num];
		OWLDataProperty[] dataProps = new OWLDataProperty[num];

		OWLNamedIndividual[] inds = new OWLNamedIndividual[num_ind];
		for (int i = 0; i < num; i++) {
			clsses[i] = fact.getOWLClass(IRI.create(iri + "Class" + i));
			props[i] = fact.getOWLObjectProperty(IRI.create(iri + "Prop" + i));
			dataProps[i] = fact.getOWLDataProperty(IRI.create(iri + "dataProp" + i));
		}
		for (int i = 0; i < num_ind; i++) {
			inds[i] = fact.getOWLNamedIndividual(IRI.create(iri + "Ind" + i));
			result.add(fact.getOWLDeclarationAxiom(inds[i]));
		}
		int ind_counter = 0;
		for (int i = 0; i <= size; i++) {
			for (int j = 0; j < (int) Math.pow(2, i); j++) {
				result.add(fact.getOWLClassAssertionAxiom(clsses[j], inds[ind_counter]));
				result.add(fact.getOWLDataPropertyAssertionAxiom(dataProps[j], inds[ind_counter], ind_counter));

				if (i < size) {
					result.add(fact.getOWLObjectPropertyAssertionAxiom(props[j], inds[ind_counter],
							inds[(ind_counter + 1) * 2 - 1]));
					result.add(fact.getOWLObjectPropertyAssertionAxiom(props[j], inds[ind_counter],
							inds[(ind_counter + 1) * 2]));
				}
				ind_counter++;
			}
		}
		return result;
	}
}
