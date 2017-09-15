/**
 *
 * @author gurnoor
 */

package qtm;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import readers.PmcMetaReader;
import resultDb.QtlDb;
import utils.Configs;


public class QtmMain {

	public static boolean doXMLInput = false;
	public static Configs confi=new Configs();
        
		
//	public static HashMmyp8ap<String, Integer> headermap = new HashMap<String, Integer>();
//	public static HashMap<String, Integer> stubmap = new HashMap<String, Integer>();
//	public static LinkedList<stats.TableStats> TStats = new LinkedList<stats.TableStats>();

	public static void main(String[] args) throws IOException {
	    
	        if(Arrays.asList(args).contains("-help")){
	            printHelp();
	            return;
	        }
	        
	        if(Arrays.asList(args).contains("-o")){
	            String dbName2 = args[Arrays.asList(args).indexOf("-o")+1];
	            QtlDb.dbName="data/"+dbName2;
                    
                }

	        if(Arrays.asList(args).contains("-csv")){
                    String csvFile = "data/"+args[Arrays.asList(args).indexOf("-csv")+1];
                    Configs.setPropertyQTM("csvFile", csvFile);
                    
                }
	        
	       String pmcs = args[Arrays.asList(args).indexOf("-pmc")+1];
	       String[] pmcIds=pmcs.split(",");
	        
                
	        
	        
		//String[] pmcIds = args;// pmcIds = new String[]{"PMC4540768"};

		String solrProgram=Configs.getPropertySolr("solrProgram");
		
		
		System.out.println("========================================================================");
		System.out.println("QTLTableMiner++ semantic mininig of QTL Tables from scientific articles");
		System.out.println("========================================================================");
		System.out.println(
				"____________________________________________________________________________________________________________________________\n");

		
		//intialisation
		QtlDb.createTables();
                
		System.out.println("\n");
                
		
		//Step1:  reading xml files with pmc ids 		
		File[] xmlFiles = new File[pmcIds.length];
		Article[] articles=new Article[pmcIds.length]; 
		
		for (int i = 0; i < pmcIds.length; i++) {
                    xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds[i]);
                    articles[i] = new Article("");
                    PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i]);
                    
		        
			//Parsing meta-data, cell entries and finding the abbreviations 
                        System.out.println("Processing Article: \t" +   pmcIds[i]);
                        System.out.println("-----------------------------------------");
			articles[i] = pmcMetaReader.read();
			
			
		}
		System.out.println("\n");

		
		//STEP2 Add abbreviations to Solr synonyms files in all 4 cores and restart 
			solrAnnotator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
			try{
			System.out.println("\n Restarting Solr");
			
			Process p=Runtime.getRuntime().exec(new String[] {"bash","-c",solrProgram+" restart"});
			p.waitFor();
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("\n");

			
			//STEP3 Inserting enteries in the data base
			System.out.println("\n\nInsert entry to the results Database \n\n ");
   			QtlDb.insertArticleEntry(articles);
   			
   			
   			
   			//STEP4 Insert in Trait Table
   			QtlDb.insertTraitEntry(articles);
   			
   			//STEP5 Insert in Trait Values and Trait Properties
   			//qtlDB.insertTraitValuesandTraitProperties(articles);
   			
   			
   			//step6 Mine Trait-Gene / Trait-Marker relationships from Trait Properties
   			//qtlDB.insertQTLTable();
   			
   			
   			//Step7 I am here
   			System.out.println("-----------------------------------------");
                        System.out.println("Finding QTL statements");
                        System.out.println("-----------------------------------------");
                        
                        QtlDb.insertQtlTable();
                        
                        System.out.println("************************************************************************* \n \n\n");
                        
                        System.out.println("=========================================================================");
                        System.out.println("RESULTS are available in the following files");
                        System.out.println("=========================================================================");
                        System.out.println("SQLite file: \t"+QtlDb.dbName);
                        System.out.println("CSV file: \t"+Configs.getPropertyQTM("csvFile"));
                        
                        
   			try{
   	   			QtlDb.c.close();
   	   			}catch(SQLException e){
   	   				System.out.println("SQL Exception is clossing the conection");
   	   				e.printStackTrace();
   	   			}
   			
	}
	
	
	public static void printHelp() {
            System.out.println("HELP pages for QTL Table Miner ++\r\n");
            
            System.out.println("DESCRIPTION");
            System.out
                            .println("QTL TableMiner++ is a command-line tool that can retrieve and semantically annotate results of QTL mapping experiments commonly buried in (heterogenous) tables.");
            
            System.out.println("java -jar QTM.jar -pmc PMC4266912");
            
            System.out.println("ARGUMENTS");
            System.out
                            .println("    -pmc\t A list of all pmcids required to be processed. Use comma(,) as a seperator between to ids. For example PMC4266912, PMC2267253");
            System.out
            .println("    -o\t Filename of the output database. This database is in sqlite format.");

            System.out
            .println("    -help\t HELP pages for QTL Table Miner ++");

	}
	
}