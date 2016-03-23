/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qtlTMdb;

import java.sql.*;

/**
 *
 * @author gurnoor
 */
public class qtlDB {
    
    public static Connection c;
    public static boolean connectionDB()
    {
        c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String sTempDb= "hello.db";
            String sJDBC= "jdbc:sqlite";
            String sDBUrl= sJDBC+":"+sTempDb;
                 
                       
            c = DriverManager.getConnection(sDBUrl);
         
        } catch ( Exception e ) {
         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        System.exit(0);
    }
    System.out.println("Opened database successfully");
    return true;
  }
    
    
   public static void createQTLtableAuto()
  {
      try{
            if(connectionDB()){
            Statement stmt = null;
            stmt = c.createStatement();
            stmt.setQueryTimeout(30);
            String sql = "CREATE TABLE IF NOT EXISTS QTLAuto " +
                   "(EntryID INTEGER PRIMARY KEY AUTOINCREMENT," +
                   " Trait TEXT NOT NULL, " + 
                   " ChromosomeNumber NUMBER NOT NULL, " + 
                   " ParentCrosses CHAR(50), " + 
                   " R2   REAL,"+
                   "LOD REAL,"+
                   "PercentVariation INT,"+
                    "LogValue Real"+
                    ")"; 
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            }
      }
      catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Table QTLAUTO created successfully");
  }  
    
   public static void createQTLtableManual()
  {
      try{
            if(connectionDB()){
            Statement stmt = null;
            stmt = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS QTLManual " +
                   "(EntryID INTEGER PRIMARY KEY AUTOINCREMENT," +
                   " Trait TEXT    NOT NULL, " + 
                   " ChromosomeNumber NUMBER     NOT NULL, " + 
                   " ParentCrosses        CHAR(50), " + 
                   " R2   REAL,"+
                   "LOD REAL,"+
                   "PercentVariation INT,"+
                    "LogValue Real"+
                    ")"; 
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            }
      }
      catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Table created successfully");
  }    
    
   
   public static void insertQTLtableManual()
  {
      try{
            if(connectionDB()){
            Statement stmt = null;
            stmt = c.createStatement();
            String sql = "INSERT INTO QTLManual" +
                   "('Fleshcolour',"+
                    "13,"+
                    "'S1*S2',"+
                    "0.33,"+
                    "0.76,"+
                    "53,"+
                    "0.51)"; 
            stmt.executeUpdate(sql);
            stmt.close();
            c.close();
            }
      }
      catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("entry inserted into QTL Manual successfully");
  }    
   
   
   
}
