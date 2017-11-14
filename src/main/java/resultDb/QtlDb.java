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

    public static Connection c;

    public static String sJdbc = Configs.getPropertyQTM("sJdbc");
    public static String dbName = Configs.getPropertyQTM("dbName");
    public static String userNameDb = Configs.getPropertyQTM("userName");
    public static String passwordDb = Configs.getPropertyQTM("password");
    // String sTempDb = "TixDb_"+a.getPmc()+".db";

    public static boolean connectionDB() {
        c = null;
        try {

            Class.forName("org.sqlite.JDBC");
            String sDBUrl = sJdbc + ":" + dbName;
            c = DriverManager.getConnection(sDBUrl, userNameDb, passwordDb);

        } catch (Exception e) {
            System.out.println("Error in connecting to the output database");
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        //System.out.println("Results Database file is: \t"+dbName);

        return true;
    }

    public static void createTables() {
        try {
            if (connectionDB()) {
                Process p = Runtime.getRuntime()
                        .exec(new String[] { "bash", "-c", "sqlite3 " + QtlDb.dbName + "< db_schema.sql" });
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

                if (connectionDB() & isArticleEntryAlredyIn(articles[i], c) == false) {
                    Statement articlestmt = null;
                    articlestmt = c.createStatement();

                    Statement abbrevstmt = null;
                    abbrevstmt = c.createStatement();

                    Statement qtlTablestmt = null;
                    qtlTablestmt = c.createStatement();

                    Statement colstmt = null;
                    colstmt = c.createStatement();

                    Statement cellstmt = null;
                    cellstmt = c.createStatement();

                    // Article Table entry
                    String pmc_id = articles[i].getPmc();
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

                                String tab_id = t.getTableid();
                                int n_cols = t.getNum_of_columns();
                                int n_rows = t.getNum_of_rows();

                                System.out.println("Inserting entries for Table: \t" + tab_id);

                                String insertQTLTable = "INSERT INTO TRAIT_TABLE VALUES('" + tab_id + "'," + n_cols + "," + n_rows
                                        + ",'" + pmc_id + "');";
                                qtlTablestmt.executeUpdate(insertQTLTable);

                                for (Columns col : t.getTableCol()) {

                                    String colAnno = "";

                                    try {
                                        if (col.getColumns_type() == "QTL value") {
                                            colAnno = solr.tagger.recognize.Evaluate2.processString(col.getHeader(), "STATO",
                                                    "LONGEST_DOMINANT_RIGHT", "dictionary");
                                        } else if (col.getColumns_type() == "QTL property") {
                                            colAnno = solr.tagger.recognize.Evaluate2.processString(
                                                    getOnlyStrings(col.getHeader()), "propTerms", "LONGEST_DOMINANT_RIGHT",
                                                    "dictionary");
                                            ;
                                        }
                                    } catch (Exception e) {
                                        colAnno = "";
                                        System.out.println("error in column Annotation");
                                        e.getMessage();
                                    }

                                    String insertColTable = "INSERT INTO COLUMN_ENTRY(col_id,header,type, annot, tab_id) VALUES('"
                                            + col.getColID() + "','" + col.getHeader() + "','" + col.getColumns_type() + "','"
                                            + colAnno + "','" + t.getTableid() + "');";

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

                                            String insertCellTable = "INSERT INTO CELL_ENTRY(row_id, value, type, col_id) VALUES("

                                                    + cel.getRow_number() + ",'" + cel.getcell_value() + "','"
                                                    + cel.getCell_type() + "','" + col.getColID() + "');";
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

    public static void insertTraitEntry(Article articles[]) {
        try {

            for (int i = 0; i < articles.length; i++) {
                if (connectionDB()) {
                    Statement stmt1 = null;
                    Statement stmt2 = null;
                    Statement stmt3 = null;
                    stmt1 = c.createStatement();
                    stmt2 = c.createStatement();
                    stmt3 = c.createStatement();
                    // System.out.println("I am here");

                    List<Trait> traits = articles[i].getTraits();

                    String sql1 = "SELECT value,col_id,row_id from CELL_ENTRY" + " WHERE type !='Empty' AND"
                            + " col_id IN (SELECT col_id FROM COLUMN_ENTRY WHERE type='QTL descriptor' AND col_id LIKE '"
                            + articles[i].getPmc() + "%');";
                    ResultSet rs1 = stmt1.executeQuery(sql1);

                    while (rs1.next()) {
                        String value = rs1.getString("value");
                        String colId = rs1.getString("col_id");
                        String tableId = colId.substring(0, (colId.length() - 2));
                        int row = rs1.getInt("row_id");

                        //System.out.println("**********");
                        //System.out.println(value + "\t" + colId + "\t" + tableId + "\t" + row);
                        //System.out.println("**********");

                        Trait T = new Trait(value);
                        JSONObject vals = new JSONObject();
                        JSONObject prop = new JSONObject();
                        JSONObject otherProp = new JSONObject();

                        String sql2 = "SELECT C.Value, Col.header,Col.type, Col.annot FROM CELL_ENTRY AS C INNER JOIN COLUMN_ENTRY AS Col ON C.col_id=Col.col_id  WHERE row_id ="
                                + row + " AND Col.tab_id='" + tableId + "' AND Col.Type!='QTL descriptor'";
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

                                vals.put(colAnnoJSON, colHeader);

                            } else if (colType.equals("QTL property")) {
                                prop.put(colAnnoJSON, colHeader);

                            } else {
                                otherProp.put(colAnnoJSON, colHeader);

                            }

                        }

                        T.setTraitValues(vals);
                        T.setTraitProperties(prop);
                        T.setOtherProperties(otherProp);

                        traits.add(T);

                        String insertTraitTable = "INSERT INTO TRAIT(trait_name,trait_value_pair,trait_prop_pair,other_trait_pair, pmc_id, tab_id, row_id) VALUES('"
                                + value + "','" + T.getTraitValues() + "','" + T.getTraitProperties() + "','"
                                + T.getOtherProperties() + "','" + articles[i].getPmc() + "','" + tableId + "','" + row + "');";

                        stmt3.executeUpdate(insertTraitTable);
                        stmt3.close();
                        stmt2.close();
                        // System.out.println("programm running till here ");

                    }
                    stmt1.close();

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // System.exit(0);
    }

    public static void insertQtlTable() {
        try {

            String server = "http://localhost:8983/solr";
            String core1 = "terms";
            String core2 = "statoTerms";
            String core3 = "propTerms";
            String core4 = "solaLyco";
            String core5 = "SGN";
            String match = "LONGEST_DOMINANT_RIGHT";
            String type = "dictionary";

            if (connectionDB()) {
                Statement stmt1 = null;
                stmt1 = c.createStatement();
                Statement stmt2 = null;
                stmt2 = c.createStatement();
                Statement stmt3 = null;
                stmt3 = c.createStatement();
                String sql1 = "SELECT DISTINCT(pmc_id) FROM TRAIT;";

                ResultSet rs1 = stmt1.executeQuery(sql1);

                while (rs1.next()) {

                    String pmcId = rs1.getString("pmc_id");
                    System.out.println("Finding QTL statements in: \t" + pmcId);
                    String sql2 = "SELECT * FROM TRAIT WHERE pmc_id LIKE '" + pmcId + "';'";

                    ResultSet rs2 = stmt2.executeQuery(sql2);

                    while (rs2.next()) {

                        String traitName = rs2.getString("trait_name");// .replaceAll("\\(.*\\)",
                        String traitId = rs2.getString("trait_id"); // "");
                        String rowNumber = rs2.getString("row_id");
                        String tableId = rs2.getString("tab_id");
                        System.out.println("***");
                        System.out.println("TraitName: " + traitName);

                        String traitAnno = "";
                        traitAnno = solr.tagger.recognize.Evaluate2.processString(getOnlyStrings(traitName), core1, match, type);
                        
                        
                        JSONObject traitAnnoJSON = new JSONObject();
                        
                        if (!"".equals(traitAnno)) {
                            traitAnnoJSON = processSolrOutputtoJson(traitAnno);
                            System.out.println(traitAnnoJSON.toJSONString());
                        }

                        else {
                            traitAnnoJSON.put("icd", "");
                            traitAnnoJSON.put("matchingText", traitName);
                            traitAnnoJSON.put("prefTerm", traitName);
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
                        // Parsing Trait Values

                        String traitValue = rs2.getString("trait_value_pair");
                        JSONParser parserV = new JSONParser();
                        JSONObject tValuesJson = (JSONObject) parserV.parse(traitValue);

                        for (Iterator iterator = tValuesJson.keySet().iterator(); iterator.hasNext();) {
                            String key = (String) iterator.next();
                            String value = tValuesJson.get(key).toString();
                            JSONParser parser2 = new JSONParser();
                            JSONObject statJsonv = (JSONObject) parserV.parse(key);

                            // Filter out Chromosome Number
                            String REGEX = "chr";
                            Pattern pattern = Pattern.compile(REGEX, Pattern.CASE_INSENSITIVE);
                            // System.out.println("pref term is \t"+
                            // statJsonv.get("prefTerm") );
                            // System.out.println("column heading is \t"+ value
                            // );
                            // System.out.println("actual value is \t"+
                            // statJsonv.get("actualValue") );

                            if (statJsonv.get("actualValue").equals("chromosome") || pattern.matcher(value).find()
                                    || statJsonv.get("actualValue").equals("chromosome")) {
                                ChromosomeNumber += statJsonv.get("actualValue").toString();
                            }
                        }

                        // Parsing Trait Properties

                        String traitPro = rs2.getString("trait_prop_pair");
                        JSONParser parserP = new JSONParser();
                        JSONObject tProJson = (JSONObject) parserP.parse(traitPro);

                        for (Iterator iterator = tProJson.keySet().iterator(); iterator.hasNext();) {
                            String key = (String) iterator.next();
                            String pvalue = tProJson.get(key).toString();
                            JSONParser parser2 = new JSONParser();
                            JSONObject statJsonp = (JSONObject) parserP.parse(key);

                            // Filter out Chromosome Number
                            String regex = "chromo";
                            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

                            if (statJsonp.get("prefTerm").equals("chromosome") || pattern.matcher(pvalue).find()) {
                                ChromosomeNumber += statJsonp.get("actualValue").toString();
                            }

                            // featureName_asinArticle like '%geno%' or
                            // featureName_asinArticle like '%gene%' or
                            // featureName_asinArticle like '%marker%' or
                            // featureValue like '%sol%' or

                            // Filterout SNP

                            
                            
                            
                            
                            
                            
                            regex = "snp";
                            String s1 = (String) statJsonp.get("actualValue");
                            String s2 = (String) statJsonp.get("prefTerm");
                            String s3 = pvalue;
                            //System.out.println(s1);
                            //System.out.println(s2);
                            //System.out.println(s3);

                            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                            Matcher matcher1 = pattern.matcher(s1);
                            Matcher matcher2 = pattern.matcher(s2);
                            Matcher matcher3 = pattern.matcher(s3);
                            if (matcher1.find() || matcher2.find() || matcher3.find()) {

                                markers_associated += statJsonp.get("actualValue").toString() + "; ";
                                try {
                                    markerOntologyAnnotation = solr.tagger.recognize.Evaluate2.processString(gene_associated,
                                            "SGN", match, type);

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
                            regex = "gen[eo]";
                            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                            //System.out.println(pattern);
                            matcher1 = pattern.matcher(s1);
                            matcher2 = pattern.matcher(s2);
                            matcher3 = pattern.matcher(s3);

                            if ((matcher1.find() || matcher2.find() || matcher3.find())
                                    || (matcher1.find() || matcher2.find() || matcher3.find())) {
                                //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                gene_associated += statJsonp.get("actualValue").toString() + "; ";
                                //System.out.println("gene is"+gene_associated);

                                try {
                                    geneOntologyAnnotation = solr.tagger.recognize.Evaluate2.processString(gene_associated, "SGN",
                                            match, type);

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


                            regex = "solyc";
                            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                            //System.out.println(pattern);
                            matcher1 = pattern.matcher(s1);
                            matcher2 = pattern.matcher(s2);
                            matcher3 = pattern.matcher(s3);

                            if (matcher1.find() || matcher2.find() || matcher3.find()) {
                                //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                gene_associated += statJsonp.get("actualValue").toString() + "; ";
                                // System.out.println("gene is"+gene_associated);

                                try {
                                    geneOntologyAnnotation = solr.tagger.recognize.Evaluate2.processString(gene_associated, "SGN",
                                            match, type);
                                    

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

                            System.out.println("I am here" + markers.size() +"\t"+ genes.size());    
                            
                            // Filterout Marker
                            regex = "marker";
                            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                            matcher1 = pattern.matcher(s1);
                            matcher2 = pattern.matcher(s2);
                            matcher3 = pattern.matcher(s3);
                            if (matcher1.find() || matcher2.find() || matcher3.find()) {
                                // marker_start -- marker_end -- peak_marker
                                // TEXT
                                //System.out.println(matcher1.find()+"\t"+matcher2.find()+"\t"+matcher3.find());
                                markers_associated += statJsonp.get("actualValue").toString() + "; ";

                                try {
                                    markerOntologyAnnotation = solr.tagger.recognize.Evaluate2.processString(gene_associated,
                                            "SGN", match, type);
                                    

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

                        }

                        String qtlId = tableId + "_" + rowNumber;

                        
                        if (markers_associated != "" || gene_associated != "") {
                            
                            String genes_icd="";
                            
                            for (JSONObject g : genes){
                                genes_icd+=g.get("icd")+";";
                            }
                            
                            String markers_icd="";
                            for (JSONObject m : markers){
                                markers_icd+=m.get("icd")+";";
                            }
                            
                            
                            if("".equals(traitAnnoJSON.get("icd")) || ";".equals(traitAnnoJSON.get("icd")) ||traitAnnoJSON.isEmpty())
                                traitAnnoJSON.put("icd",null);
                            
                            if("".equals(ChromosomeNumber) || ";".equals(ChromosomeNumber) ||ChromosomeNumber.isEmpty())
                                ChromosomeNumber=null;
                            
                            if("".equals(markers_icd) || ";".equals(markers_icd) ||markers_icd.isEmpty())
                                markers_icd=null;
                            
                            if("".equals(genes_icd) || ";".equals(genes_icd) ||genes_icd.isEmpty())
                                genes_icd=null;
                            
                                
                            String insertQTLZtable = "INSERT INTO  QTL(trait_in_article,trait_in_onto,trait_uri,"
                                    + "chromosome,marker,marker_uri, gene,gene_uri, pmc_id,tab_id,row_id)"
                                    + "VALUES('" + traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
                                    + traitAnnoJSON.get("icd") + "','" + ChromosomeNumber + "','" + markers_associated + "','"
                                    + markers_icd + "','" + gene_associated + "','" + genes_icd + "','"
                                    + pmcId + "','" + tableId + "','"
                                    + rowNumber + "');";

                            stmt3.executeUpdate(insertQTLZtable);

                        }

                    }

                    //System.out.println("pmcid IS"+pmcId);
                }

                stmt3.close();
                stmt2.close();
                stmt1.close();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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