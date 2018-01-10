/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import qtm.Article;
import qtm.Cell;
import qtm.Columns;
import qtm.Table;
import qtm.Trait;
import utils.Configs;

/**
 *
 * @author gurnoor
 */
public class QtlDb {

    public static Connection conn;

    public static String dbDriver = Configs.getPropertyQTM("dbDriver");
    public static String dbFile = Configs.getPropertyQTM("dbFile");
    // String sTempDb = "TixDb_"+a.getPmc()+".db";
    
    private static String solrUri=Configs.getPropertyQTM("solrUri");
    private static String solrRun=Configs.getPropertyQTM("solrRun");
    private static String core1=Configs.getPropertyQTM("core1");
    private static String core2=Configs.getPropertyQTM("core2");
    private static String core3=Configs.getPropertyQTM("core3");
    private static String core5=Configs.getPropertyQTM("core5");
    private static String coreSGNMarkers=Configs.getPropertyQTM("coreSGNMarkers");
    private static String coreSGNgenes=Configs.getPropertyQTM("coreSGNgenes");
    
    private static String match=Configs.getPropertyQTM("match");
    private static String type=Configs.getPropertyQTM("type");
    private static String core1Dir=Configs.getPropertyQTM("core1Dir");
    
    
    public static boolean connectionDB() {
        conn = null;
        try {

            Class.forName("org.sqlite.JDBC");
            String sDBUrl = dbDriver + ":" + dbFile;
            conn = DriverManager.getConnection(sDBUrl);

        } catch (Exception e) {
            System.out.println("Error in connecting to the output database");
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return true;
    }

    public static void createTables() {
        try {
            if (connectionDB()) {
                Process p = Runtime.getRuntime()
                        .exec(new String[] { "bash", "-c", "sqlite3 " + QtlDb.dbFile + "< db_schema.sql" });
                p.waitFor();

            }
        } catch (Exception e) {
            System.out.println("Error in creating database tables");
            e.printStackTrace();
        }
    }

    public static String getOnlyStrings(String s) {
        Pattern pattern = Pattern.compile("[^a-z A-Z]");
        Matcher matcher = pattern.matcher(s);
        String number = matcher.replaceAll("");
        return number;
    }

    public static void insertArticleEntry(Article articles[]) {
        try {
            //System.out.println("Article length is" + articles.length);

            for (int i = 0; i < articles.length; i++) {
                //System.out.println("Article pmcid is" + articles[i].getPmc());

                if (connectionDB() & isArticleEntryAlredyIn(articles[i], conn) == false) {
                    Statement articlestmt = null;
                    articlestmt = conn.createStatement();

                    Statement abbrevstmt = null;
                    abbrevstmt = conn.createStatement();

                    Statement qtlTablestmt = null;
                    qtlTablestmt = conn.createStatement();

                    Statement colstmt = null;
                    colstmt = conn.createStatement();

                    Statement cellstmt = null;
                    cellstmt = conn.createStatement();

                    // Article Table entry
                    Scanner id = new Scanner(articles[i].getPmc()).useDelimiter("[^0-9]+");
                    int pmc_id = id.nextInt();
                    String pmc_tittle = articles[i].getTitle();
                    
                    String insertArticleTable = "INSERT INTO ARTICLE VALUES('" + pmc_id + "','" + pmc_tittle + "');";
                    articlestmt.executeUpdate(insertArticleTable);

                    // System.out.println("Entry done for article number" + "\t"
                    // + articleID);

                    for (String key : articles[i].getAbbreviations().keySet()) {

                        String insertAbrevTable = "INSERT INTO ABBREVIATION VALUES('" + key + "','"
                                + articles[i].getAbbreviations().get(key) + "','" + pmc_id + "');";
                        abbrevstmt.executeUpdate(insertAbrevTable);
                    }
                    abbrevstmt.close();
                    // System.out.println("Abbreviation entries inserted in the
                    // DB");
                    // QTL table entries
                    for (Table t : articles[i].getTables()) {

                        // checking if table exists
                        try {
                            if (t.isaTraitTable()) {

                               
                               System.out.println("Inserting entries for Table: \t" + t.getTableid());

                               String insertQTLTable = "INSERT INTO TRAIT_TABLE VALUES("+ t.getTableid() + "," + pmc_id + ");";
                               qtlTablestmt.executeUpdate(insertQTLTable);

                                for (Columns col : t.getTableCol()) {

                                    String colAnno = "";

                                    try {
                                        if (col.getColumns_type() == "QTL value") {
                                            colAnno = solr.tagger.recognize.Evaluate.processString(col.getHeader(), core2,
                                                    match, type);
                                        } else if (col.getColumns_type() == "QTL property") {
                                            colAnno = solr.tagger.recognize.Evaluate.processString(
                                                    getOnlyStrings(col.getHeader()), core3, match, type);
                                            ;
                                        }
                                    } catch (Exception e) {
                                        colAnno = "";
                                        System.out.println("error in column Annotation");
                                        e.getMessage();
                                    }

                                    String insertColTable = "INSERT INTO COLUMN_ENTRY(col_id,tab_id, pmc_id, header,type, annot) VALUES("
                                            +col.getColID()+"," + t.getTableid() + "," + pmc_id + ",'" + col.getHeader() + "','" + col.getColumns_type() + "','"
                                            + colAnno + "');";

                                    colstmt.executeUpdate(insertColTable);
                                    // System.out.println("Col entries inserted
                                    // in the
                                    // DB");

                                    for (Cell cel : col.getcelz()) {

                                        // checknig if cell exists
                                        try {
                                            //System.out.println(cel.getRow_number() + "\t" + cel.getcell_value() + "\t\t" + tableID);
                                        } catch (NullPointerException e) {
                                            continue;
                                        }

                                        if (cel.getcell_value().indexOf("'") != -1)
                                            cel.setcell_values(cel.getcell_value().replace("'", "''"));

                                        if (cel.getcell_value() != null) {

                                            String insertCellTable = "INSERT INTO CELL_ENTRY(row_id, col_id, tab_id, pmc_id, value,type ) VALUES("
                                                    + cel.getRow_number() + "," + col.getColID() + "," + t.getTableid() + "," + pmc_id 
                                                    + ",'" + cel.getcell_value() +"','"+ cel.getCell_type()+ "');";
                                            cellstmt.executeUpdate(insertCellTable);

                                            // System.out.println("Cell entries
                                            // inserted in the
                                            // DB"+cel.getRow_number());
                                        }

                                    }
                                    cellstmt.close();
                                }
                                colstmt.close();
                            }
                        } catch (NullPointerException e) {
                            continue;
                        }

                    }
                    qtlTablestmt.close();

                    // InsertTraitEntry(a);

                } else {
                    System.out.println(articles[i].getPmc() + " already exists");

                }
            }

            //System.out.println("entry inserted into DB successfully");
            // System.exit(0);
        } catch (Exception e) {
            System.out.println("Error is Database entry");
            e.printStackTrace();

        }
    }

    public static void insertQTLEntry(Article articles[]) {
        try {

            for (int i = 0; i < articles.length; i++) {
                
                Scanner id = new Scanner(articles[i].getPmc()).useDelimiter("[^0-9]+");
                int pmc_id = id.nextInt();
                String pmc_tittle = articles[i].getTitle();        
                
                if (connectionDB()) {
                    Statement stmt1 = null;
                    Statement stmt2 = null;
                    Statement stmt3 = null;
                    stmt1 = conn.createStatement();
                    stmt2 = conn.createStatement();
                    stmt3 = conn.createStatement();
                    // System.out.println("I am here");

                    List<Trait> traits = articles[i].getTrait();

                    String sql1 = "SELECT  Cel.value, Cel.col_id, Cel.row_id, Cel.tab_id, Col.type from CELL_ENTRY AS Cel INNER JOIN COLUMN_ENTRY AS Col ON Cel.col_id=Col.col_id AND Cel.tab_id=Col.tab_id AND Cel.pmc_id = Col.pmc_id" 
                    + " WHERE Cel.type !='Empty' AND "
                    + " Col.type='QTL descriptor' "
                    + " AND Cel.pmc_id = "+ pmc_id + ";";
                    
                    ResultSet rs1 = stmt1.executeQuery(sql1);
                    while (rs1.next()) {
                        String pTrait = rs1.getString("value");
                        int colId = rs1.getInt("col_id");
                        double tableId = rs1.getDouble("tab_id");
                        int rowId = rs1.getInt("row_id");

                        //System.out.println("**********");
                        //System.out.println(value + "\t" + colId + "\t" + tableId + "\t" + row);
                        //System.out.println("**********");

                        Trait T = new Trait(pTrait);
                        
                        String traitAnno = "";
                        traitAnno = solr.tagger.recognize.Evaluate.processString(getOnlyStrings(T.getTraitName()), core1, match, type);

                        JSONObject traitAnnoJSON = new JSONObject();

                        if (!"".equals(traitAnno)) {
                            traitAnnoJSON = processSolrOutputtoJson(traitAnno);
                            System.out.println(traitAnnoJSON.toJSONString());
                        }

                        else {
                            traitAnnoJSON.put("icd", "");
                            traitAnnoJSON.put("matchingText", T.getTraitName());
                            traitAnnoJSON.put("prefTerm", T.getTraitName());
                            traitAnnoJSON.put("Term", "");
                            traitAnnoJSON.put("start", "");
                            traitAnnoJSON.put("end", "");
                            traitAnnoJSON.put("Uuid", "");
                        }
                        
                        String ChromosomeNumber = "";
                        String gene_associated = "";
                        String geneOntologyAnnotation = "";
                        String markers_associated = "";
                        String markerOntologyAnnotation = "";

                        JSONObject markerJSON = new JSONObject();
                        JSONObject geneJSON = new JSONObject();
                        JSONObject snpJSON = new JSONObject();
                        
                        Set<JSONObject> markers = new HashSet<JSONObject>();

                        Set<JSONObject> genes = new HashSet<JSONObject>();
                        
                        
                        JSONObject vals = new JSONObject();
                        JSONObject prop = new JSONObject();
                        JSONObject otherProp = new JSONObject();

                        String sql2 = "SELECT Cel.Value, Col.header,Col.type, Col.annot FROM CELL_ENTRY AS Cel INNER JOIN COLUMN_ENTRY AS Col ON Cel.col_id=Col.col_id AND Cel.tab_id=Col.tab_id AND Cel.pmc_id = Col.pmc_id"
                                + " WHERE row_id =" + rowId + " AND"
                                        + " Col.tab_id='" + tableId + "' AND"
                                                + " Col.Type!='QTL descriptor' AND"
                                                + " Cel.pmc_id="+ pmc_id +";" ;
                        ResultSet rs2 = stmt2.executeQuery(sql2);

                        
                        while (rs2.next()) {
                            
                            String cellValue = rs2.getString("value").replaceAll("\n", "").replace("\r", "");
                            String colHeader = rs2.getString("header").replaceAll("\n", "").replace("\r", "");
                            String colType = rs2.getString("type").replaceAll("\n", "").replace("\r", "");
                            String colAnno = rs2.getString("annot").replaceAll("\n", "").replace("\r", "");

                            JSONObject colAnnoJSON = new JSONObject();

                            if (!"".equals(colAnno)) {
                                colAnnoJSON = processSolrOutputtoJson(colAnno);
                                colAnnoJSON.put("actualValue", cellValue);
                            } else {
                                colAnnoJSON.put("icd", "");
                                colAnnoJSON.put("matchingText", colHeader);
                                colAnnoJSON.put("prefTerm", colHeader);
                                colAnnoJSON.put("Term", "");
                                colAnnoJSON.put("start", "");
                                colAnnoJSON.put("end", "");
                                colAnnoJSON.put("Uuid", "");
                                colAnnoJSON.put("actualValue", cellValue);
                            }

                            //System.out.println("Entry " + "\t" + cellValue + "\t" + colHeader + "\t" + colType);

                            if (colType.equals("QTL value")) {
                                // System.out.println("cell Values is" +
                                // cellValue);
                                
                                String regex = "chr";
                                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                                
                                Matcher matcher1 = pattern.matcher(cellValue);
                                Matcher matcher2 = pattern.matcher(colHeader);
                                
                                if (matcher1.find() || matcher2.find()){
                                    ChromosomeNumber += cellValue.toString();
                                }
                                
                                vals.put(colAnnoJSON, colHeader);

                            } else if (colType.equals("QTL property")) {
                                
                                
                                String regex1 = "marker";
                                String regex2 = "snp";
                                
                                Pattern pattern1 = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE);
                                Pattern pattern2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE);

                                Matcher matcher1 = pattern1.matcher(cellValue);
                                Matcher matcher2 = pattern1.matcher(colHeader);
                                
                                Matcher matcher3 = pattern2.matcher(cellValue);
                                Matcher matcher4 = pattern2.matcher(colHeader);
                                                     
                                
                                if (matcher1.find() || matcher2.find() || matcher3.find() || matcher4.find()) {

                                    markers_associated += cellValue + " ; ";

                                    try {
                                        markerOntologyAnnotation = solr.tagger.recognize.Evaluate.processString(markers_associated,
                                                coreSGNMarkers, match, type);

                                    } catch (Exception e) {
                                        markerOntologyAnnotation = "";
                                        System.out.println("error in solar annotations");

                                    }

                                    if (!"".equals(markerOntologyAnnotation)) {
                                        markerJSON = processSolrOutputtoJson(markerOntologyAnnotation);
                                        markers.add(markerJSON);
                                    }

                                    else {
                                        markerJSON.put("icd", "");
                                        markerJSON.put("matchingText", markers_associated);
                                        markerJSON.put("prefTerm", markers_associated);
                                        markerJSON.put("Term", "");
                                        markerJSON.put("start", "");
                                        markerJSON.put("end", "");
                                        markerJSON.put("Uuid", "");
                                        markers.add(markerJSON);
                                    }

                                }
                                
                             // Filterout Gene
                                regex1 = "gen[eo]";
                                regex2 = "solyc";
                                pattern1 = Pattern.compile(regex1, Pattern.CASE_INSENSITIVE);
                                pattern2 = Pattern.compile(regex2, Pattern.CASE_INSENSITIVE);

                                matcher1 = pattern1.matcher(cellValue);
                                matcher2 = pattern1.matcher(colHeader);
                                

                                matcher3 = pattern2.matcher(cellValue);
                                matcher4 = pattern2.matcher(colHeader);
                                
                                if (matcher1.find() || matcher2.find() || matcher3.find() || matcher4.find()) {

                                    //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                    gene_associated += cellValue + "; ";
                                    //System.out.println("gene is"+gene_associated);

                                    try {
                                        geneOntologyAnnotation = solr.tagger.recognize.Evaluate.processString(gene_associated,
                                                coreSGNgenes, match, type);

                                    } catch (Exception e) {
                                        geneOntologyAnnotation = "";
                                        System.out.println("error in solar annotations");

                                    }

                                    if (!"".equals(geneOntologyAnnotation)) {

                                        geneJSON = processSolrOutputtoJson(geneOntologyAnnotation);
                                        genes.add(geneJSON);
                                    }

                                    else {
                                        geneJSON.put("icd", "");
                                        geneJSON.put("matchingText", gene_associated);
                                        geneJSON.put("prefTerm", gene_associated);
                                        geneJSON.put("Term", "");
                                        geneJSON.put("start", "");
                                        geneJSON.put("end", "");
                                        geneJSON.put("Uuid", "");
                                        
                                        genes.add(geneJSON);    
                                   }

                                }
                                                           
                                
                                
                                
                                
                                prop.put(colAnnoJSON, colHeader);

                            } else {
                                otherProp.put(colAnnoJSON, colHeader);

                            }

                        }

                        T.setTraitValues(vals);
                        T.setTraitProperties(prop);
                        T.setOtherProperties(otherProp);

                        traits.add(T);
                        
                        System.out.println(T.getTraitName());
                        
                        
                      
                       
                       
                        
                        if (markers_associated != "" || gene_associated != "") {

                            String genes_icd = "";

                            for (JSONObject g : genes) {
                                genes_icd += g.get("icd") + ";";
                            }

                            String markers_icd = "";
                          
                            for (JSONObject m : markers) {
                                markers_icd += m.get("icd") + ";";
                            }

                            if (markers_associated == "")
                                markers_associated=null;
                            if (gene_associated == "")
                                gene_associated=null;
                            
                            if ("".equals(traitAnnoJSON.get("icd")) || ";".equals(traitAnnoJSON.get("icd"))
                                    || traitAnnoJSON.isEmpty())
                                traitAnnoJSON.put("icd", null);

                            if ("".equals(ChromosomeNumber) || ";".equals(ChromosomeNumber) || ChromosomeNumber.isEmpty())
                                ChromosomeNumber = null;

                            if ("".equals(markers_icd) || ";".equals(markers_icd) || markers_icd.isEmpty())
                                markers_icd = null;

                            if ("".equals(genes_icd) || ";".equals(genes_icd) || genes_icd.isEmpty())
                                genes_icd = null;

                            System.out.println("*********************************************");
                            System.out.println("QTL.pmc_id, QTL.tab_id, QTL.row_id is " + pmc_id + "\t" +  tableId + "\t" + rowId);
                            
                            String insertQTLZtable = "INSERT INTO  QTL(pmc_id,tab_id,row_id, trait_in_article,trait_in_onto,trait_uri,"
                                    + "chromosome,marker,marker_uri, gene,gene_uri)" + "VALUES("
                                    + pmc_id + "," + tableId + "," + rowId + ",'"
                                    + T.getTraitName() + "','" + traitAnnoJSON.get("prefTerm") + "','" + traitAnnoJSON.get("icd") + "','"
                                    + ChromosomeNumber + "','" + markers_associated + "','" + markers_icd + "','"
                                    + gene_associated + "','" + genes_icd
                                    + "');";

                            stmt3.executeUpdate(insertQTLZtable);

                        }
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                        
                       

                    }
                    stmt1.close();
                    stmt2.close();
                    stmt3.close();

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        // System.exit(0);
    }


    public static boolean isArticleEntryAlredyIn(Article a, Connection c) {
        Boolean check = false;
        try {
            Statement stmt = null;
            stmt = c.createStatement();

            // System.out.println("I am here");
            String sql = "SELECT pmc_id FROM Article";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String pmcId = rs.getString("pmc_id");
                if (pmcId.equals(a.getPmc())) {
                    check = true;
                    return check;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
        return check;

    }

    public static JSONObject processSolrOutputtoJson(String output) {
        //System.out.println("\n" + output);
        JSONObject j = new JSONObject();
        String[] s = output.split(Pattern.quote("|"));
        // System.out.println("I am here" + s[1].toString());

        j.put("icd", s[0]);
        j.put("matchingText", s[1]);
        j.put("prefTerm", s[2]);
        j.put("Term", s[3]);
        j.put("start", s[4]);
        j.put("end", s[5]);
        j.put("Uuid", s[6]);
        return j;
    }

}
