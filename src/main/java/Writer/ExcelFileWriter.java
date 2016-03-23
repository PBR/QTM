/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template TraitTablefile, choose Tools | Templates
 * and open the template in the editor.
 */
package Writer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tablInEx.Article;
import tablInEx.TablInExMainGnr;

/**
 *
 * @author gurnoor
 */
public class ExcelFileWriter {
    
    
    public static FileWriter TraitTablefile;
    public static FileWriter TableProtiesfile;

    public static void TraitTablesFirstLine(String File) throws IOException {
        
        ExcelFileWriter.TraitTablefile = new FileWriter(File);
        TraitTablefile.append("Table_id"+";");
        TraitTablefile.append("TableCaption"+";");
        TraitTablefile.append("Tablefooter"+"\n");
    }
    
    
    public static void PropertiesTableFirstLine(String File) throws IOException {
        
        ExcelFileWriter.TableProtiesfile = new FileWriter(File);
        TableProtiesfile.append("Table_id"+";");
        TableProtiesfile.append("isColSpan"+';');
        TableProtiesfile.append("isRowSpan"+';');
        TableProtiesfile.append("HasTableHeader"+';');
        TableProtiesfile.append("hasmultipletable"+";");
        TableProtiesfile.append("hastablebody"+";");
        TableProtiesfile.append("TableCaption"+";");
        TableProtiesfile.append("Tablefooter"+"\n");
    }
                
    public static void TraitTablesEntries(Article a) throws IOException{
	
                for (int s = 0; s < a.getTables().length; s++) {
                    
                    if(a.getTables()[s].isaTraitTable()==true)
                    {
                    TraitTablefile.append(a.getPmc()+"_"+"Table"+s+';');
                    TraitTablefile.append(a.getTables()[s].getTable_caption().replaceAll("\\s","_")+ ";");
                    TraitTablefile.append(a.getTables()[s].getTable_footer().replaceAll("\\s","_")+ "\n");
                    }    
                }
    }
    
    public static void TablePropertiesEntries(Article a) throws IOException{
	
                for (int s = 0; s < a.getTables().length; s++) {
                    
                    if(a.isCotainingTraitTables()==true)
                    {
                    TableProtiesfile.append(a.getPmc()+"_"+"Table"+s+';');
                    TableProtiesfile.append(a.getTables()[s].isColSpanning() + ";");
                    TableProtiesfile.append(a.getTables()[s].isRowSpanning()+ ";");
                    TableProtiesfile.append(a.getTables()[s].isHasHeader()+ ";");
                    TableProtiesfile.append("NKnow"+ ";");
                    TableProtiesfile.append(a.getTables()[s].isHasBody()+ ";");
                    TableProtiesfile.append(a.getTables()[s].getTable_caption().replaceAll("\\s","_")+ ";");
                    TableProtiesfile.append(a.getTables()[s].getTable_footer().replaceAll("\\s","_")+ "\n");
                    }    
                }
    }
    
    public static void LastLine()throws IOException{
        if(TraitTablefile!=null){
        TraitTablefile.flush();
        TraitTablefile.close();    
        }
        if(TableProtiesfile!=null){
        TableProtiesfile.flush();
        TableProtiesfile.close();    
        }
        
    }
}
