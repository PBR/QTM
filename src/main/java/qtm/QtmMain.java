/**
 * @author gurnoor
 * The main file of the QTLTableminer++
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
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import readers.PmcMetaReader;
import resultDb.QtlDb;
import utils.Configs;


public class QtmMain {

	public static boolean doXMLInput = false;

	public static void main(String[] args) throws IOException {

	long startTime = System.currentTimeMillis();

	if (args.length == 0 || Arrays.asList(args).contains("-h") || Arrays.asList(args).contains("--help")) {
		printHelp();
		return;
	}

	if (Arrays.asList(args).contains("-v") || Arrays.asList(args).contains("--version")) {
		System.out.println("1.0");
		return;
	}

	if (Arrays.asList(args).contains("-o")) {
		String outFile = args[Arrays.asList(args).indexOf("-o") + 1];
		if (outFile.endsWith(".db")) {
			QtlDb.dbFile = args[Arrays.asList(args).indexOf("-o") + 1];
		} else {
			QtlDb.dbFile = args[Arrays.asList(args).indexOf("-o") + 1] + ".db";
		}
	}

	// read input list of PMCIDs
	String inputFile = args[0];
	ArrayList<Article> articles = new ArrayList<Article>();
	BufferedReader reader = null;
	try {
		reader = new BufferedReader(new FileReader(inputFile));
		String line = null;

		while ((line = reader.readLine()) != null) {
			line = line.trim().toUpperCase();
			if (line.matches("^PMC\\d+$")) {
				Article a = new Article(line);
				articles.add(a);
			}
		}
		reader.close();
	} catch (FileNotFoundException e) {
		System.err.println(e.getMessage());
		System.exit(1);
	}
	System.out.println("===============");
	System.out.println("QTLTableMiner++");
	System.out.println("===============\n");

	// populate empty database
	QtlDb.populate();

	// (re)start Solr server
	//solrCmd("restart");

	// process articles in XML
	for (Article a : articles) {
		a.download();
	}
}

	/*
	 * for (int i = 0; i < articles.size(); i++) { /*xmlFiles[i] =
	 * PmcMetaReader.pmcDowloadXml(pmcIds.get(i)); PmcMetaReader pmcMetaReader = new
	 * PmcMetaReader(xmlFiles[i]); System.out.println("Processing article " +
	 * pmcIds.get(i) + "."); articles[i] = pmcMetaReader.read(); } System.exit(0);
	 * 
	 * // STEP2 Add abbreviations to Solr synonyms files in all 4 cores and //
	 * restart solrAnnotator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
	 * solrCmd("restart");
	 * 
	 * // Insert entries into db for (int i = 0; i < articles.length; i++) { try {
	 * if (articles[i] != null) QtlDb.insertArticle(articles[i]); else continue; }
	 * catch (Exception e) { System.exit(1); } }
	 * 
	 * System.out.println("Searching QTL in tables");
	 * System.out.println("-------------------------------------------------");
	 * QtlDb.insertQTLEntry();
	 * 
	 * String csvFile = ""; try { csvFile = FilenameUtils.getBaseName(QtlDb.dbFile)
	 * + ".csv"; System.out.println("Writing results into '" + csvFile + "'");
	 * System.out.println("-----------------------------------------"); String[]
	 * cmdline = { "bash", "-c", "sqlite3 -header -csv " + QtlDb.dbFile +
	 * " \"SELECT * FROM V_QTL\">" + csvFile }; System.out.println(String.join(" ",
	 * cmdline)); Process p = Runtime.getRuntime().exec(cmdline); p.waitFor(); }
	 * catch (Exception e) { e.printStackTrace(); } System.out.println("\n");
	 *
	 * System.out.println("==================");
	 * System.out.println("Processing summary");
	 * System.out.println("==================");
	 * System.out.println("Number of articles:\t" + n_articles);
	 * System.out.println("Number of trait tables:\t" + QtlDb.numberofTraitTable());
	 * System.out.println("Number of QTL statements:\t" + QtlDb.numberofQTL());
	 * System.out.println("Output SQLite file:\t" + QtlDb.dbFile);
	 * System.out.println("Output CSV file:\t" + csvFile);
	 * 
	 * try { QtlDb.conn.close(); } catch (SQLException e) {
	 * System.out.println("SQL Exception is clossing the conection");
	 * e.printStackTrace(); }
	 * 
	 * long totalMem = Runtime.getRuntime().totalMemory(); long freeMem =
	 * Runtime.getRuntime().freeMemory(); long memUsed = (totalMem - freeMem) / 1024
	 * / 1024; long endTime = System.currentTimeMillis(); long elapsedTime = endTime
	 * - startTime; long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime); long
	 * minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime); long seconds =
	 * TimeUnit.MILLISECONDS.toSeconds(elapsedTime);
	 * 
	 * System.out.println("Memory used (KB):\t" + memUsed);
	 * System.out.println("Runtime (HH:MM:SS):\t" + String.format("%02d:%02d:%02d",
	 * hours, minutes, seconds));
	 * 
	 * // finally stop Solr server solrCmd("stop"); }
	 * 
	 * public static void solrCmd(String cmd) {
	 * System.out.println(StringUtils.capitalize(cmd) + " Solr server."); try {
	 * String[] cmdline = { Configs.getPropertyQTM("solrBinPath"), cmd,
	 * Configs.getPropertyQTM("solrPort"), Configs.getPropertyQTM("solrCorePath") };
	 * Process p = Runtime.getRuntime().exec(cmdline); p.waitFor(); } catch
	 * (Exception e) { e.printStackTrace(); } }
	 */
	public static void printHelp() {
		System.out.println("DESCRIPTION");
		System.out.println("===========");
		System.out.println("QTLTableMiner++ tool extracts results of QTL mapping experiments "
				+ "described in tables of scientific articles.\n");
		System.out.println("USAGE");
		System.out.println("=====");
		System.out.println("  QTM [-v|-h]");
		System.out.println("  QTM [-o FILE_PREFIX] FILE\n");
		System.out.println("ARGUMENTS");
		System.out.println("=========");
		System.out.println("  FILE\t\t\t\tList of articles from Europe PMC (one PMCID per line).\n");
		System.out.println("OPTIONS");
		System.out.println("=======");
		System.out.println("  -o, --output FILE_PREFIX\tOutput files in SQLite/"
				+ "CSV formats.\n\t\t\t\t(default: qtl.{db,csv})");
		System.out.println("  -v, --version\t\t\tPrint software version.");
		System.out.println("  -h, --help\t\t\tPrint this help message.\n");
	}
}
