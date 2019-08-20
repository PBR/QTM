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
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.commons.io.FilenameUtils;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.esciencecenter.readers.PmcMetaReader;
import nl.esciencecenter.resultDb.QtlDb;
import nl.esciencecenter.utils.Configs;

public class Main {

	public static boolean doXMLInput = false;
	public static final Logger logger = Logger.getLogger(Main.class.getName());

	private static final int VERBOSITY_OFF = 0;
	private static final int VERBOSITY_FATAL = 1;
	private static final int VERBOSITY_ERROR = 2;
	private static final int VERBOSITY_WARN = 3;
	private static final int VERBOSITY_INFO = 4;
	private static final int VERBOSITY_DEBUG = 5;
	private static final int VERBOSITY_TRACE = 6;
	private static final int VERBOSITY_ALL = 7;

	public static void main(String[] args) throws IOException {
		ArgumentParser parser = ArgumentParsers.newFor("QTM").build().defaultHelp(true)
				.description("Software to extract QTL data from full-text articles.")
				.version(Main.class.getPackage().getImplementationVersion());

		parser.addArgument("-v", "--version").action(Arguments.version()).help("show version and exit");
		parser.addArgument("-o", "--output").setDefault("qtl")
				.help("filename prefix for output in SQLite and CSV formats {.db,.csv}");
		parser.addArgument("FILE").help("input list of articles (PMCIDs)");
		parser.addArgument("-c", "--config").help("config file").setDefault("config.properties");

		Logger rootLogger = Logger.getRootLogger();
		// check for console handler
		Boolean addVerboseArgument = Boolean.FALSE;
		String verboseDefault = null;
		// only add verbose argument if
		if (rootLogger != null) {
			@SuppressWarnings("unchecked")
			Enumeration<AppenderSkeleton> loggerAppenders = rootLogger.getAllAppenders();
			while (loggerAppenders.hasMoreElements()) {
				AppenderSkeleton appender = loggerAppenders.nextElement();
				if (appender.getClass() == org.apache.log4j.ConsoleAppender.class) {
					addVerboseArgument = Boolean.TRUE;
					ConsoleAppender consoleAppender = (ConsoleAppender) appender;
					Priority priority = consoleAppender.getThreshold();
					switch (priority.toInt()) {
						case Level.OFF_INT :
							verboseDefault = "0 [" + Level.OFF + "]";
							break;
						case Level.FATAL_INT :
							verboseDefault = "1 [" + Level.FATAL + "]";
							break;
						case Level.ERROR_INT :
							verboseDefault = "2 [" + Level.ERROR + "]";
							break;
						case Level.WARN_INT :
							verboseDefault = "3 [" + Level.WARN + "]";
							break;
						case Level.INFO_INT :
							verboseDefault = "4 [" + Level.INFO + "]";
							break;
						case Level.DEBUG_INT :
							verboseDefault = "5 [" + Level.DEBUG + "]";
							break;
						case Level.TRACE_INT :
							verboseDefault = "6 [" + Level.TRACE + "]";
							break;
						case Level.ALL_INT :
							verboseDefault = "7 [" + Level.ALL + "]";
							break;
						default :
							verboseDefault = null;
					}
				}
			}
		}
		// only add verboseArgument if console handler has been found (set in
		// log4j.properties)
		// use default argument derived from this definition
		if (addVerboseArgument) {
			String helpText = "verbosity console output: " + VERBOSITY_OFF + "-" + VERBOSITY_ALL + " for " + Level.OFF
					+ ", " + Level.FATAL + ", " + Level.ERROR + ", " + Level.WARN + ", " + Level.INFO + ", "
					+ Level.DEBUG + ", " + Level.TRACE + " or " + Level.ALL;
			if (verboseDefault != null) {
				helpText += " (default: " + verboseDefault + ")";
			}
			parser.addArgument("-V", "--verbose").type(Integer.class).help(helpText);
		}

		try {
			Namespace res = parser.parseArgs(args);
			String inputArticles = res.get("FILE");
			String configFile = res.get("config");
			String outputFile = res.get("output");

			if (addVerboseArgument && res.get("verbose") != null) {
				// get level
				int verbosityLevel = (Integer) res.get("verbose");
				// try to find console appender in log4j properties
				if (rootLogger != null) {
					@SuppressWarnings("unchecked")
					Enumeration<AppenderSkeleton> loggerAppenders = rootLogger.getAllAppenders();
					while (loggerAppenders.hasMoreElements()) {
						AppenderSkeleton appender = loggerAppenders.nextElement();
						if (appender.getClass() == org.apache.log4j.ConsoleAppender.class) {
							switch (verbosityLevel) {
								case VERBOSITY_OFF :
									appender.setThreshold(Level.OFF);
									break;
								case VERBOSITY_FATAL :
									appender.setThreshold(Level.FATAL);
									break;
								case VERBOSITY_ERROR :
									appender.setThreshold(Level.ERROR);
									break;
								case VERBOSITY_WARN :
									appender.setThreshold(Level.WARN);
									break;
								case VERBOSITY_INFO :
									appender.setThreshold(Level.INFO);
									break;
								case VERBOSITY_DEBUG :
									appender.setThreshold(Level.DEBUG);
									break;
								case VERBOSITY_TRACE :
									appender.setThreshold(Level.TRACE);
									break;
								case VERBOSITY_ALL :
									appender.setThreshold(Level.ALL);
									break;
								default :
									logger.warn("incorrect verbosity level " + verbosityLevel);
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

	public static void run(String inputArticlesFile, String configFile, String outputFile) throws IOException {
		long startTime = System.currentTimeMillis();

		Configs.configFileName = configFile;
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
		logger.info("=== QTLTableMiner++ ====");
		logger.info("Input list of articles:\n\t" + pmcIds.toString());

		// init db
		QtlDb.createTables();

		// download/read articles in XML
		File[] xmlFiles = new File[pmcIds.size()];
		Article[] articles = new Article[pmcIds.size()];
		for (int i = 0; i < pmcIds.size(); i++) {
			if (QtlDb.isPmcIdAlredyInDb(pmcIds.get(i)) == false) {
				xmlFiles[i] = PmcMetaReader.pmcDowloadXml(pmcIds.get(i));
				PmcMetaReader pmcMetaReader = new PmcMetaReader(xmlFiles[i]);
				// parse metadata
				logger.info("Processing " + pmcIds.get(i) + "...");
				articles[i] = pmcMetaReader.read();
			} else {
				logger.info("Article with " + pmcIds.get(i) + " already exists.");
				if (pmcIds.size() == i + 1)
					return;
				else
					continue;
			}
		}

		// add abbreviations to Solr synonyms files
		nl.esciencecenter.solr.abbreviator.AbbrevtoSynonyms.abbrevToSolrSynonyms(articles);
		controlSolr("restart");

		logger.info("Storing article entries in the database.");
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

		logger.info("Storing QTL data.");
		QtlDb.insertQTLEntry();

		String csvFile = "";
		try {
			csvFile = FilenameUtils.getBaseName(QtlDb.dbFile) + ".csv";
			logger.info("Writing results into " + csvFile + "");
			String[] cmdline = {"bash", "-c",
					"sqlite3 -header -csv " + QtlDb.dbFile + " \"SELECT * FROM V_QTL\" 	>" + csvFile};
			logger.debug(String.join(" ", cmdline));
			Process p = Runtime.getRuntime().exec(cmdline);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("=== Summary ===");
		logger.info("Number of processed articles: " + articles.length);
		logger.info("Number of trait tables: " + QtlDb.numberofTraitTable());
		logger.info("Number of QTL statements: " + QtlDb.numberofQTL());
		logger.info("SQLite file: " + QtlDb.dbFile);
		logger.info("CSV file: " + csvFile);

		try {
			QtlDb.conn.close();
		} catch (SQLException e) {
			logger.warn("SQL Exception is closing the connection.");
			e.printStackTrace();
		}

		Runtime runtime = Runtime.getRuntime();
		runtime.gc();
		long memory = (runtime.totalMemory() - runtime.freeMemory()) / 1024;
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		String eTime = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedTime),
				TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime)),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));
		logger.info("Memory used (KB): " + memory);
		logger.info("Total runtime (HH:MM:SS): " + eTime);

		// finally stop Solr server
		controlSolr("stop");
	}

	public static void controlSolr(String cmd) {
		logger.info("Solr server " + cmd + "ed.");
		try {
			String[] cmdline = {Configs.getPropertyQTM("solrRun"), cmd, Configs.getPropertyQTM("solrPort"),
					Configs.getPropertyQTM("solrCorePath")};
			logger.debug(String.join(" ", cmdline));
			Process p = Runtime.getRuntime().exec(cmdline);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
