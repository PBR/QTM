package solrTagger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import qtlTMdb.qtlDB;

public class AnnotateCellEntries {
	public static Connection c;

	public static boolean connectionDB() {
		c = null;
		try {
			Class.forName("org.sqlite.JDBC");

			String sJDBC = "jdbc:sqlite";
			String sTempDb = "TixNewDB.db";

			String sDBUrl = sJDBC + ":" + sTempDb;

			c = DriverManager.getConnection(sDBUrl);

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Opened database successfully \n");
		return true;
	}
	
	public static void AnnotateColumnHeadings(){
		try {
			
			if(connectionDB()){
				String sql1="Select cellValue,cellId from cellEntries where colId in (select colId from columnEntries where colType = 'QTL descriptor' OR colType= 'QTL property' ) AND cellType!= 'Empty'; ";
				String server="http://localhost:8983/solr";
				String core="terms";
				String match="LONGEST_DOMINANT_RIGHT";
				String type="dictionary";
				
				Statement stmt1 = null;
				Statement stmt2 = null;
				
				stmt1 = c.createStatement();
				stmt2 = c.createStatement();
				
				ResultSet rs1 = stmt1.executeQuery(sql1);
				
				while (rs1.next()) {
					
					String cellValue = rs1.getString("cellValue");//.replaceAll("\\(.*\\)", "");
					int cellid = rs1.getInt("cellId");
					System.out.println("cell Value is " + cellValue +"cellID is"+ cellid );
					String output= nl.erasmusmc.biosemantics.tagger.recognize.Evaluate2.processString(cellValue, core, match, type);
					
					if(output != null){
						String sql2 = "UPDATE cellEntries"
								+ " SET cellAnnotations='"+output+"' where cellId="+cellid+";";
						stmt2.executeUpdate(sql2);
					}
					
					
				}
				c.close();
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		
		
	}
	
}
