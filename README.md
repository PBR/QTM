# QTL TableMiner++ (QTM)

[![Build Status](https://travis-ci.org/candYgene/QTM.svg?branch=dev)](https://travis-ci.org/candYgene/QTM)
 [![DOI](https://zenodo.org/badge/85691450.svg)](https://doi.org/10.5281/zenodo.1193639)
[![Published in BMC Bioinformatics](https://img.shields.io/badge/published%20in-BMC%20Bioinformatics-blue.svg)](https://doi.org/10.1186/s12859-018-2165-7)

## Description
A significant amount of experimental information about [_Quantitative Trait Locus_](https://en.wikipedia.org/wiki/Quantitative_trait_locus) (QTL) studies are described in (heterogenous) tables of scientific articles. Briefly, a QTL is a genomic region that correlates with a trait of interest (phenotype). _QTM_ is a command-line tool to retrieve and semantically annotate results obtained from QTL mapping experiments. It takes full-text articles from the [Europe PMC](https://europepmc.org/) repository as input and outputs QTLs in a relational database (SQLite, see the [ER diagram](doc/ER_diagram.png)) and a text file (CSV).

## Requirements

* Oracle/OpenJDK8
* [Apache Maven](https://maven.apache.org/) 3.x
* [SQLite](https://sqlite.org/) 3.x
* [Apache Solr](https://lucene.apache.org/solr/) 6.x with cores based on domain-specific vocabularies and ontologies (_Solr cores_):
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

## Usage

```
./QTM -h
usage: QTM [-h] [-v] [-o OUTPUT] [-c CONFIG] [-V VERBOSE] FILE

Software to extract QTL data from full-text articles.

positional arguments:
  FILE                   input list of articles (PMCIDs, one per line)

named arguments:
  -h, --help             show this help message and exit
  -v, --version          show version and exit
  -o OUTPUT, --output OUTPUT
                         filename prefix for output in SQLite (.db) and text (.csv) files (default: qtl)
  -c CONFIG, --config CONFIG
                         config file (default: config.properties)
  -V VERBOSE, --verbose VERBOSE
                         verbosity console output: 0-7 for OFF, FATAL,  ERROR,  WARN,  INFO,  DEBUG, TRACE or ALL (default: 4 [INFO])
```

## Example data

- **input**: `articles.txt` and `config.properties` files
- **output**: `qtl.csv` and `qtl.db` files

```
./QTM articles.txt
```
