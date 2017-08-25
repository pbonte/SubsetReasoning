/**
 * 
 */

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.ibcn.subsetreasoner.OWLSubsetReasoner;
import be.ugent.ibcn.subsetreasoner.UpdatePolicyExecutor.UpdatePolicy;

/**
 * @author pbonte
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int wards = 1;
		if (args.length > 0) {
			wards = Integer.parseInt(args[0].trim());
		}
		System.out.println("Running for " + wards + " wards");
		String matIRI = "computed-" + wards + "-wards.owl";
		String normalIRI = "normal-" + wards + "-wards.owl";
		String[] setup = new String[] { "SetUpWorking", "0SetUpKarelPatient", "1SetUpLisaCorridor",
				"2SetUpMariePatient", "3SetUpPersonalRelation" };
		String[] setupStream = new String[] { "working", "location", "location", "location", "relation" };
		String[] reset = new String[] { "4ResetMariePatient" };
		String[] resetStream = new String[] { "location" };

		String[] scene = new String[] { "5SceneCallLaunchedSarah", "6SceneCallRedirectLisaMedical",
				"7SceneCallTempAcceptMarieMedical", "8SceneMarieCorridor", "9SceneMariePatient",
				"10SceneMariePresenceOn", "11SceneMariePresenceOff", "12SceneMarieCorridor" };
		String[] sceneStream = new String[] { "call", "call", "call", "location", "location", "presence", "presence",
				"location" };

		String[] setupDelQuery = new String[1];
		setupDelQuery[0] = getFile("query_setupLights.q");
		String[] setupUpdateQuery = new String[1];
		setupUpdateQuery[0] = getFile("query_setupLights2.q");
		String getLights = getFile("query_setupLights3.q");
		String getLights2 = getFile("query_lights.q");

		String[] setupStreams = new String[] { "lights" };

		OWLOntologyManager manager = OWLManager.createConcurrentOWLOntologyManager();
		OWLOntology[] setupOnts = new OWLOntology[setup.length];
		for (int i = 0; i < setup.length; i++) {
			setupOnts[i] = manager.loadOntologyFromOntologyDocument(Main.class.getResourceAsStream(setup[i] + ".owl"));
		}

		OWLOntology[] resetOnts = new OWLOntology[reset.length];
		for (int i = 0; i < reset.length; i++) {
			resetOnts[i] = manager.loadOntologyFromOntologyDocument(Main.class.getResourceAsStream(reset[i] + ".owl"));
		}
		OWLOntology[] sceneOnts = new OWLOntology[scene.length];
		for (int i = 0; i < scene.length; i++) {
			sceneOnts[i] = manager.loadOntologyFromOntologyDocument(Main.class.getResourceAsStream(scene[i] + ".owl"));
		}
		String[] queries = new String[7];
		queries[0] = getFile("query1.q");
		queries[1] = getFile("query2.q");
		queries[2] = getFile("query3.q");
		queries[3] = getFile("query4.q");
		queries[4] = getFile("query5.q");
		queries[5] = getFile("query_spocLights.q");
		queries[6] = getFile("query_spocLightOff.q");

		String[] updates = new String[queries.length];
		updates[0] = "out";
		updates[1] = "out";
		updates[2] = "lights";
		updates[3] = "call";
		updates[4] = "out";
		updates[5] = "lights";
		updates[6] = "lights";

		OWLOntology matOnt = manager.loadOntologyFromOntologyDocument(Main.class.getResourceAsStream(matIRI));
		OWLOntology normalOnt = manager.loadOntologyFromOntologyDocument(Main.class.getResourceAsStream(normalIRI));

		OWLSubsetReasoner reasoner = new OWLSubsetReasoner(normalOnt, matOnt, Arrays.asList(queries),
				Arrays.asList(updates));
		reasoner.addUpdatePolicy("call", UpdatePolicy.UPDATE);
		reasoner.addUpdatePolicy("location", UpdatePolicy.UPDATE);
		reasoner.addUpdatePolicy("presence", UpdatePolicy.UPDATE);
		reasoner.addUpdatePolicy("lights", UpdatePolicy.UPDATE);

		for (int step = 0; step < 35; step++) {
			System.out.println("##############\n#Step: " + step);
			// set up queries
			for (int i = 0; i < setupDelQuery.length; i++) {
				Model m = reasoner.queryConstruct(setupDelQuery[i]);
				reasoner.setupDelete(m);
				for(int w = 0; w <= wards; w++){
					Model n = reasoner.queryConstruct(String.format(setupUpdateQuery[i],w));
					reasoner.addEvent(n,setupStreams[i],getLights2);
					//reasoner.addEvent(n,setupStreams[i]);
				}

			}
			for (int i = 0; i < setupOnts.length; i++) {
				reasoner.addEvent(setupOnts[i].getAxioms(), setupStream[i]);
			}
			for (int i = 0; i < sceneOnts.length; i++) {
				long time1 = System.currentTimeMillis();
				int size = 0;
				int size2 = 0;
				size = reasoner.addEvent(sceneOnts[i].getAxioms(), sceneStream[i]);
				int j = 0;
				for (String query : queries) {
					int counter = j++;
					if (counter == 3 || counter == 2 || counter == 5 || counter == 6) {
						Model result = reasoner.queryConstruct(query);
						if (updates[counter].equals("out")) {
							System.out.println("sending out");
							System.out.println(result);
						} else if (result != null && result.size() > 0) {
							if(updates[counter].equals("lights")){
								size2 = reasoner.addEvent(result, updates[counter],getLights2);
							}else{
								size2 = reasoner.addEvent(result, updates[counter]);
							}
							System.out.println("updating " + updates[counter] + result);
						}
					} else {
						List<Map<String, String>> result = reasoner.query(query);
						System.out.print("Results " + scene[i] + " query " + counter + ": ");
						System.out.println(result);
					}
				}
				System.out.println("time: " + (System.currentTimeMillis() - time1));
				log("", step, scene[i], (System.currentTimeMillis() - time1), size, size2);
			}
		}
	}

	public static void log(String text, int iteration, String component, long time, int size, int size2) {
		System.out.println(String.format("LOG\t%d\tCOMPONENT:\t%s\tTIME:\t%d\tTEXT:\t%sSUBSEIZE:\t%d\tSUBSIZE2:\t%d",
				iteration, component, time, text, size, size2));
	}

	public static String getFile(String fileName) {
		InputStream in = Main.class.getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return reader.lines().collect(Collectors.joining());
	}

}
