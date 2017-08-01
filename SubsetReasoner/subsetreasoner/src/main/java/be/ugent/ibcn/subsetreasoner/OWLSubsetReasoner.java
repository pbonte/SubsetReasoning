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
	}

	public boolean addEvent(Set<OWLAxiom> event, String streamURI) {
		// convert axioms to model
		// ds.addNamedModel(streamURI,model)
		Set<OWLAxiom> results = extractor.extract(event);

		manager.addAxioms(emptyAontology, results);
		OWLUtils.saveOntology(emptyAontology, "subset_premat.owl");

		// 1)materialize new subset, however make sure only to materialize the
		// subset
		OWLOntology subsetOnt = Materializer.materialize(results, reasoner);
		OWLUtils.saveOntology(subsetOnt, "subset.owl");
		// 2)convert to model
		OntModel subsetModel = OWLJenaTranslator.getOntologyModel(subsetOnt.getOWLOntologyManager(), subsetOnt);
		// 3) remove the axioms form the onotlogy
		manager.removeAxioms(emptyAontology, results);
		// 4)add as new namedmoded to the dataset
		return addEvent(subsetModel, streamURI);
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
