/**
 *
 * @author gurnoor
 */

package qtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.base.Sys;
import org.apache.zookeeper.Shell.ExitCodeException;

import com.github.andrewoma.dexx.collection.List;

import readers.PmcMetaReader;
import resultDb.QtlDb;
import utils.Configs;

public class QtmMain {

    public static boolean doXMLInput = false;
    public static Configs confi = new Configs();
    private static final long megabyte = 1024L * 1024L;

    //	public static HashMmyp8ap<String, Integer> headermap = new HashMap<String, Integer>();
    //	public static HashMap<String, Integer> stubmap = new HashMap<String, Integer>();
    //	public static LinkedList<stats.TableStats> TStats = new LinkedList<stats.TableStats>();

    public static long bytesToMegabytes(long bytes) {
        return bytes / megabyte;
    }

    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();//to calculate run-time

        if (args.length == 0 | Arrays.asList(args).contains("-h") | Arrays.asList(args).contains("--help")) {
            printHelp();
            return;
        }

        if (Arrays.asList(args).contains("-v") | Arrays.asList(args).contains("--version")) {
            System.out.println("1.0 ");
            return;
        }

        if (Arrays.asList(args).contains("-o")) {
           String outFile = args[Arrays.asList(args).indexOf("-o") + 1];
           if (outFile.endsWith(".db")) {
               QtlDb.dbFile=args[Arrays.asList(args).indexOf("-o") + 1];
           } else {
               QtlDb.dbFile=args[Arrays.asList(args).indexOf("-o") + 1] + ".db";
           }
        }

        String inputFile = args[0];
        ArrayList<String> pmcIds = new ArrayList<String>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(inputFile));
            String pmcId = null;

            while ((pmcId = reader.readLine()) != null) {
                pmcIds.add(pmcId);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file '" + inputFile + "' not found.");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader.close();

        // String solrProgram = Configs.getPropertyQTM("solrProgram");
        System.out.println("===============");
        System.out.println("QTLTableMiner++");
        System.out.println("===============\n");

        
        //intialisation
        QtlDb.createTables();

       //Step1:  reading xml files with pmc ids
        File[] xmlFiles = new File[pmcIds.size()];
        Article[] articles = new Article[pmcIds.size()];

        for (int i = 0; i < pmcIds.size(); i++) {
            if(QtlDb.isPmcIdAlredyInDb(pmcIds.get(i))== false)
            {
            
            xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds.get(i));
            articles[i] = new Article("");
            PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i]);

            //Parsing meta-data, cell entries and finding the abbreviations
            System.out.println("Processing article:\n");
            System.out.println("\t" + pmcIds.get(i));
            System.out.println("---------------------------------------------");
            articles[i] = pmcMetaReader.read();
            }
            else
            {
                System.out.println("EuroPMC article arlready exits"+pmcIds.get(i));
                if (pmcIds.size()==i+1)
                    return;
                else
                    continue;
            }
        }
        System.out.println("\n");
        
        //STEP2 Add abbreviations to Solr synonyms files in all 4 cores and restart
        
        solrAnnotator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
        
        
        try {
             System.out.println("Restarting Solr.");
             System.out.println("---------------------------------------------");
        
             Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", Configs.getPropertyQTM("solrRun") + " restart" });
            
             p.waitFor();
             
         } catch (Exception e) {
             e.printStackTrace();
         }
         System.out.println("\n");

        //STEP3 Inserting enteries into the database
        System.out.println("Insert entry to the database.");
        System.out.println("-------------------------------------------------");

        for(int i=0; i<articles.length;i++){
        try{
            if(articles[i] != null)
                QtlDb.insertArticleEntry(articles[i]);
            else
                continue;
        }catch(Exception e){
            System.exit(1);
            
        }
        }
        
//        System.out.println("Searching QTL in tables");
//        System.out.println("-------------------------------------------------");
//        //      STEP4 Insert in QTL Table
//        QtlDb.insertQTLEntry();
        
        

        String csvFile = "";
        try {
            csvFile = FilenameUtils.getBaseName(QtlDb.dbFile) + ".csv";
            System.out.println("Writing results into '" + csvFile + "'");
            System.out.println("-----------------------------------------");
            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c",
                    "sqlite3 -header -csv " + QtlDb.dbFile + " \"SELECT * FROM QTL;\" >" + csvFile });
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n");
        System.out.println("=================================================");
        System.out.println("RESULTS are available in the following files:");
        System.out.println("=================================================");
        System.out.println("Number of processed articles:\t" + articles.length);
        System.out.println("Number of Trait tables founds:\t" + QtlDb.numberofTraitTable());
        System.out.println("Number of QTL founds:\t" + QtlDb.numberofQTL());
        System.out.println("SQLite file: \t" + QtlDb.dbFile);
        System.out.println("CSV file: \t" + csvFile);
        
        try {
            QtlDb.conn.close();
        } catch (SQLException e) {
            System.out.println("SQL Exception is clossing the conection");
            e.printStackTrace();
        }

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        System.out.println("Memory used (bytes): \t" + memory);
        System.out.println("Total runtime (s): \t" + elapsedTime);
    }

    public static void printHelp() {
        System.out.println("\nDESCRIPTION");
        System.out.println("===========");
        System.out.println("QTL TableMiner++ is a command-line tool to retrieve"
                + " and semantically annotate\nresults of QTL mapping studies"
                + " described in tables of scientific articles.\n");
        System.out.println("USAGE");
        System.out.println("=====");
        System.out.println("  QTM [-v|-h]");
        System.out.println("  QTM [-o FILE_PREFIX] FILE\n");
        System.out.println("ARGUMENTS");
        System.out.println("=========");
        System.out.println("  FILE\t\t\t\tList of full-text articles from Europe PMC.\n"
          + "\t\t\t\tEnter one PMCID per line.\n");
        System.out.println("OPTIONS");
        System.out.println("=======");
        System.out.println(
                "  -o|--output FILE_PREFIX\tOutput files in SQLite/" + "CSV formats.\n\t\t\t\t(default: qtl.{db,csv})");
        System.out.println("  -v|--version\t\t\tPrint software version.");
        System.out.println("  -h|--help\t\t\tPrint this help message.");
        System.out.println("\n");
    }
}
