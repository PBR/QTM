/**
 *
 * @author gurnoor
 */

package qtm;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import qtldb.qtlDB;
import readers.PmcMetaReader;

public class qtmMain {

	public static boolean doXMLInput = false;

//	public static HashMap<String, Integer> headermap = new HashMap<String, Integer>();
//	public static HashMap<String, Integer> stubmap = new HashMap<String, Integer>();
//	public static LinkedList<stats.TableStats> TStats = new LinkedList<stats.TableStats>();

	public static void main(String[] args) throws IOException {

		//intialisation
		qtlDB.createTables();
		
		
		String[] pmcIds = args;// pmcIds = new String[]{"PMC4540768"};

		
		
		System.out.println("=============================================");
		System.out.println("QTLTableMiner++ semantic mininig of QTL Tables from scientific articles");
		System.out.println("=============================================");
		System.out.println(
				"____________________________________________________________________________________________________________________________");

		//Step1:  reading xml files with pmc ids 		
		File[] xmlFiles = new File[pmcIds.length];
		Article[] articles=new Article[pmcIds.length]; 
		
		for (int i = 0; i < pmcIds.length; i++) {
                    xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds[i]);
                    articles[i] = new Article("");
                    PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i].getPath());
                    
		        
			//Parsing meta-data, cell entries and finding the abbreviations  
			articles[i] = pmcMetaReader.read();
		}
		
		
		//STEP2 Add abbreviations to Solr synonyms files in all 4 cores and restart 
			solrTagger.AbbrevAnnotaions.abbrevToSolrSynonyms(articles);
			try{
			System.out.println("Restarting Solr");
			Process p=Runtime.getRuntime().exec(new String[] {"bash","-c","/opt/solr/bin/plants restart"});
			p.waitFor();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			//STEP3 Inserting enteries in the data base
			System.out.println("\n\nInsert entry to the TixDB \n\n ");
   			qtlDB.insertArticleEntry(articles);
   			
   			
   			
   			//STEP4 Insert in Trait Table
   			System.out.println("Finding traits nows");
   			qtlDB.InsertTraitEntry(articles);
   			
   			//STEP5 Insert in Trait Values and Trait Properties
   			//qtlDB.insertTraitValuesandTraitProperties(articles);
   			
   			
   			//step6 Mine Trait-Gene / Trait-Marker relationships from Trait Properties
   			//qtlDB.insertQTLTable();
   			
   			
   			//Step7 I am here
   			qtlDB.insertQtlTable();
   			
   			try{
   	   			qtlDB.c.close();
   	   			}catch(SQLException e){
   	   				System.out.println("SQL Exception is clossing the conection");
   	   				e.printStackTrace();
   	   			}
   			
	}

}