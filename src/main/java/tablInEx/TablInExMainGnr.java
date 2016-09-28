/**
 *
 * @author gurnoor
 */

package tablInEx;

import fileWriter.ExcelFileWriter;

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
import readers.PmcMetaReader;
import readers.PmcXmlReader;
import readers.Reader;
import stats.Statistics;
import utils.Utilities;

public class TablInExMainGnr {

	public static boolean learnheaders = false;
	public static boolean doXMLInput = false;

	public static HashMap<String, Integer> headermap = new HashMap<String, Integer>();
	public static HashMap<String, Integer> stubmap = new HashMap<String, Integer>();
	public static LinkedList<String> PMCBMI = new LinkedList<String>();
	public static LinkedList<stats.TableStats> TStats = new LinkedList<stats.TableStats>();

	public static void main(String[] args) throws IOException {

		String[] pmcIds = args;

		// pmcIds = new String[]{"PMC4540768"};

		System.out.println("=============================================");
		System.out.println("Extracting Tables from scientific literature in xml format");
		System.out.println("=============================================");
		System.out.println(
				"____________________________________________________________________________________________________________________________");

		// Excel File writer for viewing table properties.
		ExcelFileWriter.TraitTablesFirstLine("TraitTables.csv");
		ExcelFileWriter.PropertiesTableFirstLine("TableProperties.csv");

		// reading xml files with pmc ids **CHECK**
		File[] XMLfiles = new File[pmcIds.length];

		for (int i = 0; i < pmcIds.length; i++) {
			XMLfiles[i] = PmcMetaReader.PmcDowloadXml(pmcIds[i]);
			Article a = new Article("");
			PmcMetaReader P = new PmcMetaReader();

			P.init(XMLfiles[i].getPath());
			a = P.Read();

			a.setNumQTLtables();
			
			//System.out.println(a.getPlain_text() + "\n\n" + "NUMBER OF QTL TABLESSS:" + a.getNumQTLtables());
		}

		ExcelFileWriter.LastLine();

	}

}
