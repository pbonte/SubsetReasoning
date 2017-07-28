/**
 * 
 */
package be.ugent.ibcn.subsetreasoner.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static OntModel getOntologyModel(OWLOntologyManager manager, OWLOntology ontology){
		OntModel noReasoningModel = null;
		
		noReasoningModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		noReasoningModel.getDocumentManager().setProcessImports(false);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			manager.saveOntology(ontology, new RDFXMLDocumentFormat(),out);
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
	public static OntModel getOntologyModel(OWLOntologyManager manager, Set<OWLAxiom> axioms){
		OntModel model= null;
		try {
			OWLOntology temp = manager.createOntology();
			model = getOntologyModel(manager, temp);
			manager.removeOntology(temp);
		} catch (OWLOntologyCreationException e) {
			logger.error("Unable to create empty ontology",e);
		}
		return model;
	}

}
