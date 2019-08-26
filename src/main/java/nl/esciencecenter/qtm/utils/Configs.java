package nl.esciencecenter.qtm.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

public class Configs {

	public static Properties prop = new Properties();
	public static String configFileName;

	public static void setPropertyQTM(String title, String value) {
		try {
			FileInputStream in = new FileInputStream(
					configFileName);
			Properties props = new Properties();
			props.load(in);
			in.close();

			FileOutputStream out = new FileOutputStream(
					"config/configQtm.properties");
			props.setProperty(title, value);
			props.store(out, null);
			out.close();

		} catch (IOException io) {
			io.printStackTrace();
		} finally {

		}
	}

	public static String getPropertyQTM(String title) {
		String value = "";

		try {
			prop.load(new FileInputStream(configFileName));
			value = prop.getProperty(title);
		} catch (IOException e) {

		}
		return value;
	}



}
