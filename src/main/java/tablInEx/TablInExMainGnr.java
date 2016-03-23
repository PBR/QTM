/**
 *
 * @author gurnoor
 */

package tablInEx;

import Utils.Utilities;
import Writer.ExcelFileWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import net.didion.jwnl.JWNL;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import qtlTMdb.qtlDB;
import readers.PmcXmlReader;
import readers.Reader;
import stats.Statistics;




public class TablInExMainGnr {

	public static boolean databaseSave = false;
	public static boolean doStats = false;
	public static boolean TypeClassify = false;
	public static boolean ComplexClassify = false;
	public static boolean learnheaders = false;
	public static boolean doIE = false;
	public static String outputDest = "";
	public static boolean doXMLInput = false;
	public static boolean shouldTag = false;
	public static boolean IEinSQLTial = false;
	public static boolean IEFreqSQLTial = false;
	public static boolean IEFine = false;
	public static boolean Conceptization = false;
	public static boolean ExportLinkedData = false;
	public static String Inpath;
        
	public static HashMap<String, Integer> headermap = new HashMap<String, Integer>();
	public static HashMap<String, Integer> stubmap = new HashMap<String, Integer>();
	public static LinkedList<String> PMCBMI = new LinkedList<String>();
	public static LinkedList<stats.TableStats> TStats = new LinkedList<stats.TableStats>();
       
    public static void main(String[] args) throws IOException{
        
        if (Arrays.asList(args).contains("-help")) {
			printHelp();
			return;
		}
        
        
        //qtlDB.connectionDB();
       // qtlDB.createQTLtableAuto();
        //qtlDB.createQTLtableManual();
        //qtlDB.insertQTLtableManual();
        //List of PMCIDS
        //Example --->//String PMC1="PMC3245175";
	//String PMC="PMC4266912";
        String[] pmcIds = args;
        
        //pmcIds= new String[] {"PMC4301655","PMC4266912", "PMC3852376","PMC4321030","PMC4691107","PMC3970963", "PMC4008630", "PMC4726135", "PMC3464107", "PMC4678209", "PMC2271080",  "PMC2246063", "PMC2652058"};
        //pmcIds=new String[]{"PMC4321030"};
        //Display
        System.out.println("=============================================");
        System.out.println("=============================================");
	System.out.println("Extracting Tables from scientific literature in xml format");
	System.out.println("=============================================");
		
	System.out.println("=============================================");
	System.out.println("____________________________________________________________________________________________________________________________");
		
        //Excel File writer for viewing table properties.
        ExcelFileWriter.TraitTablesFirstLine("TraitTables.csv");
        ExcelFileWriter.PropertiesTableFirstLine("TableProperties.csv");
        
        
        //reading xml files with pmc ids
        File [] XMLfiles=new File[pmcIds.length];
        
        for(int i=0; i<pmcIds.length; i++){
                    XMLfiles[i]=PmcXmlReader.PmcDowloadXml(pmcIds[i]);
                    Article  a = new Article("");
                    PmcXmlReader P=new PmcXmlReader();
                    P.init(XMLfiles[i].getPath());
                    a = P.Read();
                    System.out.println(a.getAbstract());
                    //Write to entries to excel
                    try{
                       ExcelFileWriter.TablePropertiesEntries(a); 
                       ExcelFileWriter.TraitTablesEntries(a);
                    }
                    catch(NullPointerException e){
                        System.out.println(pmcIds[i]+"is giving problem");
                    }
        }
        
        ExcelFileWriter.LastLine();
                
    }      
    
    
    /**
	 * Prints the help.
	 */
    public static void printHelp() {
		System.out.println("HELP pages for XMLTablReader\r\n");
		System.out.println("DESCRIPTION");
		System.out.println("     XMLTable Reader is prime purpose is to extract Tables from articles in xml format. It further, filters QTL/traits from the list of all tables");
		System.out.println("ARGUMENTS");
		System.out.println(" Argument should be the list of PMC ids");
		System.out.println("OUTPUT FILES");
		System.out.println(" Two Excel Files TableProperties.csv and TraitTables.csv is generated in your current directory.");
                
	}
    
}
