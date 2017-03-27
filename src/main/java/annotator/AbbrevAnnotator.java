//package annotator;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Set;
//
//import org.json.simple.JSONObject;
//
//import tablInEx.Article;
//import tablInEx.C;
//import tablInEx.HC;
//import tablInEx.Table;
//
//public class AbbrevAnnotator {
//
//	public static void jsonEntry() {
//		/*
//		 * JSONObject j1 = new JSONObject(); j1.put("name", "foo");
//		 * j1.put("num", new Integer(100)); j1.put("balance", new
//		 * Double(1000.21)); j1.put("is_vip", new Boolean(true));
//		 * 
//		 * System.out.print(j1);
//		 */
//
//	}
//
//	public static Article AbbreviationAnnotator(Article a) {
//		// HashMap<String, String> abbrevMap=new HashMap<>();
//		HashMap<String, String> abbrevMap = a.getAbbreviations();
//
//		Table[] tables = a.getTables();
//		for (Table t : tables) {
//			
//			if(t.isaTraitTable()){	
//						
//			System.out.println(t.getTable_caption());
//
//			HC[][] header_cells = t.getTable_Headercells();
//
//			for (int i = 0; i < header_cells.length; i++) {
//				for (int j = 0; j < header_cells[i].length; j++) {
//
//					JSONObject Annotations = new JSONObject();
//					Annotations = header_cells[i][j].getAnnotations();
//					// System.out.println("\n\n&&\n"+header_cells[i][j].getHeadercell_value()+"\n\n");
//
//					Set<String> totalkeys = abbrevMap.keySet();
//					Iterator<String> keys = totalkeys.iterator();
//
//					while (keys.hasNext()) {
//						String key = (String) keys.next();
//
//						// System.out.println(abbrevMap.get(key));
//
//						try {
//							if (header_cells[i][j].getHeadercell_value().toLowerCase().matches(abbrevMap.get(key))) {
//								System.out.println(
//										"Abbreviation found in " + t.getTable_label() + t.getDocumentFileName());
//
//								Annotations.put(key, abbrevMap.get(key));
//
//								header_cells[i][j].setAbbreviated_value(abbrevMap.get(key));
//
//								header_cells[i][j].setHeadercell_values(key);
//
//								// header_cells[i][j].set_spAnnotaion = key;
//							}
//						} catch (Exception e) {
//
//						}
//
//					}
//
//					header_cells[i][j].setAnnotations(Annotations);
//
//				}
//			}
//			t.setTableHeadercells(header_cells);
//
//			C[][] cells = t.getTable_cells();
//
//			for (int i = 0; i < cells.length; i++) {
//				for (int j = 0; j < cells[i].length; j++) {
//
//					JSONObject Annotations = new JSONObject();
//					Annotations = cells[i][j].getAnnotations();
//					//System.out.println("\n\n&&\n"+cells[i][j].getcell_value()+"\n\n");
//
//					Set<String> totalkeys = abbrevMap.keySet();
//					Iterator<String> keys = totalkeys.iterator();
//
//					while (keys.hasNext()) {
//						String key = (String) keys.next();
//
//						//System.out.println(abbrevMap.get(key));
//
//						try {
//							if (cells[i][j].getcell_value().toLowerCase().matches(abbrevMap.get(key)) || cells[i][j].getcell_value().matches(abbrevMap.get(key))) {
//								System.out.println(
//										"Abbreviation found in " + t.getTable_label() + t.getDocumentFileName());
//
//								Annotations.put(key, abbrevMap.get(key));
//
//								cells[i][j].setAbbreviated_value(abbrevMap.get(key));
//
//								cells[i][j].setcell_values(key);
//
//								// header_cells[i][j].set_spAnnotaion = key;
//							}
//						} catch (Exception e) {
//
//						}
//
//					}
//
//					cells[i][j].setAnnotations(Annotations);
//
//				}
//			}
//			t.setTable_cells(cells);
//			}
//		}
//		return a;
//	}
//
//}
