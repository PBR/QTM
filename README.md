QTLTABLEMiner++ -  Semantic-mining of QTL tables in scientific articles 
===============================================
A quantitative trait locus (QTL) is a region on  the genome that correlates with variation in a phenotype. A significant amount of experimental information about QTL studies are implicitly described in tables of  scientific publications. QTLTableMiner++, is a java based tool that extracts QTL information from (heterogeneous) tables and semantically annotates their content.


Requirements
------------
Java 1.7
SQLite
Apache Solr
Crop Ontology (SP)
Trait Ontology
Stato

Examlple
---------
java -jar QTM.jar PMC4266912


Arguements
------------

Default:    List of PMC ids

-help  :    for using helps


Output
-------------
Tix.db(SQLiteDB file containing a database of the knowledge mined from the tables.)


