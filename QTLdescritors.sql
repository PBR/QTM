Select cellValue from cellEntries where colId in (select colId from columnEntries where colType = 'QTL descriptor' );
#Select cellValue from cellEntries where colId in (select colId from columnEntries where colType = 'QTL property' )  ;
Select cellValue from cellEntries where colId in (select colId from columnEntries where colType = 'QTL value' )  ;
