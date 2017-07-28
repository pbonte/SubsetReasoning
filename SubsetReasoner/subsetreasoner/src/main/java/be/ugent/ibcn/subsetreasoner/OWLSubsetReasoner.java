/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.List;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import be.ugent.ibcn.subsetreasoner.util.OWLJenaTranslator;

/**
 * @author pbonte
 *
 */
public class OWLSubsetReasoner {

	private OWLOntology ontology;
	private List<String> queries;
	private Dataset ds;
	private SubsetExtractor extractor;

	public OWLSubsetReasoner(OWLOntology ontology, List<String> queries) {
		this.ontology = ontology;//static ontology
		this.queries = queries;
		this.ds = DatasetFactory.create();
		this.extractor = new SubsetExtractor(ontology,3);
	}

	public boolean addEvent(Set<OWLAxiom> event, String streamURI) {
		// convert axioms to model
		// ds.addNamedModel(streamURI,model)
		Set<OWLAxiom> results = extractor.extract(event);
		//TODO
		//1)materialize new subset, however make sure only to materialize the subset
		//2)convert to model
		//3)add as new namedmoded to the dataset
		//4)query that MF
		Model model = OWLJenaTranslator.getOntologyModel(manager, axioms);
		return addEvent(model, streamURI);
	}

	public boolean addEvent(Model event, String streamURI) {
		ds.addNamedModel(streamURI, event);
		return true;
	}

	public void query() {
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		String queryString = "Select * WHERE { ?s <http://purl.org/dc/elements/1.1/author> ?o."
				+ " ?o <http:test/hasProp> ?t} LIMIT 10";
		Query query = QueryFactory.create(queryString);
		try (QueryExecution qexec = QueryExecutionFactory.create(query, merge)) {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				System.out.println(soln);

			}
		}
	}

}
