QTL TableMiner++
================
A significant amount of experimental information about [_Quantitative Trait Locus_](https://en.wikipedia.org/wiki/Quantitative_trait_locus) (QTL) studies are described in tables of scientific articles. Briefly, a QTL is a genomic region that correlates with a trait (phenotype). _QTL TableMiner++_ is a command-line tool that can retrieve and semantically annotate results of QTL mapping experiments commonly "buried" in (heterogenous) tables. It requires full-text articles (in XML) available from the [Europe PMC](https://europepmc.org/) repository of life sciences literature.


Requirements
------------
* Java 1.7
* SQLite 3.x
* Apache Solr 6.x
* Ontologies/dictionaries (e.g. [Plant Trait Ontology](http://www.ontobee.org/ontology/PATO), [STATistics Ontology](http://www.ontobee.org/ontology/STATO), [ChEBI](https://www.ebi.ac.uk/chebi/))
* full-text article(s) in XML

Installation
------------


Example use
-----------

* _Input_: one or more articles in XML files or PMC IDs
* _Output_: tabulated data in CSV and/or SQLite database file (*.db)

`java -jar QTM.jar PMC4266912`
