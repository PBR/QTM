/**
 * @author gurnoor
 * The main file of the QTLTableminer++
 */

package nl.esciencecenter.qtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.commons.io.FilenameUtils;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.esciencecenter.readers.PmcMetaReader;
import nl.esciencecenter.resultDb.QtlDb;
import nl.esciencecenter.utils.Configs;

public class Main {

	public static boolean doXMLInput = false;
	public static final Logger logger = Logger.getLogger(Main.class.getName());
	private static final long megabyte = 1024L * 1024L;
	
	private static final int VERBOSITY_OFF = 0;
	private static final int VERBOSITY_FATAL = 1;
	private static final int VERBOSITY_ERROR = 2;
	private static final int VERBOSITY_WARN = 3;
	private static final int VERBOSITY_INFO = 4;
	private static final int VERBOSITY_DEBUG = 5;
	private static final int VERBOSITY_TRACE = 6;
	private static final int VERBOSITY_ALL = 7;


	public static void main(String[] args) throws IOException {
		ArgumentParser parser = ArgumentParsers.newFor("QTM").build()
				.defaultHelp(true)
				.description("Software to extract QTL data from full-text articles.")
				.version(Main.class.getPackage().getImplementationVersion());
				
		parser.addArgument("-v", "--version").action(Arguments.version())
				.help("show version and exit");
		parser.addArgument("-o", "--output").setDefault("qtl").help("filename prefix for output in SQLite and CSV formats {.db,.csv}");
		parser.addArgument("FILE").help("input list of articles (PMCIDs)");
		parser.addArgument("-c", "--config").help("config file").setDefault("config.properties");
		parser.addArgument("-V", "--verbose").type(Integer.class).help("verbosity console output: "+
				VERBOSITY_OFF+"-"+VERBOSITY_ALL+" for OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE or ALL");	 	
		
		try {
			Namespace res = parser.parseArgs(args);
			String inputArticles = res.get("FILE");
			String configFile = res.get("config");
			String outputFile = res.get("output");
			if(res.get("verbose")!=null) {
				//get level
			    int verbosityLevel = (Integer) res.get("verbose");			  							
				//try to find console appender in log4j properties
				Logger rootLogger = Logger.getRootLogger();		
				if(rootLogger!=null) {
					@SuppressWarnings("unchecked")
					Enumeration<AppenderSkeleton> loggerAppenders = rootLogger.getAllAppenders();
					while(loggerAppenders.hasMoreElements()) {
						AppenderSkeleton appender = loggerAppenders.nextElement();					
						if(appender.getClass()==org.apache.log4j.ConsoleAppender.class) {
							if(verbosityLevel==VERBOSITY_OFF) {
							    appender.setThreshold(Level.OFF);
							} else if(verbosityLevel==VERBOSITY_FATAL) {
								appender.setThreshold(Level.FATAL);
							} else if(verbosityLevel==VERBOSITY_ERROR) {
								appender.setThreshold(Level.ERROR);
							} else if(verbosityLevel==VERBOSITY_WARN) {
								appender.setThreshold(Level.WARN);
							} else if(verbosityLevel==VERBOSITY_INFO) {
								appender.setThreshold(Level.INFO);
							} else if(verbosityLevel==VERBOSITY_DEBUG) {
								appender.setThreshold(Level.DEBUG);
							} else if(verbosityLevel==VERBOSITY_TRACE) {
								appender.setThreshold(Level.TRACE);
							} else if(verbosityLevel==VERBOSITY_ALL) {
								appender.setThreshold(Level.ALL);
							} else {
								logger.warn("incorrect verbosity level "+verbosityLevel);
							}
						}
					}
				}
			}
			run(inputArticles, configFile, outputFile);
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}
	
	}

	public static void run(String inputArticlesFile, String configFile,
			String outputFile) throws IOException {
		long startTime = System.currentTimeMillis();// to calculate run-time

		Configs.configFileName=configFile;

		if (outputFile.endsWith(".db")) {
			QtlDb.dbFile = outputFile;
		} else {
			QtlDb.dbFile = outputFile + ".db";
		}


	// (re)start Solr server
	controlSolr("restart");

		ArrayList<String> pmcIds = new ArrayList<String>();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(inputArticlesFile));
			String pmcIdline = null;
			while ((pmcIdline = reader.readLine()) != null) {
				pmcIdline = pmcIdline.trim();
				if (!pmcIdline.equals(""))
					pmcIds.add(pmcIdline);
			}
		} catch (FileNotFoundException e) {
			logger.error("Input file '" + inputArticlesFile + "' not found.");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		reader.close();
		
		logger.info("===============");
		logger.info("QTLTableMiner++");
		logger.info("===============");

		logger.info("Input List: " + pmcIds.toString());

		// intialisation
		QtlDb.createTables();

		// Step1: reading xml files with pmc ids
		File[] xmlFiles = new File[pmcIds.size()];
		Article[] articles = new Article[pmcIds.size()];

		for (int i = 0; i < pmcIds.size(); i++) {
			if (QtlDb.isPmcIdAlredyInDb(pmcIds.get(i)) == false) {

				xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds.get(i));

				PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i]);

				// Parsing meta-data, cell entries and finding the abbreviations
				logger.info("- Processing article " + pmcIds.get(i));
				articles[i] = pmcMetaReader.read();
			} else {
				logger.info("- Article "+pmcIds.get(i)+" already exists");
				if (pmcIds.size() == i + 1)
					return;
				else
					continue;
			}
		}		

		// STEP2 Add abbreviations to Solr synonyms files in all 4 cores and
		// restart
		nl.esciencecenter.solr.abbreviator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
		controlSolr("restart");

		// STEP3 Inserting entries into the database
		logger.info("Inserting entries to the database");
		for (int i = 0; i < articles.length; i++) {
			try {
				if (articles[i] != null)
					QtlDb.insertArticleEntry(articles[i]);
				else
					continue;
			} catch (Exception e) {
				System.exit(1);

			}
		}

		logger.info("Searching QTL in tables");
		QtlDb.insertQTLEntry();

		String csvFile = "";
		try {
			csvFile = FilenameUtils.getBaseName(QtlDb.dbFile) + ".csv";
			logger.info("Writing results into " + csvFile + "");
			String[] cmdline = {"bash", "-c", "sqlite3 -header -csv " + QtlDb.dbFile +
                         " \"SELECT * FROM V_QTL\" 	>" + csvFile};
			logger.debug(String.join(" ", cmdline));
			Process p = Runtime.getRuntime().exec(cmdline);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("=======");
		logger.info("RESULTS");
		logger.info("=======");
		logger.info("Total number of processed articles: " + articles.length);
		logger.info("Total number of trait tables: " + QtlDb.numberofTraitTable());
		logger.info("Total number of QTL statements: " + QtlDb.numberofQTL());
		logger.info("SQLite file: " + QtlDb.dbFile);
		logger.info("CSV file: " + csvFile);

		try {
			QtlDb.conn.close();
		} catch (SQLException e) {
			logger.warn("SQL Exception is closing the connection");
			e.printStackTrace();
		}

		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		String eTime = String.format("%02d:%02d:%02d",
				TimeUnit.MILLISECONDS.toHours(elapsedTime),
				TimeUnit.MILLISECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS
						.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime)),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
						- TimeUnit.MINUTES.toSeconds(
								TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));

		logger.info("Memory used (KB): " + memory);
		logger.info("Total runtime (HH:MM:SS): " + eTime);

		// finally stop Solr server
		controlSolr("stop");
	}

	public static long bytesToMegabytes(long bytes) {
		return bytes / megabyte;
	}

	public static void controlSolr(String cmd) {
		logger.info("Solr server has been " + cmd + "ed.");
		try {
			String[] cmdline = {Configs.getPropertyQTM("solrRun"), cmd,
					Configs.getPropertyQTM("solrPort"),
					Configs.getPropertyQTM("solrCorePath")};
			logger.debug(String.join(" ", cmdline));
			Process p = Runtime.getRuntime().exec(cmdline);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}