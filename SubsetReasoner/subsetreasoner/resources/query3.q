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
PREFIX task: <http://orca.test/ontology/TaskAccio.owl#>
PREFIX rolecomp: <http://orca.test/ontology/RoleCompetenceAccio.owl#>

CONSTRUCT{
	?s wsnadj:hasValue '1.0'^^xsd:float
}
WHERE { 
	
	?p rdf:type profile:Person .
	?p profile:hasRole ?role .
	?role rdf:type rolecomp:StaffMember .
	?p context:hasLocation ?l .
	?l rdf:type task:RoomWithPatientorResident.
	?l context:containsSystem ?s .
	?s rdf:type wsnext:UnderBedLight .
	?s wsnadj:hasValue '0.0'^^xsd:float
}