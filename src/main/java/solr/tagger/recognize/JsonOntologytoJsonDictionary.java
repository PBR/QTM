package solr.tagger.recognize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonOntologytoJsonDictionary {
		public static void main(String [] args){
			try{
			String OntologyFile="/home/gurnoor/workspace/Wageningen/data/CO_330.json"; 
			File filecsv = new File("/home/gurnoor/workspace/Wageningen/data/TraitDictionary /CO_330.csv");
			if (!filecsv.exists()) {
				filecsv.createNewFile();
			}

			FileWriter fwCsv = new FileWriter(filecsv.getAbsoluteFile());
			BufferedWriter bwCsv = new BufferedWriter(fwCsv);
			
			
				JsonObject newDic=new JsonObject();
			for ( Entry<String, JsonElement> entry : new JsonParser().parse(new FileReader(OntologyFile)).getAsJsonObject().entrySet() ){
				//System.out.println("Entry is "+ entry.getKey());
				JsonObject subEntry=entry.getValue().getAsJsonObject();
				JsonObject subEntryName=subEntry.getAsJsonObject("name");
				JsonObject subEntryAbbrev=subEntry.getAsJsonObject("abbreviation");
				
				JsonObject add=new JsonObject();
				add.addProperty("name", subEntryName.get("english").getAsString() );
				add.addProperty("abbreviation", subEntryAbbrev.get("english").getAsString());
				//System.out.println(subsubEntry.toString());
				//JsonObject subsubsubEntry=subsubEntry.getAsJsonObject("english");
				
				//System.out.println(subsubEntry.get("english"));
				newDic.add(entry.getKey(),add);
				bwCsv.write(entry.getKey()+","+ subEntryName.get("english").getAsString()+","+subEntryAbbrev.get("english").getAsString()+"\n");
				
			}
			System.out.println(newDic.toString());
			
			File file = new File("/home/gurnoor/workspace/Wageningen/data/TraitDictionary /CO_330.json");
			
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(newDic.toString());
			bw.close();

			bwCsv.close();
			}catch(Exception e){
				e.printStackTrace();
				e.getMessage();
			}
		}			
}	
