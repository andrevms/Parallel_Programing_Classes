package br.com.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class loadFileThread extends Thread{
	private Path file;
	private Map<String, Map<String, Double>> documentsMap;
	private Map<String, Double> documentsSizes;
	
	public loadFileThread(Path file, Map<String, Map<String, Double>> documentsMap, Map<String, Double> documentsSizes) {
		this.file = file;
		this.documentsMap = documentsMap;
		this.documentsSizes = documentsSizes;
	}
	
	public void run() {
		loadDirectory(file.toString());
	}
	
	private void loadDirectory(String directoryName) {
		try {
			Double docsize = 0.0;
			System.out.println("Loading ... " + file.toString());
				
			//Loading words from file
			Map<String, Double> doc = new HashMap<String, Double>(); 
			String[] allWordsInDoc = Files.readString(file).split(" ");
				
			for(String word: allWordsInDoc) {
				String formattedWord = word.replaceAll("[\\s.,(–)]", "").toLowerCase();
					
				doc.compute(formattedWord, (key,val) -> (val == null)? val=1.0 : val+1 );
				docsize++;
			}
				
				addDocInMap(file.getFileName().toString(), doc, docsize);
				
				System.out.println("Success in loading " + file.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void addDocInMap(String fileName, Map<String, Double> doc, Double docsize) {
		documentsMap.put(fileName,doc);
		documentsSizes.put(fileName,docsize);
	}
}
