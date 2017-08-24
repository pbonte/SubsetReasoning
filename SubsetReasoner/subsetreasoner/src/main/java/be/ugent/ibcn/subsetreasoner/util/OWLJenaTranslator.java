/**
 * 
 */
package be.ugent.ibcn.subsetreasoner.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataPropertyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * @author pbonte
 *
 */
public class OWLJenaTranslator {
	final static Logger logger = LoggerFactory.getLogger(OWLJenaTranslator.class);

	public static OWLOntology getOWLOntology(final Model model) {
		OWLOntology ontology;
		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is)) {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.write(os, "TURTLE", null);
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			ontology = man.loadOntologyFromOntologyDocument(is);
			return ontology;
		} catch (Exception e) {
			throw new RuntimeException("Could not convert JENA API model to OWL API ontology.", e);
		}
	}

	public static OntModel getOntologyModel(OWLOntologyManager manager, OWLOntology ontology) {
		OntModel noReasoningModel = null;

		noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		noReasoningModel.getDocumentManager().setProcessImports(false);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			manager.saveOntology(ontology, new RDFXMLDocumentFormat(), out);
		} catch (OWLOntologyStorageException e) {
			logger.error("Unable to write ontology to stream");
		}

		try {
			noReasoningModel.read(new ByteArrayInputStream(out.toByteArray()), "RDF/XML");
		} catch (Exception e) {
			logger.error("Problems reading stream. Might be ignored");
		}

		return noReasoningModel;
	}

	public static OntModel getOntologyModel(OWLOntologyManager manager, Set<OWLAxiom> axioms) {
		OntModel model = null;
		try {
			OWLOntology temp = manager.createOntology();
			model = getOntologyModel(manager, temp);
			manager.removeOntology(temp);
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to create empty ontology", e);
		}
		return model;
	}

	public static Set<OWLAxiom> checkForIncorrectAnnotations(Set<OWLAxiom> axioms, OWLOntology ontology) {
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		Set<OWLOntology> allOnts = ontology.getImports();
		allOnts.add(ontology);
		for (OWLAxiom ax : axioms) {
			boolean found = false;
			for (OWLOntology ont : allOnts) {
				if (ax instanceof OWLAnnotationAssertionAxiom) {
					OWLAnnotationAssertionAxiom anno = (OWLAnnotationAssertionAxiom) ax;
					OWLIndividual subject = new OWLNamedIndividualImpl(IRI.create(anno.getSubject().toString()));
					if (ont.containsObjectPropertyInSignature(anno.getProperty().getIRI())) {
						OWLIndividual object = new OWLNamedIndividualImpl(IRI.create(anno.getValue().toString()));
						OWLObjectProperty objProp = new OWLObjectPropertyImpl(anno.getProperty().getIRI());

						newAxioms.add(manager.getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(objProp, subject,
								object));
						found = true;
						break;
					} else if (ont.containsDataPropertyInSignature(anno.getProperty().getIRI())) {
						OWLDataProperty dataProp = new OWLDataPropertyImpl(anno.getProperty().getIRI());
						newAxioms.add(manager.getOWLDataFactory().getOWLDataPropertyAssertionAxiom(dataProp, subject,
								(OWLLiteral) anno.getValue()));
						found = true;
						break;
					}
				}
			}
			// if(ax instanceof OWLDeclarationAxiom){
			// System.out.println(ax);
			// found = true;
			// }
			if (!found) {
				newAxioms.add(ax);
			}
		}

		return newAxioms;
	}
}
