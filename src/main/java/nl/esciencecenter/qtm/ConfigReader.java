package nl.esciencecenter.qtm;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

public class ConfigReader {

	private static String CFG_FILE = "src/main/resources/configQtm.properties";
	
	public static String getPropValue(String name) {
		String value = "";
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(CFG_FILE));
			value = props.getProperty(name);
		} catch (IOException e) {
			System.out.println(e);
		}
		return value;
	}
}