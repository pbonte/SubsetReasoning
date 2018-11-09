/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
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
	private List<Query> queries;
	private List<String> queryUpdateStreams;
	private Dataset ds;
	private SubsetExtractor extractor;
	private Reasoner reasoner;
	private OWLOntologyManager manager;
	private OWLOntology emptyAontology;
	private Map<String, UpdatePolicy> updatePolicies;
	private Map<String, Set<OWLAxiom>> currentView;
	private OWLOntology materializedOntology;

	public OWLSubsetReasoner(OWLOntology ontology, OWLOntology materializedOntology, List<String> queries,
			List<String> streams,int subsetsize) {
		this.ontology = ontology;// static ontology
		this.emptyAontology = OWLUtils.removeABox(OWLUtils.copyOntology(ontology));
		OWLUtils.saveOntology(emptyAontology, "empty.owl");
		this.queries = new ArrayList<Query>();
		queries.stream().map(q -> this.queries.add(QueryFactory.create(q)));
		this.queryUpdateStreams = streams;
		this.ds = DatasetFactory.create();
		this.ds.addNamedModel("background",
				OWLJenaTranslator.getOntologyModel(materializedOntology.getOWLOntologyManager(), materializedOntology));
		Configuration c = new Configuration();
		c.ignoreUnsupportedDatatypes = true;
		this.reasoner = new Reasoner(c, emptyAontology);
		this.manager = ontology.getOWLOntologyManager();
		this.updatePolicies = new HashMap<String, UpdatePolicy>();
		this.currentView = new HashMap<String, Set<OWLAxiom>>();
		this.materializedOntology = materializedOntology;
		this.extractor = new SubsetExtractor(this.materializedOntology, subsetsize);
	}

	public void addUpdatePolicy(String stream, UpdatePolicy policy) {
		updatePolicies.put(stream, policy);
	}
	public int addEvent(Model event,String streamURI,String query){
		List<Map<String, String>> results = test(event,query);
		Map<String,String> temp = results.stream().findAny().orElse(Collections.emptyMap());
		String result=temp.get(temp.keySet().stream().findAny().orElse("none"));
		if(result != null && !result.equals("none")){
			return addEvent(event,streamURI+result);
		}else{
			return 0;
		}
	}
	public int addEvent(Model event, String streamURI) {
		OWLOntology ont = OWLJenaTranslator.getOWLOntology(event);
		Set<OWLAxiom> axes = OWLJenaTranslator.checkForIncorrectAnnotations(ont.getAxioms(), emptyAontology);
		return addEvent(axes, streamURI);
	}
	public List<Map<String, String>> test(Model event,String query){
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		
		Dataset test = DatasetFactory.create(merge);
		test.addNamedModel("http://stream", event);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, test)) {		
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

	public int addEvent(Set<OWLAxiom> event, String streamURI) {
		// 0)update event stream through update policy
		Set<OWLAxiom> currentViewEvent = updateCurrentViewEvent(event, streamURI);
		// 1)extract the subset from the materialized base
		Set<OWLAxiom> results = extractor.extract(currentViewEvent);
		// add axioms to the ontology used for reasoning
		manager.addAxioms(emptyAontology, results);
		// 2)materialize new subset, however make sure only to materialize the
		// subset
		int subsetSize = results.size();
		OWLOntology subsetOnt = Materializer.materialize(results, reasoner);
		//OWLUtils.saveOntology(subsetOnt, "subset.owl");
		// 3)convert to model
		OntModel subsetModel = OWLJenaTranslator.getOntologyModel(subsetOnt.getOWLOntologyManager(), subsetOnt);
		// 4) remove the axioms form the onotlogy
		manager.removeAxioms(emptyAontology, results);
		// 5)remove current named model if exists
		removeEvent(streamURI);
		// 6)add as new namedmoded to the dataset
		addEventToGlobalModel(subsetModel, streamURI);
		return subsetSize;
	}

	public void setupDelete(Model del) {
		// delete from materialized ont used for subset calculation
		OWLOntology ont = OWLJenaTranslator.getOWLOntology(del);
		Set<OWLAxiom> axes = OWLJenaTranslator.checkForIncorrectAnnotations(ont.getAxioms(), emptyAontology);
		materializedOntology.getOWLOntologyManager().removeAxioms(materializedOntology, axes);
		// delete from materialized ont used for querying
		ds.getNamedModel("background").remove(del);

	}

	private Set<OWLAxiom> updateCurrentViewEvent(Set<OWLAxiom> event, String streamURI) {
		Set<OWLAxiom> currentViewEvent = null;
		if (!updatePolicies.containsKey(streamURI)) {
			currentViewEvent = event;
			updatePolicies.put(streamURI, UpdatePolicy.UPDATE);
			currentView.put(streamURI, currentViewEvent);

		} else {
			if (!currentView.containsKey(streamURI)) {
				currentView.put(streamURI, event);
				currentViewEvent = event;
			} else {
				Set<OWLAxiom> oldView = currentView.get(streamURI);
				currentViewEvent = UpdatePolicyExecutor.update(oldView, event, updatePolicies.get(streamURI));
				currentView.put(streamURI, currentViewEvent);
			}
		}
		return currentViewEvent;
	}

	private boolean addEventToGlobalModel(Model event, String streamURI) {
		ds.addNamedModel(streamURI, event);
		return true;
	}

	public boolean removeEvent(String streamURI) {
		if (ds.containsNamedModel(streamURI)) {
			ds.removeNamedModel(streamURI);
			return true;
		}
		return false;
	}

	public List<Map<String, String>> query(Query query) {
		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
			if (query.isSelectType()) {
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
			} else {
				Model result = qexec.execConstruct();
				System.out.println(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	public Model queryConstruct(Query query) {
		Model result = null;
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
			result = qexec.execConstruct();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<Map<String, String>> query(String queryString) {
		Query query = QueryFactory.create(queryString);
		return query(query);
	}

	public Model queryConstruct(String queryString) {
		Query query = QueryFactory.create(queryString);
		return queryConstruct(query);
	}
	public Dataset queryConstructQuad(String queryString) {
		Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
		return queryConstructQuad(query);
	}
	public Dataset queryConstructQuad(Query query) {
		Dataset result = null;
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
			result = qexec.execConstructDataset();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public List<ResultSet> query() {
		List<ResultSet> results = new ArrayList<ResultSet>();
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		for (Query query : queries) {
			try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
				ResultSet result = qexec.execSelect();
				results.add(result);
			}
		}
		return results;
	}

}
