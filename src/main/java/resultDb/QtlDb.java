/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package resultDb;

import java.awt.Cursor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

    public static Connection conn = null;

    public static String dbDriver = Configs.getPropertyQTM("dbDriver");
    public static String dbFile = Configs.getPropertyQTM("dbFile");
    // String sTempDb = "TixDb_"+a.getPmc()+".db";

    private static String solrUri = Configs.getPropertyQTM("solrUri");
    private static String solrRun = Configs.getPropertyQTM("solrRun");
    private static String core1 = Configs.getPropertyQTM("core1");
    private static String core2 = Configs.getPropertyQTM("core2");
    private static String core3 = Configs.getPropertyQTM("core3");
    private static String coreSGNMarkers = Configs.getPropertyQTM("coreSGNMarkers");
    private static String coreSGNgenes = Configs.getPropertyQTM("coreSGNgenes");

    private static String match = Configs.getPropertyQTM("match");
    private static String type = Configs.getPropertyQTM("type");
    private static String core1Dir = Configs.getPropertyQTM("core1Dir");

    public static boolean connectionDB() {
     if(conn == null){
        
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
     } else
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

        //System.out.println("Article length is" + articles.length);

        for (int i = 0; i < articles.length; i++) {
            //System.out.println("Article pmcid is" + articles[i].getPmc());
            try {
                if (connectionDB() & isArticleEntryAlredyIn(articles[i], conn) == false) {

                    Statement abbrevstmt = null;
                    abbrevstmt = conn.createStatement();

                    Statement getRowidStmt = null;
                    getRowidStmt = conn.createStatement();

                    // Article Table entry
                    Scanner id = new Scanner(articles[i].getPmc()).useDelimiter("[^0-9]+");
                    int pmc_id = id.nextInt();
                    String pmc_tittle = articles[i].getTitle();

                    try {

                        String insertArticleTable = "INSERT INTO ARTICLE VALUES" + "(?,?)";

                        PreparedStatement articlestmt = conn.prepareStatement(insertArticleTable);

                        articlestmt.setInt(1, pmc_id);
                        try {
                            articlestmt.setString(2, pmc_tittle);
                        } catch (NullPointerException e) {
                            articlestmt.setNull(2, java.sql.Types.VARCHAR);
                        }

                        articlestmt.executeUpdate();
                        articlestmt.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("*************************************************");

                        System.out.println("Article already exits, Please provide unique entries");

                        System.out.println("*************************************************");

                        System.exit(1);
                    }
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

                                System.out.println("Inserting entries for Table: \t" + t.getTabnum());

                                
                                String insertTraitTable = "INSERT INTO TRAIT_TABLE (tab_num, pmc_id) VALUES" + "(?,?); ";

                                PreparedStatement traitTableStmt = conn.prepareStatement(insertTraitTable);

                                traitTableStmt.setInt(1, t.getTabnum());
                                traitTableStmt.setInt(2, pmc_id);
                                
                                traitTableStmt.executeUpdate();
                                
                                traitTableStmt.close();

                                String getTabid = "select max(tab_id) from TRAIT_TABLE;";

                                ResultSet rs1 = getRowidStmt.executeQuery(getTabid);
                                int tab_id=rs1.getInt("max(tab_id)");

                                for (Columns col : t.getTableCol()) {

                                    String colAnno = "";
                                    String colHeader = col.getHeader().replaceAll("[^\\w]", "");
                                    try {
                                        if (col.getColumns_type() == "QTL value") {
                                            colAnno = solr.tagger.recognize.Evaluate.processString(colHeader, core2, match, type);
                                        } else if (col.getColumns_type() == "QTL property") {
                                            colAnno = solr.tagger.recognize.Evaluate.processString(colHeader, core3, match, type);
                                            ;
                                        }
                                    } catch (Exception e) {
                                        colAnno = "";
                                        System.out.println("error in column Annotation" + colHeader);
                                    }

                                    if (colAnno.equals("") || colAnno.equals(" ")) {
                                        colAnno = null;
                                    }

                                    String insertColTable = "INSERT INTO COLUMN_ENTRY(tab_id, header,type, annot) VALUES"
                                            + "(?,?,?,?)";

                                    PreparedStatement colStmt = conn.prepareStatement(insertColTable);

                                    colStmt.setInt(1, tab_id);

                                    try {
                                        colStmt.setString(2, col.getHeader());
                                    } catch (NullPointerException e) {
                                        colStmt.setNull(2, java.sql.Types.VARCHAR);
                                    }

                                    try {
                                        colStmt.setString(3, col.getColumns_type());
                                    } catch (NullPointerException e) {
                                        colStmt.setNull(3, java.sql.Types.VARCHAR);
                                    }

                                    try {
                                        colStmt.setString(4, colAnno);
                                    } catch (NullPointerException e) {
                                        colStmt.setNull(4, java.sql.Types.VARCHAR);
                                    }

                                    colStmt.executeUpdate();
                                    // System.out.println("Col entries inserted
                                    // in the
                                    // DB");
                                    colStmt.close();


                                    String getColid = "select max(col_id) from COLUMN_ENTRY;";

                                    ResultSet rs2 = getRowidStmt.executeQuery(getColid);
                                    int col_id=rs2.getInt("max(col_id)");

                                    
                                    
                                    for (Cell cel : col.getcelz()) {

                                        if (cel.getcell_value().indexOf("'") != -1)
                                            cel.setcell_values(cel.getcell_value().replace("'", "''"));

                                        if (cel.getcell_value().equals("") || cel.getcell_value().equals(" "))
                                            cel.setcell_values(null);

                                        String insertCellTable = "INSERT INTO CELL_ENTRY(row_id, col_id, value) VALUES"
                                                + "(?, ?, ?)";

                                        PreparedStatement cellStmt = conn.prepareStatement(insertCellTable);

                                        cellStmt.setInt(1, cel.getRow_number());
                                        cellStmt.setInt(2, col_id);
                                        
                                        try {
                                            cellStmt.setString(3, cel.getcell_value());
                                        } catch (NullPointerException e) {
                                            cellStmt.setNull(3, java.sql.Types.VARCHAR);
                                        }

                                        cellStmt.executeUpdate();
                                        cellStmt.close();
                                    }

                                }

                            }
                        } catch (NullPointerException e) {
                            continue;
                        }

                    }

                    // InsertTraitEntry(a);

                } else {
                    System.out.println(articles[i].getPmc() + " already exists");

                }

                //System.out.println("entry inserted into DB successfully");
                // System.exit(0);
            } catch (Exception e) {
                System.out.println("Error is Insert Article Function");
                e.printStackTrace();

            }
        }
    }

    public static void insertQTLEntry() {
        try {

            if (connectionDB()) {
                Statement stmt1 = null;
                Statement stmt2 = null;
                Statement stmt3 = null;
                stmt1 = conn.createStatement();
                stmt2 = conn.createStatement();
                stmt3 = conn.createStatement();
                // System.out.println("I am here");

                //                    List<Trait> traits = articles[i].getTrait();

                String sql1 = "SELECT  Cel.value, Cel.col_id, Cel.row_id, Col.tab_id, Col.type from CELL_ENTRY AS Cel INNER JOIN COLUMN_ENTRY AS Col ON Cel.col_id=Col.col_id"
                        + " WHERE Cel.value IS NOT null AND " + " Col.type='QTL descriptor' ;";

                ResultSet rs1 = stmt1.executeQuery(sql1);
                while (rs1.next()) {
                    String pTrait = rs1.getString("value");
                    int colId = rs1.getInt("col_id");
                    int tableId = rs1.getInt("tab_id");
                    int rowId = rs1.getInt("row_id");

                    //System.out.println("**********");
                    //System.out.println(value + "\t" + colId + "\t" + tableId + "\t" + row);
                    //System.out.println("**********");

                    Trait T = new Trait(pTrait);

                    String traitAnno = "";
                    traitAnno = solr.tagger.recognize.Evaluate.processString(getOnlyStrings(T.getTraitName()), core1, match,
                            type);

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

                    String sql2 = "SELECT Cel.Value, Col.header,Col.type, Col.annot FROM CELL_ENTRY AS Cel INNER JOIN COLUMN_ENTRY AS Col ON Cel.col_id=Col.col_id"
                            + " WHERE row_id =" + rowId + " AND" + " Col.tab_id='" + tableId + "' AND"
                            + " Col.Type!='QTL descriptor' ;";
                    ResultSet rs2 = stmt2.executeQuery(sql2);

                    while (rs2.next()) {
                        String cellValue = "";
                        String colHeader = "";
                        String colType = "";
                        String colAnno = "";

                        try {
                            cellValue = rs2.getString("value").replaceAll("\n", "").replace("\r", "");
                            colHeader = rs2.getString("header").replaceAll("\n", "").replace("\r", "");
                            colType = rs2.getString("type").replaceAll("\n", "").replace("\r", "");
                            colAnno = rs2.getString("annot").replaceAll("\n", "").replace("\r", "");
                        } catch (NullPointerException e) {
                        }

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

                            if (matcher1.find() || matcher2.find()) {
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
                            markers_associated = null;
                        if (gene_associated == "")
                            gene_associated = null;

                        if ("".equals(traitAnnoJSON.get("icd")) || ";".equals(traitAnnoJSON.get("icd"))
                                || traitAnnoJSON.isEmpty())
                            traitAnnoJSON.put("icd", null);

                        if ("".equals(ChromosomeNumber) || ";".equals(ChromosomeNumber) || ChromosomeNumber.isEmpty())
                            ChromosomeNumber = null;

                        if ("".equals(markers_icd) || ";".equals(markers_icd) || markers_icd.isEmpty())
                            markers_icd = null;

                        if ("".equals(genes_icd) || ";".equals(genes_icd) || genes_icd.isEmpty())
                            genes_icd = null;

                        System.out.println("");

                        String insertTableSQL = "INSERT INTO QTL"
                                + "(tab_id,row_id, trait_in_article,trait_in_onto,trait_uri,chromosome,marker,marker_uri, gene,gene_uri) VALUES"
                                + "(?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement preparedStatement = conn.prepareStatement(insertTableSQL);

                        preparedStatement.setDouble(1, tableId);
                        preparedStatement.setInt(2, rowId);

                        try {
                            preparedStatement.setString(3, T.getTraitName());
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(3, java.sql.Types.VARCHAR);
                        }
                        try {
                            preparedStatement.setString(4, traitAnnoJSON.get("prefTerm").toString());
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(4, java.sql.Types.VARCHAR);
                        }
                        try {
                            preparedStatement.setString(5, traitAnnoJSON.get("icd").toString());
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(5, java.sql.Types.VARCHAR);
                        }
                        try {
                            preparedStatement.setString(6, ChromosomeNumber);
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(6, java.sql.Types.VARCHAR);
                        }
                        try {
                            preparedStatement.setString(7, markers_associated);
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(7, java.sql.Types.VARCHAR);
                        }

                        try {
                            preparedStatement.setString(8, markers_icd);
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(8, java.sql.Types.VARCHAR);
                        }
                        try {
                            preparedStatement.setString(9, gene_associated);
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(9, java.sql.Types.VARCHAR);
                        }
                        try {
                            preparedStatement.setString(10, genes_icd);
                        } catch (NullPointerException e) {
                            preparedStatement.setNull(10, java.sql.Types.VARCHAR);
                        }

                        preparedStatement.executeUpdate();

                        //                            
                        //                            String insertQTLZtable = "INSERT INTO  QTL(pmc_id,tab_id,row_id, trait_in_article,trait_in_onto,trait_uri,"
                        //                                    + "chromosome,marker,marker_uri, gene,gene_uri)" + "VALUES(" + pmc_id + "," + tableId + ","
                        //                                    + rowId + ",'" + T.getTraitName() + "','" + traitAnnoJSON.get("prefTerm") + "','"
                        //                                    + traitAnnoJSON.get("icd") + "','" + ChromosomeNumber + "','" + markers_associated + "','"
                        //                                    + markers_icd + "','" + gene_associated + "','" + genes_icd + "');";
                        //
                        //                            stmt3.executeUpdate(insertQTLZtable);

                    }

                }
                stmt1.close();
                stmt2.close();
                stmt3.close();

            }

        } catch (SQLException e) {

            e.printStackTrace();
        } catch (Exception e) {
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

    public static int numberofTraitTable() {
        int tables = 0;
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT COUNT(*) as count FROM TRAIT_TABLE";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                tables = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return tables;
    }

    public static int numberofQTL() {
        int numQTL = 0;
        try {
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT COUNT(*) as count FROM QTL";
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                numQTL = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return numQTL;
    }

}
