package utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Configs {

    public static Properties prop=new Properties();

    public static void setPropertyQTM(String title, String value){
        Properties prop = new Properties();
        OutputStream confOutputStream = null;

        try {

                confOutputStream = new FileOutputStream("config/configQtm.properties");

                // set the properties value
                prop.setProperty(title, value);
               
                // save properties to project root folder
                prop.store(confOutputStream, null);

        } catch (IOException io) {
                io.printStackTrace();
        } finally {
                if (confOutputStream != null) {
                        try {
                                confOutputStream.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }

        }
  }
        
        
    public static void setPropertySolr(String title, String value){
        Properties prop = new Properties();
        OutputStream confOutputStream = null;

        try {

                confOutputStream = new FileOutputStream("config/configSolr.properties");

                // set the properties value
                prop.setProperty("core9", "value9");
                prop.setProperty("core10", "value10");
                prop.setProperty("core11", "value11");

                // save properties to project root folder
                prop.store(confOutputStream, null);

        } catch (IOException io) {
                io.printStackTrace();
        } finally {
                if (confOutputStream != null) {
                        try {
                                confOutputStream.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }

        }
  }
        
    public static void setPropertyDb(String title, String value){
        Properties prop = new Properties();
        OutputStream confOutputStream = null;

        try {

                confOutputStream = new FileOutputStream("config/configDb.properties");

                // set the properties value
                prop.setProperty("core9", "value9");
                prop.setProperty("core10", "value10");
                prop.setProperty("core11", "value11");

                // save properties to project root folder
                prop.store(confOutputStream, null);

        } catch (IOException io) {
                io.printStackTrace();
        } finally {
                if (confOutputStream != null) {
                        try {
                                confOutputStream.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }

        }
  }
             
      
        
    public static String getPropertyQTM(String title){
        String value="";
        
        try{
            
            
            prop.load(new FileInputStream("config/configQtm.properties"));;
            value=prop.getProperty(title);
        }catch (IOException e){
            
        }
        return value;
    }
    
    public static String getPropertyDb(String title){
        Properties prop = new Properties();
        InputStream input = null;

        try {

                input = new FileInputStream("config/configDb.properties");

                // load a properties file
                prop.load(input);

                // get the property value and print it out
                //System.out.println(prop.getProperty(title));
                
        } catch (IOException ex) {
                ex.printStackTrace();
        } finally {
                if (input != null) {
                        try {
                                input.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                
        }
        return prop.getProperty(title);

  }
    
    
    
    public static String getPropertySolr(String title){
        String value="";
        
        try{
            
            
            prop.load(new FileInputStream("config/configSolr.properties"));;
            value=prop.getProperty(title);
        }catch (IOException e){
            
        }
        return value;
    }

}
