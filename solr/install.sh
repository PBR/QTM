#!/bin/bash -e

# Install Solr version (6.2.1)
URL=http://archive.apache.org/dist/lucene/solr/6.2.1/solr-6.2.1.tgz
wget -qO- $URL | tar xvz --strip=1 -C solr
