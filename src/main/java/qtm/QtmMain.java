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
        
        if (args.length == 0 | Arrays.asList(args).contains("-h") | Arrays.asList(args).contains("--help") ) {
            printHelp();
            return;
        }
        
        if (Arrays.asList(args).contains("-v") | Arrays.asList(args).contains("--version") ) {
            System.out.println("1.0 ");
            return;
        }
        
        if (Arrays.asList(args).contains("-o")) {
            String dbName2 = args[Arrays.asList(args).indexOf("-o") + 1];
            QtlDb.dbName = dbName2;

        }

        String pmcs = args[Arrays.asList(args).indexOf("-pmc") + 1];
        String[] pmcIds = pmcs.split(",");

        //String[] pmcIds = args;// pmcIds = new String[]{"PMC4540768"};
        System.out.println(Configs.getPropertyQTM("solrProgram"));
        String solrProgram = Configs.getPropertyQTM("solrProgram");

        System.out.println("===============");
        System.out.println("QTLTableMiner++");
        System.out.println("===============\n\n");
        
        //intialisation
        QtlDb.createTables();

        System.out.println("\n");

        //Step1:  reading xml files with pmc ids 		
        File[] xmlFiles = new File[pmcIds.length];
        Article[] articles = new Article[pmcIds.length];

        for (int i = 0; i < pmcIds.length; i++) {
            xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds[i]);
            articles[i] = new Article("");
            PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i]);

            //Parsing meta-data, cell entries and finding the abbreviations 
            System.out.println("Processing article:\n");
            System.out.println("\t" + pmcIds[i]);
            System.out.println("---------------------------------------------");
            articles[i] = pmcMetaReader.read();

        }
        System.out.println("\n");

        //STEP2 Add abbreviations to Solr synonyms files in all 4 cores and restart 
        solrAnnotator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
        try {
            System.out.println("Restarting Solr.");
            System.out.println("---------------------------------------------");

            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", solrProgram + " restart" });
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n");

        //STEP3 Inserting enteries into the database
        System.out.println("Insert entry to the database.");
        System.out.println("-------------------------------------------------");
        
        QtlDb.insertArticleEntry(articles);

        //STEP4 Insert in Trait Table
        QtlDb.insertTraitEntry(articles);

        //STEP5 Insert in Trait Values and Trait Properties
        //qtlDB.insertTraitValuesandTraitProperties(articles);

        //step6 Mine Trait-Gene / Trait-Marker relationships from Trait Properties
        //qtlDB.insertQTLTable();

        //Step7 I am here
        System.out.println("Finding QTL statements.");
        System.out.println("-------------------------------------------------");

        QtlDb.insertQtlTable();

        try {
            System.out.println("\nSolr stoped!");
            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", solrProgram + " stop" });
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n");

        if (Arrays.asList(args).contains("-csv")) {
            try {
                String csvfile = args[Arrays.asList(args).indexOf("-csv") + 1];
                System.out.println("Writing results to csv file.");
                System.out.println("-----------------------------------------");
                Process p = Runtime.getRuntime()
                        .exec(new String[] { "bash", "-c", "sqlite3 -header -csv " + QtlDb.dbName + " \"Select * from QTL;\" >" + csvfile});
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("\n");
        }

        System.out.println("\n\n");
        System.out.println("=================================================");
        System.out.println("RESULTS are available in the following files:");
        System.out.println("=================================================");
        System.out.println("SQLite file: \t" + QtlDb.dbName);
        if (Arrays.asList(args).contains("-csv"))
            System.out.println("CSV file: \t" + args[Arrays.asList(args).indexOf("-csv") + 1]);

        try {
            QtlDb.c.close();
        } catch (SQLException e) {
            System.out.println("SQL Exception is clossing the conection");
            e.printStackTrace();
        }

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: \t" + memory);
        System.out.println("Used memory is megabytes: \t" + bytesToMegabytes(memory));
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Total run time: \t" + elapsedTime);

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
        System.out.println("  FILE\t\tList of full-text articles from Europe PMC"
          + " (one PMCID per line).\n");
        System.out.println("OPTIONS");
        System.out.println("=======");
        System.out.println("  -o|--output FILE_PREFIX\tOutput files in SQLite/"
          + "CSV formats.\n\t\t\t\t(default: PMCID.{db,csv})");        
        System.out.println("  -v|--version\t\t\tPrint software version.");        
        System.out.println("  -h|--help\t\t\tPrint this help message.");
        System.out.println("\n");
    }
}
