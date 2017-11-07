#!/bin/bash
# 1. Install Solr version (6.2.1)
sudo wget -nc http://archive.apache.org/dist/lucene/solr/6.2.1/solr-6.2.1.tgz -O /opt/solr-6.2.1.tgz
sudo tar -xvf /opt/solr-6.2.1.tgz --directory /opt/
sudo ln -sf /opt/solr-6.2.1/ /opt/solr
#sudo chmod 777 solr
echo "Check if solr is running"
sudo /opt/solr/bin/solr restart
sudo /opt/solr/bin/solr stop

#2. Copy core files from ../data/solr to /var/solr/data with cpSolrCore
# Make directory /var/solr
sudo mkdir -p /var/solr/data
sudo cp -r installSolr/solrData/solrData/* /var/solr/data/
sudo chmod 777 -R /var/solr/*
# 3. Restart solr
sudo /opt/solr/bin/solr restart -p 8983 -s /var/solr/data
