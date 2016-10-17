TablesinXMI - A program to read Tables from scientific articles in xml format. 
===============================================
This program parses Tables from scitific literature and filters out tables containing data related to QTL informtaion.

Requirements
------------
Java 1.7
SQLite
Plant Ontology
Crop Ontology (SP)
Trait Ontology
Stato

Examlple
---------
java -jar TablesinXMI.jar PMC4266912


Arguements
------------

Default:    List of PMC ids

-help  :    for using helps


Output
-------------
Tix.db(SQLiteDB file containing a database of the knowledge mined from the tables.)


