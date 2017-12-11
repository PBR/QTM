#!/bin/bash -e

# 1. Install Solr version (6.2.1)
wget -nc http://archive.apache.org/dist/lucene/solr/6.2.1/solr-6.2.1.tgz -O solr-6.2.1.tgz
tar -xvf solr-6.2.1.tgz --directory ./
ln -sf solr-6.2.1/ solr

# 2. Start Solr server incl. cores
solr/bin/solr start -p 8983 -s core/data

