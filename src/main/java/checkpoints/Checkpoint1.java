package checkpoints;

import java.io.File;

public class Checkpoint1 {
	
	public static void readCheckpointfile(){
		File folder = new File("/home/gurnoor/Documents/Text Corpus/GS");
		File[] listOfFiles = folder.listFiles();

	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	        System.out.println("File " + listOfFiles[i].getName());
	      } else if (listOfFiles[i].isDirectory()) {
	        System.out.println("Directory " + listOfFiles[i].getName());
	      }
	    }
	}
	
}
