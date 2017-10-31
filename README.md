# QTL TableMiner++

[![Build Status](https://travis-ci.org/candYgene/QTM.svg?branch=master)](https://travis-ci.org/candYgene/QTM)

A significant amount of experimental information about [_Quantitative Trait Locus_](https://en.wikipedia.org/wiki/Quantitative_trait_locus) (QTL) studies are described in (heterogenous) tables of scientific articles. Briefly, a QTL is a genomic region that correlates with a trait of interest (phenotype). _QTLTableMiner++_ (QTM) is a command-line tool to retrieve and semantically annotate results obtained from QTL mapping experiments. QTM takes full-text articles from the [Europe PMC](https://europepmc.org/) repository as input and writes the extracted QTLs onto CSV or SQLite database file.

## Requirements

* Java 1.7
* [SQLite](https://sqlite.org/) 3.x
* [Apache Maven](https://maven.apache.org/) 3.x
* [Apache Solr](https://lucene.apache.org/solr/) 6.x with domain-specific controlled vocabularies & ontologies for Solr _cores_:
  * [Plant Trait Ontology](http://www.ontobee.org/ontology/PATO)
  * [STATistics Ontology](http://www.ontobee.org/ontology/STATO)
  * [ChEBI](https://www.ebi.ac.uk/chebi/)
* full-text articles (or PMCIDs) from [Europe PMC](https://europepmc.org/)
* Operating System: Linux

## Install

For Linux user, a bash script /installSolr/installSolr(6.2.1) is provide to Apache Solr 6.2.1 with 5 cores. This script require sudo rights and installs Solr at /opt/Solr and make /var/Solr as your Solr data directory.

## Usage

java -jar target/QTM-1.0-SNAPSHOT.jar -pmc PMC4266912 -o results_PMC4266912

## Command-line options

-pmc    A list of all pmcids as input. Use comma(,) as a separator between to ids. For example PMC4266912, PMC2267253

-o	Filename of the SQLite output database. By default, name of the database is resultQTM.db and there is no username and password for the database

-csv	Filename of the csv output file. By default, name of the file is results_QTM

-help	  HELP pages for QTL Table Miner++
