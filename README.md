QTL TableMiner++
================
A significant amount of experimental information about [_Quantitative Trait Locus_](https://en.wikipedia.org/wiki/Quantitative_trait_locus) (QTL) studies are described in tables of scientific articles. Briefly, a QTL is a genomic region that correlates with a trait (phenotype). _QTLTableMiner++_ (QTM) is a command-line tool that can retrieve and semantically annotate results of QTL mapping experiments commonly "buried" in (heterogenous) tables. It requires a pmcid of a full-text articles (in XML) available from the [Europe PMC](https://europepmc.org/) as an input. QTM outputs a csv file and a sqlite database file containing list of all QTL statements from tables of an article.


Requirements
------------
* Java 1.7
* SQLite 3.x
* Apache Solr 6.2.1
* QTM specific Solr cores based on  Plant/Trait Ontology, STATO Ontology, ChEBI
* PMCID (identifier of an article from Europe PMC)


Installation
------------

### Installing Solr with core ### 

For Linux user, a bash script /installSolr/installSolr(6.2.1) is provide to Apache Solr 6.2.1 with 5 cores. This script require sudo rights and installs Solr at /opt/Solr and make /var/Solr as your Solr data directory. 



Arguements
----------
-pmc    A list of all pmcids as input. Use comma(,) as a seperator between to ids. For example PMC4266912, PMC2267253

-o	    Filename of the output database. This database is in sqlite format. By default, there is no username and password for the database

-help	  HELP pages for QTL Table Miner++

Example use
-----------
java -jar target/QTM-1.0-SNAPSHOT.jar -pmc PMC4266912
