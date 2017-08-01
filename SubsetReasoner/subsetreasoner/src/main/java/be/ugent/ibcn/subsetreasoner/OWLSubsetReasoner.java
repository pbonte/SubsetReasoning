/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import be.ugent.ibcn.subsetreasoner.UpdatePolicyExecutor.UpdatePolicy;
import be.ugent.ibcn.subsetreasoner.util.OWLJenaTranslator;
import be.ugent.ibcn.subsetreasoner.util.OWLUtils;

/**
 * @author pbonte
 *
 */
public class OWLSubsetReasoner {

	private OWLOntology ontology;
	private List<String> queries;
	private Dataset ds;
	private SubsetExtractor extractor;
	private Reasoner reasoner;
	private OWLOntologyManager manager;
	private OWLOntology emptyAontology;
	private Map<String, UpdatePolicy> updatePolicies;
	private Map<String,Set<OWLAxiom>> currentView;

	public OWLSubsetReasoner(OWLOntology ontology, OWLOntology materializedOntology, List<String> queries) {
		this.ontology = ontology;// static ontology
		this.emptyAontology = OWLUtils.removeABox(OWLUtils.copyOntology(ontology));
		this.queries = queries;
		this.ds = DatasetFactory.create(
				OWLJenaTranslator.getOntologyModel(materializedOntology.getOWLOntologyManager(), materializedOntology));
		Configuration c = new Configuration();
		c.ignoreUnsupportedDatatypes = true;
		this.reasoner = new Reasoner(c, emptyAontology);
		this.extractor = new SubsetExtractor(materializedOntology, 3);
		this.manager = ontology.getOWLOntologyManager();
		this.updatePolicies = new HashMap<String,UpdatePolicy>();
		this.currentView = new HashMap<String,Set<OWLAxiom>>();
	}
	public void addUpdatePolicy(String stream, UpdatePolicy policy){
		updatePolicies.put(stream, policy);
	}
	public boolean addEvent(Set<OWLAxiom> event, String streamURI) {
		//0)update event stream through update policy
		Set<OWLAxiom> currentViewEvent = updateCurrentViewEvent(event,streamURI);		
		//1)extract the subset from the materialized base
		Set<OWLAxiom> results = extractor.extract(currentViewEvent);
		//add axioms to the ontology used for reasoning
		manager.addAxioms(emptyAontology, results);

		// 2)materialize new subset, however make sure only to materialize the
		// subset
		OWLOntology subsetOnt = Materializer.materialize(results, reasoner);
		OWLUtils.saveOntology(subsetOnt, "subset.owl");
		// 3)convert to model
		OntModel subsetModel = OWLJenaTranslator.getOntologyModel(subsetOnt.getOWLOntologyManager(), subsetOnt);
		// 4) remove the axioms form the onotlogy
		manager.removeAxioms(emptyAontology, results);
		// 5)add as new namedmoded to the dataset
		return addEvent(subsetModel, streamURI);
	}

	private Set<OWLAxiom> updateCurrentViewEvent(Set<OWLAxiom> event, String streamURI){
		Set<OWLAxiom> currentViewEvent = null;
		if(!updatePolicies.containsKey(streamURI)){
			currentViewEvent=event;
		}else{
			if(!currentView.containsKey(streamURI)){
				currentView.put(streamURI, event);
				currentViewEvent = event;
			}else{
				Set<OWLAxiom> oldView = currentView.get(streamURI);
				currentViewEvent = UpdatePolicyExecutor.update(oldView, event, updatePolicies.get(streamURI));
			}
		}
		return currentViewEvent;
	}
	public boolean addEvent(Model event, String streamURI) {
		ds.addNamedModel(streamURI, event);
		return true;
	}

	public List<Map<String,String>> query(String queryString) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();

		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
			ResultSet result = qexec.execSelect();
			while (result != null && result.hasNext()) {			
				Map<String, String> tempMap = new HashMap<String, String>();
				
				QuerySolution solution = result.next();
				Iterator<String> it = solution.varNames();
				
				// Iterate over all results
				while (it.hasNext()) {
					String varName = it.next();
					String varValue = solution.get(varName).toString();
					tempMap.put(varName, varValue);
					
				}
				
				// Only add if we have some objects in temp map
				if (tempMap.size() > 0) {
					results.add(tempMap);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public List<ResultSet> query() {
		List<ResultSet> results = new ArrayList<ResultSet>();
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		for (String queryString : queries) {
			Query query = QueryFactory.create(queryString);
			try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
				ResultSet result = qexec.execSelect();
				results.add(result);
			}
		}
		return results;
	}

}
