/**
 *
 * @author gurnoor
 */

package qtm;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import qtlTMdb.qtlDB;
import readers.PmcMetaReader;

public class qtmMain {

	public static boolean learnheaders = false;
	public static boolean doXMLInput = false;

	public static HashMap<String, Integer> headermap = new HashMap<String, Integer>();
	public static HashMap<String, Integer> stubmap = new HashMap<String, Integer>();
	public static LinkedList<String> PMCBMI = new LinkedList<String>();
	public static LinkedList<stats.TableStats> TStats = new LinkedList<stats.TableStats>();

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
			xmlFiles[i] = PmcMetaReader.PmcDowloadXml(pmcIds[i]);
			articles[i] = new Article("");
			PmcMetaReader P = new PmcMetaReader();
			
			P.init(xmlFiles[i].getPath());

			//Parsing meta-data, cell entries and finding the abbreviations  
			articles[i] = P.Read();
		}
		
		
		//STEP2 Add abbreviations to Solr synonyms files in all 4 cores and restart 
			solrTagger.AbbrevAnnotaions.AbbreviationtoSolarSysnonyms(articles);
			try{
			System.out.println("Restarting Solr");
			Process p=Runtime.getRuntime().exec(new String[] {"bash","-c","/opt/solr/bin/plants restart"});
			p.waitFor();
			}catch(Exception e){
				e.printStackTrace();
			}
			
			
			//STEP3
			//inserting enteries in the data base
			System.out.println("\n\nInsert entry to the TixDB \n\n ");
   			qtlDB.insertArticleEntry(articles);
   			
   			
   			
   			//STEP4
   			//insert in Trait Table
   			System.out.println("Finding traits nows");
   			qtlDB.InsertTraitEntry(articles);
   			
   			//STEP5
   			//insert in Trait Values and Trait Properties
   			qtlDB.insertTraitValuesandTraitProperties(articles);

   			
   			

	}

}