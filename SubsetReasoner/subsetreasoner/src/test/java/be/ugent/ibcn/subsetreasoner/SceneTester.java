/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.ibcn.subsetreasoner.UpdatePolicyExecutor.UpdatePolicy;

/**
 * @author pbonte
 *
 */
public class SceneTester {

	@Test
	public void scene7() throws Exception{
		String ontIri="file:///home/pbonte/Github/SubsetReasoning/data/Events/";
		String matIRI = "file:///home/pbonte/Github/SubsetReasoning/SubsetReasoner/subsetreasoner/resources/precomputed_1ward.owl";
		String normalIRI = "file:///home/pbonte/Github/SubsetReasoning/SubsetReasoner/subsetreasoner/resources/1ward.owl";
		String[] setup = new String[]{"0SetUpKarelPatient","1SetUpLisaCorridor","2SetUpMariePatient","3SetUpPersonalRelation"};
		String[] setupStream = new String[]{"location","location","location","relation"};
		String[] reset = new String[]{"4ResetMariePatient"};
		String[] resetStream = new String[]{"location",};

		String[] scene = new String[]{"5SceneCallLaunchedSarah","6SceneCallRedirectLisaMedical","7SceneCallTempAcceptMarieMedical","8SceneMarieCorridor",
				"9SceneMariePatient","10SceneMariePresenceOn","11SceneMariePresenceOff","12SceneMarieCorridor"};
		String[] sceneStream = new String[]{"call","call","call","location","location","presence","presence","location"};

		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		OWLOntology[] setupOnts = new OWLOntology[setup.length];
		for(int i = 0 ; i < setup.length;i++){
			setupOnts[i] = manager.loadOntology(IRI.create(ontIri+setup[i]+".owl")); 		
		}
		OWLOntology[] resetOnts = new OWLOntology[reset.length];
		for(int i = 0 ; i < reset.length;i++){
			resetOnts[i] = manager.loadOntology(IRI.create(ontIri+reset[i]+".owl")); 
		}
		OWLOntology[] sceneOnts = new OWLOntology[scene.length];
		for(int i = 0 ; i < scene.length;i++){
			sceneOnts[i] = manager.loadOntology(IRI.create(ontIri+scene[i]+".owl")); 
		}
		String query = new String(Files.readAllBytes(Paths.get("resources/query1.q")));
		OWLOntology matOnt = manager.loadOntologyFromOntologyDocument(IRI.create(matIRI));
		OWLOntology normalOnt = manager.loadOntologyFromOntologyDocument(IRI.create(normalIRI));

		OWLSubsetReasoner reasoner = new OWLSubsetReasoner(normalOnt, matOnt, Collections.singletonList(query));
		for(int i = 0 ; i< setupOnts.length;i++){
			reasoner.addEvent(setupOnts[i].getAxioms(), setupStream[i]);
		}
		for(int i = 0 ; i< sceneOnts.length;i++){
			reasoner.addEvent(sceneOnts[i].getAxioms(), sceneStream[i]);
			List<Map<String,String>> result = reasoner.query(query);
			System.out.println(result);
			break;
		}
		
	}
}
