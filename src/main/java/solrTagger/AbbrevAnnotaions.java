package solrTagger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.io.PrintWriter;

import tablInEx.Article;

public class AbbrevAnnotaions {

	public static void AbbreviationtoSolarSysnonyms(Article a) {
		HashMap<String, String> abbrevMap = a.getAbbreviations();
		Set<String> totalkeys = abbrevMap.keySet();
		Iterator<String> keys = totalkeys.iterator();
		
		String synonymfile="/var/solr/data/terms/conf/synonyms.txt";
		
		try{
		    PrintWriter writer = new PrintWriter(synonymfile, "UTF-8");
		    
		while (keys.hasNext()) {
			String key=keys.next();
			writer.println(abbrevMap.get(key)+" => "+key);
					
		
		}
		
		
		writer.println("α"+" => "+"alpha");
		writer.println("β"+" => "+"beta");
		writer.println("γ"+" => "+"gamma");
		
		
		
		
		writer.close();
		}catch(Exception e){
			
		}
		
	}
}
