# SubsetReasoning
Approximate OWL Subset reasoning for streaming ontology data.
The subset reasoner tries to minimize the data needed to reason over streaming ontology data. To do this, the subset reasoner incorporates the notion of update policies, that define the influence of the streaming data on current view on the data.

## How to run (with maven):
~~~~
cd SubsetReasoner/subsetreasoner/
mvn clean install
java -jar target/subsetreasoner-0.0.1-SNAPSHOT-jar-with-dependencies.jar
~~~~
This will execute a predefined example.
To incorporate your own example, adapt Main.java (in the be.ugent.ibcn.run package).

## How to run your own code:
Note that the subset reasoner uses the OWL API.
First, initialize the SubsetReasoner:
~~~
OWLSubsetReasoner reasoner = new OWLSubsetReasoner(ontology, matOntology, Arrays.asList(queries), Arrays.asList(streams));
~~~
With ontology the ontology, matOntology the materialized ontology, a list of queries and a list of streams. If you don't have a materialized version of your ontology you can materialize it using the following code fragment:
~~~
Reasoner reasoner = new Reasoner(ontology);
OWLOntology matOntology = Materializer.materialize(ontology, reasoner);
~~~

Define the update policy that best matches the changes in the stream:
~~~
reasoner.addUpdatePolicy(someStream, UpdatePolicy.UPDATE);
~~~
You can add data to a specific stream:
~~~
reasoner.addEvent(event.getAxioms(), someStream);
~~~
Query the data:
~~~
reasoner.query(); // executes all the queries defined in the constructor
reasoner.query(specificQuery); // executes a specific query
reasoner.queryConstruct(specificConstructQuery); // executes a specific construct query
~~~

