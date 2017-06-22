/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * @author pbonte
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//https://jena.apache.org/documentation/tdb/datasets.html
		//TODO how expensive is the merge operator...
		//		Its cheap: Merge time: 0 -> perfect option
		
		
		// TODO Auto-generated method stub
		Model model = ModelFactory.createDefaultModel() ;
		model.read("file:///tmp/test1.nt") ;
		Model model2 = ModelFactory.createDefaultModel() ;
		model2.read("file:///tmp/test2.nt") ;
		Model model3 = ModelFactory.createDefaultModel() ;
		model3.read("file:///tmp/test3.nt") ;
		
		Model model4 = ModelFactory.createDefaultModel() ;
		model4.read("file:///tmp/citybench.rdf") ;
		Model model5 = ModelFactory.createDefaultModel() ;
		model5.read("file:///tmp/citybench2.rdf") ;
		Dataset ds = DatasetFactory.create();
		
		ds.addNamedModel("test", model);
		ds.addNamedModel("test2", model2);
//		ds.addNamedModel("test3", model3);
		long time1 = System.currentTimeMillis();
		Model merge = ds.getNamedModel("urn:x-arq:UnionGraph");
		System.out.println("Merge time: " + (System.currentTimeMillis() - time1));
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
