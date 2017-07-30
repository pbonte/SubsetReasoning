/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.List;
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

	public OWLSubsetReasoner(OWLOntology ontology, List<String> queries) {
		this.ontology = ontology;//static ontology
		this.queries = queries;
		this.ds = DatasetFactory.create();
		Configuration c = new Configuration();
		c.ignoreUnsupportedDatatypes=true;
		this.reasoner = new Reasoner(c, ontology);
		this.extractor = new SubsetExtractor(ontology,3);
		this.manager = ontology.getOWLOntologyManager();
	}

	public boolean addEvent(Set<OWLAxiom> event, String streamURI) {
		// convert axioms to model
		// ds.addNamedModel(streamURI,model)
		Set<OWLAxiom> results = extractor.extract(event);
		//TODO
		//0) add new axioms in event to ontology, make sure to remove only those that were not present before adding
		Set<OWLAxiom> uniqueAxioms = OWLUtils.getUniqueAxioms(ontology, event);
		manager.addAxioms(ontology, uniqueAxioms);
		//1)materialize new subset, however make sure only to materialize the subset
		OWLOntology subsetOnt = Materializer.materialize(results, reasoner);
		//2)convert to model
		OntModel subsetModel = OWLJenaTranslator.getOntologyModel(subsetOnt.getOWLOntologyManager(), subsetOnt);
		//3) remove the axioms form the onotlogy
		manager.removeAxioms(ontology, uniqueAxioms);
		//4)add as new namedmoded to the dataset
		return addEvent(subsetModel,streamURI);
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
