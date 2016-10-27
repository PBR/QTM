package Annotator;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.omg.CosNaming.BindingIteratorOperations;

import Annotator.BiostarTO_Ontology.Term;

import org.json.simple.JSONObject;

import tablInEx.Article;
import tablInEx.C;
import tablInEx.HC;
import tablInEx.Table;

public class OntologybasedAnnotator {

	public static void jsonEntry() {
		/*
		 * JSONObject j1 = new JSONObject(); j1.put("name", "foo");
		 * j1.put("num", new Integer(100)); j1.put("balance", new
		 * Double(1000.21)); j1.put("is_vip", new Boolean(true));
		 * 
		 * System.out.print(j1);
		 */

	}

	public static Article OA(Article a) {
		// Trait Ontology

		for (Table t : a.getTables()) {

			if (t.isaTraitTable()) {
				
				System.out.println(t.getTable_caption());

				BiostarTO_Ontology TO = new BiostarTO_Ontology();

				try {
					TO.parse();

				} catch (Exception e) {
					System.out.println("TO not found");
				}

				// Solanacae Phenotypic Ontology
				JSONParser parser = new JSONParser();
				Object obj = new Object();
				try {
					obj = parser.parse(new FileReader("/home/gurnoor/workspace/XMLTAB/Dictionaries/spDictionary.json"));

				} catch (Exception e) {
					System.out.println("SP dictionary not found");
				}

				JSONObject json = (org.json.simple.JSONObject) obj;

				HC[][] header_cells = t.getTable_Headercells();

				for (int i = 0; i < header_cells.length; i++) {
					for (int j = 0; j < header_cells[i].length; j++) {

						JSONObject Annotations = header_cells[i][j].getAnnotations();

						for (Term term : TO.id2term.values()) {
							//System.out.println(header_cells[i][j].getHeadercell_value() + ":" + t.name);
							try {
								if (header_cells[i][j].getHeadercell_value().toLowerCase().matches(term.name)) {
									System.out.println(
											"TO annotations found in " + t.getTable_label() + t.getDocumentFileName());
									Annotations.put("TO", term.id);
									// header_cells[i][j].set_spAnnotaion = key;
								}
							} catch (Exception e) {

							}
						}

						Set<String> totalkeys = json.keySet();
						Iterator<String> keys = totalkeys.iterator();

						while (keys.hasNext()) {
							String key = (String) keys.next();
							try {
								if (header_cells[i][j].getHeadercell_value().toLowerCase()
										.matches(json.get(key).toString())) {
									System.out.println(
											"SP annotations found in " + t.getTable_label() + t.getDocumentFileName());
									Annotations.put("SP", key);
									// header_cells[i][j].set_spAnnotaion = key;
								}
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
						header_cells[i][j].setAnnotations(Annotations);

					}
				}

				C[][] cells = t.getTable_cells();

				for (int i = 0; i < cells.length; i++) {
					for (int j = 0; j < cells[i].length; j++) {

						JSONObject Annotations = cells[i][j].getAnnotations();
						if( !(cells[i][j].getCell_type().equals("Numeric"))  ){
						
							for (Term term : TO.id2term.values()) {
							try {
								//System.out.println(cells[i][j].getcell_value()+"::"+t.name);
								if (cells[i][j].getcell_value().matches(term.name)) {
									System.out.println(
											"TO annotations"+ term.id +"found in " + t.getTable_label() + t.getDocumentFileName());
									Annotations.put("TO", term.id);

								}
							} catch (Exception e) {

							}

						}

						Set<String> totalkeys = json.keySet();
						Iterator<String> keys = totalkeys.iterator();

						while (keys.hasNext()) {
							String key = (String) keys.next();
							try {
								if (cells[i][j].getcell_value().matches(json.get(key).toString())) {
									System.out.println(
											"SP annotations "+ key +" found in " + t.getTable_label() + t.getDocumentFileName());
									Annotations.put("SP", key);
									// header_cells[i][j].set_spAnnotaion = key;
									}
								}catch(Exception e){
									
								}
						}
						cells[i][j].setAnnotations(Annotations);

					}
					}		
				}
			}
		}
		return a;
	}

}
