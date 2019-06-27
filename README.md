# QTL TableMiner++ (QTM)

[![Build Status](https://travis-ci.org/candYgene/QTM.svg?branch=dev)](https://travis-ci.org/candYgene/QTM)

## Description
A significant amount of experimental information about [_Quantitative Trait Locus_](https://en.wikipedia.org/wiki/Quantitative_trait_locus) (QTL) studies are described in (heterogenous) tables of scientific articles. Briefly, a QTL is a genomic region that correlates with a trait of interest (phenotype). _QTM_ is a command-line tool to retrieve and semantically annotate results obtained from QTL mapping experiments. It takes full-text articles from the [Europe PMC](https://europepmc.org/) repository as input and outputs the extracted QTLs into a relational database (SQLite) and text file (CSV).

## Requirements

* [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) 1.7 or later
* [Apache Maven](https://maven.apache.org/) 3.x
* [SQLite](https://sqlite.org/) 3.x
* [Apache Solr](https://lucene.apache.org/solr/) 6.x with domain-specific vocabularies and ontologies (_Solr cores_):
  * [Gene Ontology](http://www.ontobee.org/ontology/GO) (GO)
  * [Plant Trait Ontology](http://www.ontobee.org/ontology/TO) (TO)
  * [Phenotypic quality ontology](http://www.ontobee.org/ontology/PATO) (PATO)
  * [Solanaceae Phenotype Ontology](http://purl.bioontology.org/ontology/SPTO) (SPTO)
  * [STATistics Ontology](http://www.ontobee.org/ontology/STATO) (STATO)
  * [Chemical Entities of Biological Interest](https://www.ebi.ac.uk/chebi/) (ChEBI)
* access to full-text articles (in XML) from [Europe PMC](https://europepmc.org/)

## Installation

```
git clone https://github.com/PBR/QTM.git
cd QTM
mvn install
solr/install_solr.sh
```

## Example use

- input: `articles.txt` with PMCIDs (one per line) and a config file
- output: `qtl.csv` and `qtl.db` (see the database model or Entity-Relationship diagram [here](doc/ER_diagram.png))

`./QTM articles.txt`

`./QTM -h`

```
usage: QTM [-h] [-v] [-o OUTPUT] [-c CONFIG] FILE

Software to extract QTL data from full-text articles.

positional arguments:
  FILE                   input list of articles (PMCIDs)

named arguments:
  -h, --help             show this help message and exit
  -v, --version          show version and exit
  -o OUTPUT, --output OUTPUT
                         filename prefix  for  output  in  SQLite  and  CSV  formats {.db,.csv}
                         (default: qtl)
  -c CONFIG, --config CONFIG
                         config file (default: config.properties)

```
