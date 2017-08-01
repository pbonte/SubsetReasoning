PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

PREFIX profile: <http://orca.test/ontology/ProfileAccio.owl#>
PREFIX upper: <http://orca.test/ontology/UpperAccio.owl#>
PREFIX context: <http://orca.test/ontology/ContextAccio.owl#>
PREFIX medical: <http://orca.test/ontology/MedicalAccio.owl#>
PREFIX wsnadj: <http://orca.test/ontology/WSNadjustedAccio.owl#>
PREFIX wsnext: <http://orca.test/ontology/WSNextensionAccio.owl#>
PREFIX rolecomp: <http://orca.test/ontology/RoleCompetenceAccio.owl#>
PREFIX task: <http://orca.test/ontology/TaskAccio.owl#>

SELECT ?p

WHERE { 

	?p rdf:type profile:Person .
	?p profile:hasCurrentRole ?crole .
	?crole rolecomp:isWorking "true"^^xsd:boolean .
	
		
}