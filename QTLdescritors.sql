Select * from cellEntries where colId in (select colId from columnEntries where colType = 'QTL descriptor' OR colType= 'QTL property' ) AND cellType!= 'Empty'; 
