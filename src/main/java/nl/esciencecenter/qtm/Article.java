package nl.esciencecenter.qtm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.commons.io.FileUtils;

public class Article {

	private String pmcId;

	public Article(String pmcId) {
		setPmcId(pmcId);
	}

	public void setPmcId(String pmcId) {
		this.pmcId = pmcId;
	}

	public String getPmcId() {
		return pmcId;
	}

	public String getFileName() {
		return pmcId + ".xml";
	}

	public void download() {
		URL url = null;
		String fileName = getFileName();
		String api = ConfigReader.getPropValue("webAPI");
		File file = new File(fileName);
		api = api.replace("${PMC_ID}", pmcId); // substitute placeholder
		
		try {
			url = new URL(api);
		} catch (MalformedURLException e) {
			System.out.println("The URL of the Web API seems invalid: " + api);
			System.out.println(e);
			System.exit(1);
		}

		try {
			if (!file.exists() || file.length() == 0) {
				System.out.format("Downloading article '%s' in XML...\t", pmcId);
				FileUtils.copyURLToFile(url, file);
				System.out.println("Done.");
			}
		} catch (IOException e) {
			System.out.println("Failed.");
			System.out.println(e);
			System.exit(1);
		}
	}
}