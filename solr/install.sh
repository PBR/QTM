#!/usr/bin/env bash

set -xe

# Install Solr
VERSION=6.2.1
URL=http://archive.apache.org/dist/lucene/solr/$VERSION/solr-$VERSION.tgz
wget -qO- $URL | tar xvz --strip=1 -C $(dirname $0)
