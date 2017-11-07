# QTL TableMiner++

[![Build Status](https://travis-ci.org/candYgene/QTM.svg?branch=master)](https://travis-ci.org/candYgene/QTM)

A significant amount of experimental information about [_Quantitative Trait Locus_](https://en.wikipedia.org/wiki/Quantitative_trait_locus) (QTL) studies are described in (heterogenous) tables of scientific articles. Briefly, a QTL is a genomic region that correlates with a trait of interest (phenotype). _QTLTableMiner++_ (QTM) is a command-line tool to retrieve and semantically annotate results obtained from QTL mapping experiments. QTM takes full-text articles from the [Europe PMC](https://europepmc.org/) repository as input and writes the extracted QTLs onto CSV or SQLite database file.

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
* full-text articles (XML) or PMCIDs from [Europe PMC](https://europepmc.org/)

## Install

```
git clone https://github.com/PBR/QTM.git
cd QTM
mvn install
bash installSolr/installSolr_linux.sh
```

## Usage

`./QTM -pmc PMC4266912 -o QTL_PMC4266912.db`

**Command-line arguments**

[TODO]
