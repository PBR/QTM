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

        if (Arrays.asList(args).contains("-h")) {
            printHelp();
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
        Article[] articles = new Article[pmcIds.length];

        for (int i = 0; i < pmcIds.length; i++) {
            xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds[i]);
            articles[i] = new Article("");
            PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i]);

            //Parsing meta-data, cell entries and finding the abbreviations 
            System.out.println("Processing Article: \t" + pmcIds[i]);
            System.out.println("-----------------------------------------");
            articles[i] = pmcMetaReader.read();

        }
        System.out.println("\n");

        //STEP2 Add abbreviations to Solr synonyms files in all 4 cores and restart 
        solrAnnotator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
        try {
            System.out.println("Restarting Solr");

            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", solrProgram + " restart" });
            p.waitFor();
        } catch (Exception e) {
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

        try {
            System.out.println("\nSolr stoped");
            Process p = Runtime.getRuntime().exec(new String[] { "bash", "-c", solrProgram + " stop" });
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n");

        if (Arrays.asList(args).contains("-csv")) {
            try {
                String csvfile = args[Arrays.asList(args).indexOf("-csv") + 1];
                System.out.println("\nExporting results to csv");
                Process p = Runtime.getRuntime()
                        .exec(new String[] { "bash", "-c", "sqlite3 -header -csv " + QtlDb.dbName + " \"Select * from QTL;\" >" + csvfile});
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("\n");
        }

        System.out.println("************************************************************************* \n \n\n");

        System.out.println("=========================================================================");
        System.out.println("RESULTS are available in the following files");
        System.out.println("=========================================================================");
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
        System.out.println("HELP pages for QTL Table Miner ++\r\n");

        System.out.println("DESCRIPTION");
        System.out.println(
                "QTL TableMiner++ is a command-line tool that can retrieve and semantically annotate results of QTL mapping experiments commonly buried in (heterogenous) tables.");

        System.out.println("java -jar QTM.jar -pmc PMC4266912");

        System.out.println("ARGUMENTS");
        System.out.println(
                "    -pmc\t A list of all pmcids required to be processed. Use comma(,) as a seperator between to ids. For example PMC4266912, PMC2267253");
        System.out.println("    -o\t Filename of the output database. This database is in sqlite format.");

        System.out.println("    -h\t HELP pages for QTL Table Miner ++");

    }

}
