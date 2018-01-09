package solrAnnotator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import qtm.Article;


public class Annotate {
        public static Connection c;

        public static boolean connectionDB() {
                c = null;
                try {
                        Class.forName("org.sqlite.JDBC");

                        String sJDBC = "jdbc:sqlite";
                        // String sTempDb = "TixDb_" + a.getPmc() + ".db";
                        String sTempDb = "TixDb.db";

                        String sDBUrl = sJDBC + ":" + sTempDb;

                        c = DriverManager.getConnection(sDBUrl);

                } catch (Exception e) {
                        System.err.println(e.getClass().getName() + ": " + e.getMessage());
                        System.exit(0);
                }
                System.out.println("Opened database successfully \n");
                return true;
        }

        public static void annotateColumnTable(Article a) {
                try {

                        if (connectionDB()) {

                                String sql1 = "Select colId, colHeader from columnEntries where tableId like '" + a.getPmc()
                                                + "%' AND colType='QTL value'; ";

                                String server = "http://localhost:8983/solr";
                                String core1 = "terms";
                                String core2 = "statoTerms";
                                String match = "LONGEST_DOMINANT_RIGHT";
                                String type = "dictionary";

                                Statement stmt1 = null;
                                Statement stmt2 = null;

                                stmt1 = c.createStatement();
                                stmt2 = c.createStatement();
                                ResultSet rs1 = stmt1.executeQuery(sql1);

                                while (rs1.next()) {
                                        String colId = rs1.getString("colId");
                                        String colName = rs1.getString("colHeader");// .replaceAll("\\(.*\\)",
                                        // "");

                                        System.out.println("\nColumn Names " + colName + "\n");

                                        String colAnno = "";
                                        try {
                                                colAnno = solr.tagger.recognize.Evaluate
                                                                .processString(getOnlyStrings(colName), core2, match, type);

                                        } catch (Exception e) {
                                                System.out.println("error in solar annotations");
                                        }

                                        JSONObject colAnnoJSON = new JSONObject();
                                        if (!"".equals(colAnno)) {

                                                colAnnoJSON = processSolarOutputtoJson(colAnno);
                                                System.out.println(colAnnoJSON.toJSONString());

                                                String UpdateColAnnotation = "UPDATE columnEntries SET colAnntations=" + colAnnoJSON.toString()
                                                                + "WHERE colId=" + colId + ";";

                                                stmt2.executeQuery(UpdateColAnnotation);

                                        }

                                        else {
                                                colAnnoJSON.put("icd", "");
                                                colAnnoJSON.put("matchingText", colName);
                                                colAnnoJSON.put("prefTerm", colName);
                                                colAnnoJSON.put("Term", "");
                                                colAnnoJSON.put("start", "");
                                                colAnnoJSON.put("end", "");
                                                colAnnoJSON.put("Uuid", "");
                                        }

                                }
                        }
                } catch (Exception e) {

                }

        }

        public static void annotateTraitTable(Article articles[]) {
                try {

                        if (connectionDB()) {

                                for (int i = 0; i < articles.length; i++) {
                                        System.out.println("Traits founds in Article" + articles[i].getPmc());

                                        String sql1 = "Select TraitId, TraitName, tableId from traits where pmcId like '"
                                                        + articles[i].getPmc() + "'; ";

                                        String server = "http://localhost:8983/solr";
                                        String core1 = "terms";
                                        String core2 = "statoTerms";
                                        String match = "LONGEST_DOMINANT_RIGHT";
                                        String type = "dictionary";

                                        Statement stmt1 = null;
                                        Statement stmt2 = null;
                                        Statement stmt3 = null;

                                        stmt1 = c.createStatement();
                                        stmt2 = c.createStatement();
                                        stmt3 = c.createStatement();
                                        ResultSet rs1 = stmt1.executeQuery(sql1);

                                        while (rs1.next()) {

                                                String traitName = rs1.getString("TraitName");// .replaceAll("\\(.*\\)",
                                                String traitId = rs1.getString("TraitId"); // "");
                                                String tableId = rs1.getString("tableId");
                                                // traitName=getOnlyStrings(traitName);

                                                System.out.println("\nTraitNames " + traitName + "\n");

                                                String traitAnno = "";
                                                try {
                                                        traitAnno = solr.tagger.recognize.Evaluate2
                                                                        .processString(getOnlyStrings(traitName), core1, match, type);

                                                } catch (Exception e) {
                                                        System.out.println("error in solar annotations");
                                                }

                                                JSONObject traitAnnoJSON = new JSONObject();
                                                if (!"".equals(traitAnno)) {

                                                        traitAnnoJSON = processSolarOutputtoJson(traitAnno);
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

                                                String sql2 = "Select TraitValues from traits where TraitId like '" + traitId
                                                                + "'AND pmcId like '" + articles[i].getPmc() + "'; ";

                                                ResultSet rs2 = stmt2.executeQuery(sql2);

                                                while (rs2.next()) {
                                                        String traitValue = rs2.getString("TraitValues");
                                                        JSONParser parser = new JSONParser();
                                                        JSONObject tValuesJson = (JSONObject) parser.parse(traitValue);

                                                        for (Iterator iterator = tValuesJson.keySet().iterator(); iterator.hasNext();) {
                                                                String v = (String) iterator.next();
                                                                String key = tValuesJson.get(v).toString();

                                                                JSONParser parser2 = new JSONParser();
                                                                JSONObject statJsonv = (JSONObject) parser.parse(v);

                                                                String insertTraitValue = "INSERT INTO traitValues(TraitOriginalName, TraitAnnotation, TraitAnnotationID , StatValue, StatValueAnnotation, StatAnnotationID, ActualValue,  pmcId, tableId) Values('"
                                                                                + traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
                                                                                + traitAnnoJSON.get("icd") + "','" + key + "','" + statJsonv.get("prefTerm")
                                                                                + "','" + statJsonv.get("icd") + "','" + statJsonv.get("actualValue") + "','"
                                                                                + articles[i].getPmc() + "','" + tableId + "');";

                                                                stmt3.executeUpdate(insertTraitValue);
                                                        }

                                                }

                                                String sql3 = "Select TraitProperties from traits where TraitName like '" + traitName
                                                                + "'AND pmcId like '" + articles[i].getPmc() + "'; ";

                                                ResultSet rs3 = stmt2.executeQuery(sql3);

                                                while (rs3.next()) {
                                                        String traitPro = rs3.getString("TraitProperties");
                                                        JSONParser parser = new JSONParser();
                                                        JSONObject tProJson = (JSONObject) parser.parse(traitPro);

                                                        for (Iterator iterator = tProJson.keySet().iterator(); iterator.hasNext();) {
                                                                String p = (String) iterator.next();
                                                                String key = tProJson.get(p).toString();
                                                                JSONParser parser2 = new JSONParser();
                                                                JSONObject statJsonp = (JSONObject) parser.parse(p);

                                                                String insertTraitValue = "INSERT INTO traitProperties(TraitOriginalName, TraitAnnotation, TraitAnnotationID , Property, ProAnnotation , ProAnnotationID ,ActualProValue,  pmcId, tableId) Values('"
                                                                                + traitName + "','" + traitAnnoJSON.get("prefTerm") + "','"
                                                                                + traitAnnoJSON.get("icd") + "','" + key + "','" + statJsonp.get("prefTerm")
                                                                                + "','" + statJsonp.get("icd") + "','" + statJsonp.get("actualValue") + "','"
                                                                                + articles[i].getPmc() + "','" + tableId + "');";

                                                                stmt3.executeUpdate(insertTraitValue);
                                                        }

                                                }

                                                stmt3.close();
                                        }

                                        // }

                                        c.close();
                                        // }
                                        //

                                }
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }

        }

        public static String getOnlyStrings(String s) {
                Pattern pattern = Pattern.compile("[^a-z A-Z]");
                Matcher matcher = pattern.matcher(s);
                String number = matcher.replaceAll("");
                return number;
        }

        public static JSONObject processSolarOutputtoJson(String output) {
                System.out.println("\n" + output);
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
