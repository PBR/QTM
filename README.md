# QTL TableMiner++ (QTM)

[![Build Status](https://travis-ci.org/candYgene/QTM.svg?branch=master)](https://travis-ci.org/candYgene/QTM)

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

- input: `articles.txt` with PMCIDs (one per line)
- output: `qtl.csv` and `qtl.db` (see the database model or Entity-Relationship diagram [here](doc/ER_diagram.png))

`./QTM articles.txt`

`./QTM -h`

```
...
USAGE
=====
  QTM [-v|-h]
  QTM [-o FILE_PREFIX] FILE

ARGUMENTS
=========
  FILE				List of full-text articles from Europe PMC.
				Enter one PMCID per line.

OPTIONS
=======
  -o, --output FILE_PREFIX	Output files in SQLite/CSV formats.
				(default: qtl.{db,csv})
  -v, --version			Print software version.
  -h, --help			Print this help message.
```
