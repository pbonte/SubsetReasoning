/**
 * 
 */
package be.ugent.ibcn.subsetreasoner;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * @author pbonte
 *
 */
public class SubsetExtractor {
	
	private OWLOntology ontology;
	private int depth;
	public SubsetExtractor(OWLOntology ontology, int depth){
		this.ontology = ontology;
		this.depth = depth;
	}
	
	public Set<OWLAxiom> extract(Set<OWLAxiom> stream){
		Set<OWLAxiom> extracted = new HashSet<OWLAxiom>(stream);
		for(OWLAxiom ax: stream){
			if(ax instanceof OWLDeclarationAxiom){
				OWLDeclarationAxiom declAx = (OWLDeclarationAxiom)ax;
				OWLNamedIndividual ind = declAx.getEntity().asOWLNamedIndividual();
				if(ontology.containsIndividualInSignature(ind.getIRI())){
					Set<OWLAxiom> retrievedAx = getReferencedAxioms(ind, ontology, depth);
					extracted.addAll(retrievedAx);
				}
			}
		}
		return extracted;
	}
	
	/**
	 * Retrieves the axioms of the linked individuals that have a connection the the rootInd.
	 * The depth of the search for these individuals can be defined. Meaning that depth 1 will 
	 * only retrieve the individuals linked to the rootInd, whereas depth 2 will also retrieve 
	 * the individuals linked to the individuals that are linked to the rootInd, and so on.
	 * @param rootInd		Starting point of the search. The linked individuals to this individual will be retrieved
	 * @param ontology		The ontology in which the axioms from the individuals need to be find.
	 * @param depth			The depth of the search.
	 * @return				The axioms defining the linked individuals.
	 */
	private Set<OWLAxiom> getReferencedAxioms(OWLIndividual rootInd, OWLOntology ontology, int depth){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		if(depth > 0){
			getReferencedAxioms_helper(rootInd, ontology, depth, axioms);
		}
		
		return axioms;
	}
	/**
	 * Helper method for the recursive method getReferencedAxioms
	 * @param rootInd		Temporarily starting point of the search. The linked individuals to this individual will be retrieved
	 * @param ontology		The ontology in which the axioms from the individuals need to be find.
	 * @param depth			The temporarily depth of the search.
	 * @param axioms		The list with axioms, we pass it by reference so we don't need to create new methodes each time.
	 * @return
	 */
	private  void getReferencedAxioms_helper(OWLIndividual rootInd, OWLOntology ontology, int depth, Set<OWLAxiom> axioms){
		OWLDataFactory dfact = ontology.getOWLOntologyManager().getOWLDataFactory();
		OWLNamedIndividual ind = rootInd.asOWLNamedIndividual();
		//first we add all the referencing axioms
		for(OWLAxiom ax: ontology.getReferencingAxioms(ind,false)){
			if(ax instanceof OWLObjectPropertyAssertionAxiom){
				if(((OWLObjectPropertyAssertionAxiom) ax).getSubject().equals(ind)){
					axioms.add(ax);
				}
				
			}else{
				axioms.add(ax);
			}
		}
		
		//then we loop to find the object properties to add the axioms regarding the linked individual
		for(OWLAxiom ax: ontology.getReferencingAxioms(ind)){
			if(ax instanceof OWLObjectPropertyAssertionAxiom && ((OWLObjectPropertyAssertionAxiom) ax).getSubject().equals(ind)){
				OWLObjectPropertyAssertionAxiom axObj = (OWLObjectPropertyAssertionAxiom)ax;
				OWLIndividual objInd = axObj.getObject();
				Stream<OWLClassExpression> test = EntitySearcher.getTypes(objInd, ontology);
				for(OWLClassExpression clsExp : EntitySearcher.getTypes(objInd, ontology).collect(Collectors.toSet())){
					//adding the class assertion of this linked individual
					axioms.add(dfact.getOWLClassAssertionAxiom(clsExp,objInd));
					axioms.add(dfact.getOWLDeclarationAxiom(objInd.asOWLNamedIndividual()));
				}
				//add data properties
				for(Entry<OWLDataPropertyExpression, OWLLiteral> ent:EntitySearcher.getDataPropertyValues(objInd, ontology).entries()){
					axioms.add(dfact.getOWLDataPropertyAssertionAxiom(ent.getKey(), objInd, ent.getValue()));
				}
				if(depth - 1 > 0){
					//if there still is some ground to discover, we dive deeper in the pool with a recursive call
					getReferencedAxioms_helper(objInd, ontology, depth-1,axioms);
				}
							
			}
		}			
	}

}
