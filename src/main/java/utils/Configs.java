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
        
        
    
 
             
      
        
    public static String getPropertyQTM(String title){
        String value="";
        
        try{
            prop.load(new FileInputStream("config/configQtm.properties"));
            value=prop.getProperty(title);
        }catch (IOException e){
            
        }
        return value;
    }
    

}
