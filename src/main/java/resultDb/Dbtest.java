package resultDb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Dbtest {

    public static Connection c;
    public static String sJdbc = "jdbc:sqlite";
    public static String tempDb = "data/TixDb.db";

    public static boolean connectionDB() {
        c = null;
        try {
            Class.forName("org.sqlite.JDBC");

            String sDBUrl = sJdbc + ":" + tempDb;

            c = DriverManager.getConnection(sDBUrl);

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully \n");
        return true;
    }

    public static void main(String[] args) {
        try {

            if (connectionDB()) {
                Statement stmt1 = null;
                stmt1 = c.createStatement();
                Statement stmt2 = null;
                stmt2 = c.createStatement();
                Statement stmt3 = null;
                stmt3 = c.createStatement();
                String sql1 = "SELECT DISTINCT(pmcId) FROM Trait;";

                ResultSet rs1 = stmt1.executeQuery(sql1);

                while (rs1.next()) {

                    String pmcId = rs1.getString("pmcId");
                    System.out.println("pmcid IS" + pmcId);
                    String sql2 = "SELECT * FROM Trait WHERE pmcId LIKE '" + pmcId + "';'";

                    ResultSet rs2 = stmt2.executeQuery(sql2);

                    while (rs2.next()) {

                        String traitName = rs2.getString("TraitName");// .replaceAll("\\(.*\\)",
                        String traitId = rs2.getString("TraitId"); // "");
                        String rowNumber = rs2.getString("rowNumber");
                        String tableId = rs2.getString("tableId");

                        System.out.println("\nTraitName: " + traitName + "\n");

                    }

                    System.out.println("pmcid IS" + pmcId);
                }
                
                stmt2.close();
                stmt1.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
