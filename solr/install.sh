#!/bin/bash -e

# Install Solr
VERSION=6.6.6
URL=http://archive.apache.org/dist/lucene/solr/$VERSION/solr-$VERSION.tgz
wget -qO- $URL | tar xvz --strip=1 -C solr
