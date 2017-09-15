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
        try {
            FileInputStream in = new FileInputStream("config/configQtm.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream("config/configQtm.properties");
            props.setProperty(title, value);
            props.store(out, null);
            out.close();
            
        
       } catch (IOException io) {
                io.printStackTrace();
        } finally {
  
        }
  }
        
        
    public static void setPropertySolr(String title, String value){
        try {
            FileInputStream in = new FileInputStream("config/configSolr.properties");
            Properties props = new Properties();
            props.load(in);
            in.close();

            FileOutputStream out = new FileOutputStream("config/configSolr.properties");
            props.setProperty(title, value);
            props.store(out, null);
            out.close();

        } catch (IOException io) {
                io.printStackTrace();
        } finally {

        }
  }
        
    public static void setPropertyDb(String title, String value){
        try {
        FileInputStream in = new FileInputStream("config/configDb.properties");
        Properties props = new Properties();
        props.load(in);
        in.close();

        FileOutputStream out = new FileOutputStream("config/configDb.properties");
        props.setProperty(title, value);
        props.store(out, null);
        out.close();
        } catch (IOException io) {
                io.printStackTrace();
        } finally {

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
