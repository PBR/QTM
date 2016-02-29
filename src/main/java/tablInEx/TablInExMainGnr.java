/**
 *
 * @author gurnoor
 */

package tablInEx;

import Utils.Utilities;
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
import readers.PMCXMLReader;
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
    
    
    public static void TableCsvFile(Article a, FileWriter writer){
	
                for (int s = 0; s < a.getTables().length; s++) {
                    try {
                        writer.append(a.getPmc()+"_"+"Table"+s+';');
                        writer.append(a.getTables()[s].isColSpanning() + ";");
                        writer.append(a.getTables()[s].isRowSpanning()+ ";");
                        writer.append(a.getTables()[s].isHasHeader()+ ";");
                        writer.append("NKnow"+ ";");
                        writer.append(a.getTables()[s].isHasBody()+ ";");
                        writer.append(a.getTables()[s].getTable_caption().replaceAll("\\s","_")+ ";");
                        writer.append(a.getTables()[s].getTable_footer().replaceAll("\\s","_")+ "\n");
                    } catch (IOException ex) {
                        Logger.getLogger(TablInExMainGnr.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                
                //generate whatever data you want
    }
    
    public static File PMCDowloadXML(String PMCID)throws IOException, MalformedURLException {
    
        File xmlfile=new File("/home/gurnoor/NetBeansProjects/XMLTABLE/PMCfiles/"+PMCID+".xml");
    if (!xmlfile.exists()) {
				xmlfile.createNewFile();
			}
             
     String API_PMCXML= "http://www.ebi.ac.uk/europepmc/webservices/rest/"+PMCID+"/fullTextXML";
     URL website = new URL(API_PMCXML);
     ReadableByteChannel rbc = Channels.newChannel(website.openStream());
     FileOutputStream fos = new FileOutputStream(xmlfile);
     fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
     fos.close();
     
     try {
			JWNL.initialize(new FileInputStream(xmlfile));
		} catch (Exception Ex) {
			System.out.println("JWNL.intialize exception");
		}
		
        return xmlfile;
    }     
    

    public static void main(String[] args) throws IOException {
        
                //String PMC1="PMC3245175";
		//String PMC="PMC4266912";
                
                String PMCIDS [];
                PMCIDS= new String[] {"PMC4301655","PMC4266912", "PMC3852376","PMC4321030","PMC4691107","PMC3970963", "PMC4008630", "PMC4726135", "PMC3464107", "PMC4678209", "PMC2271080", "PMC3209458", "PMC2246063", "PMC2652058"};
                //concept = new ConceptizationStats();
		// concept2 = new ConceptizationStats();

                    
                FileWriter writer = new FileWriter("TableProperties.csv");
                writer.append("Table_id"+';');
                writer.append("isColSpan"+';');
                writer.append("isRowSpan"+';');
                writer.append("HasTableHeader"+';');
                writer.append("hasmultipletable"+";");
                writer.append("hastablebody"+";");
                writer.append("TableCaption"+";");
                writer.append("Tablefooter"+"\n");
                
		
                
                
                
                System.out.println("=============================================");
		System.out.println("=============================================");
		System.out.println("____________________________________________________________________________________________________________________________");
		
                File [] XMLPMCS=new File[PMCIDS.length];
                  
                for(int i=0; i<PMCIDS.length; i++){
                   XMLPMCS[i]=PMCDowloadXML(PMCIDS[i]);
                           
                    Article  a = new Article("");
                
                    PMCXMLReader P=new PMCXMLReader();
                    P.init(XMLPMCS[i].getPath());
                    a = P.Read();
                    
                    try{
                
                    TableCsvFile(a, writer);
                    }
                    catch(NullPointerException e){
                    
                    System.out.println(PMCIDS[i]+"is giving problem");
                }
                    
                }
                        
                
      

                 writer.flush();
                 writer.close();

                //System.out.println(a.getAbstract()+"\n\n\n\n*********\n");
                //System.out.println(a.getAuthors()+"\n\n\n\n*********\n");
                
                //System.out.println(a.getTitle()+"\n\n\n\n*********\n");
                //System.out.println(a.getJournal_name()+"\n\n\n\n*********\n");
                
                //System.out.println(P.getNumOfTablesInArticle(a)+"\n\n\n\n*********\n");
                
                
                
//                for (int s = 0; s < a.getTables().length; s++) {
//					if (a.getTables()[s].cells == null)
//						continue;
//					Cell[][] original_cells = new Cell[a.getTables()[s].cells.length][];
//                                        System.err.println("I am here");
//					for (int i = 0; i < a.getTables()[s].cells.length; i++) {
//						original_cells[i] = new Cell[a.getTables()[s].cells[i].length];
//						for (int j = 0; j < a.getTables()[s].cells[i].length; j++)
//							original_cells[i][j] = new Cell(
//									a.getTables()[s].cells[i][j]);
//					}
//					a.getTables()[s].original_cells = original_cells;
//				}
                
                
                
                        
                        
                
	
    }

}
