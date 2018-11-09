/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * @author pbonte
 *
 */
public class SubsetReasonerTest {

	
	@Test
	public void testSubSetReasoning() throws Exception{
		String matIRI = "precomputed_1ward.owl";
		String normalIRI = "1ward.owl";
		String smallEventIRI = "event.owl";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		String query = new String(Files.readAllBytes(Paths.get("resources/query_smallEvent.q")));
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		OWLOntology matOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(matIRI));
		OWLOntology normalOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(normalIRI));
		OWLOntology smallEventOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(smallEventIRI));


		OWLSubsetReasoner reasoner = new OWLSubsetReasoner(normalOnt, matOnt, Collections.singletonList(query),null,3);
		reasoner.addEvent(smallEventOnt.getAxioms(), "call");
		List<Map<String,String>> result = reasoner.query(query);
		if(result.size()>0){
			Map<String,String> map = result.iterator().next();
			boolean contains = map.containsKey("s");
			assertTrue(contains);
			if(contains){
				assertEquals(map.get("s").toString(),"http://www.semanticweb.org/pbonte/ontologies/2017/7/untitled-ontology-175#Call_test");
			}
		}



	}
	@Test
	public void testSubSetReasoning2() throws Exception{
		String matIRI = "precomputed_1ward.owl";
		String normalIRI = "1ward.owl";
		String eventIRI = "8SceneMarieCorridor.owl";
		String event2IRI = "8SceneMarieCorridor.owl";
		String smallEventIRI = "event.owl";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		String queryLoc = "query.q";
		String query = new String(Files.readAllBytes(Paths.get("resources/query_smallEvent.q")));
		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		OWLOntology matOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(matIRI));
		OWLOntology normalOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(normalIRI));
		OWLOntology eventOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(eventIRI));
		OWLOntology event2Ont = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(event2IRI));
		OWLOntology smallEventOnt = manager.loadOntologyFromOntologyDocument(classloader.getResourceAsStream(smallEventIRI));


		OWLSubsetReasoner reasoner = new OWLSubsetReasoner(normalOnt, matOnt, Collections.singletonList(query),null,3);

		for(int i = 0 ; i<10 ;i++){
			long time1 = System.currentTimeMillis();
			reasoner.addEvent(smallEventOnt.getAxioms(), "loction");
			reasoner.query();
			System.out.println("Total time: " + (System.currentTimeMillis() - time1));

		}

	}
}
