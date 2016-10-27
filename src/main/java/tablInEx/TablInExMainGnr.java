/**
 *
 * @author gurnoor
 */

package tablInEx;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import qtlTMdb.qtlDB;
import readers.PmcMetaReader;

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
		// ExcelFileWriter.TraitTablesFirstLine("TraitTables.csv");
		// ExcelFileWriter.PropertiesTableFirstLine("TableProperties.csv");

		// reading xml files with pmc ids **CHECK**

		qtlDB.createTables();

		File[] XMLfiles = new File[pmcIds.length];

		for (int i = 0; i < pmcIds.length; i++) {
			XMLfiles[i] = PmcMetaReader.PmcDowloadXml(pmcIds[i]);
			Article a = new Article("");
			PmcMetaReader P = new PmcMetaReader();

			P.init(XMLfiles[i].getPath());

			a = P.Read();

			System.out.println("\n\nInsert entry to the TisDB \n\n ");
   			qtlDB.insertArticleEntry(a);
   			
   			System.out.println("Finding traits nows");
   			qtlDB.InsertTraitEntry(a);
   			
   			System.out.println("***************");
   			System.out.println("Traits founds in Article"+a.getPmc());
   			
//   			for (Trait t: a.getTraits()){
//   				System.out.println("Trait is "+t.getTraitName() );
//   				
//   				System.out.println("Trait Properties");
//   				System.out.println(t.getTraitProperties());
//   				System.out.println("Trait Values");
//   				System.out.println(t.getTraitValues());
//   				System.out.println("Other Trait properties");
//   				System.out.println(t.getOtherProperties());
//   				
//   			}

//			System.out.println("\n\n$$$Abbreviation annotation$$$");
//			a = Annotator.AbbrevAnnotator.AbbreviationAnnotator(a);

//			System.out.println("\n\n$$$Ontology based annotation$$$");
//			a = Annotator.OntologybasedAnnotator.OA(a);

//			System.out.println("\n\n\nTable Headers");			
//			for (Table t : a.getTables()) {
//				for (String col : t.getTableHeadersColumns()) {
//					System.out.println(col);
//				}
//			}
			
//			for(Table t : a.getTables()){
//				for (Columns c: t.getTableCol()){
//					System.out.println(c.getHeader());
//					for(String s: c.getRowEntries()){
//						System.out.println(s);
//					}
//				}
//			}
			
		
		}

		// ExcelFileWriter.LastLine();

	}

}