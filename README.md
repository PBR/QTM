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


Arguements
----------
-pmc    A list of all pmcids as input. Use comma(,) as a seperator between to ids. For example PMC4266912, PMC2267253

-o	    Filename of the output database. This database is in sqlite format. By default, there is no username and password for the database

-help	  HELP pages for QTL Table Miner ++

Example use
-----------
`java -jar QTM.jar -pmc PMC4266912
