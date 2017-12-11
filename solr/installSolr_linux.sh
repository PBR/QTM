#!/bin/bash
# 1. Install Solr version (6.2.1)
wget -nc http://archive.apache.org/dist/lucene/solr/6.2.1/solr-6.2.1.tgz -O solr-6.2.1.tgz
tar -xvf solr-6.2.1.tgz --directory ./
ln -sf solr-6.2.1/ solr
#sudo chmod 777 solr
echo "Check if solr is running"
solr/bin/solr restart
solr/bin/solr stop

# 3. Restart solr
solr/bin/solr restart -p 8983 -s core/data
