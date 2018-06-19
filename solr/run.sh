curl -XPOST \
  'http://localhost:8983/solr/sgn_markers/tag?overlaps=NO_SUB&tagsLimit=5000&fl=*' \
  -H 'Content-Type:text/plain' -d test.txt
