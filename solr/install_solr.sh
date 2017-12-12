#!/bin/bash -e

# 1. Install Solr version (6.2.1)
URL=http://archive.apache.org/dist/lucene/solr/6.2.1/solr-6.2.1.tgz
wget -qO- $URL | tar xvz --strip=1 -C solr

# 2. Start Solr server incl. cores
cd $(dirname $0) && bin/solr start -p 8983 -s core/data

