package br.com.classes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TFiDFMutex {

	private volatile Map<String, Map<String, Double>> documentsMap;
	private volatile Map<String, Double> documentsSizes;
	
	public static void main(String[] args) {
		TFiDFMutex t = new TFiDFMutex();
		t.loadDirectorySerial("src/main/resources/arquivostxt");
		t.calculateTFiDF();		
	}
	
	public TFiDFMutex(){
		this.documentsMap = new HashMap<String, Map<String, Double>>();
		this.documentsSizes = new HashMap<String, Double>();
	}
	
	public void loadDirectorySerial(String directoryName) {
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directoryName));
			for (Path file: stream) {
				Double docsize = 0.0;
				//System.out.println("Loading ... " + file.toString());
				
				//Loading words from file
				Map<String, Double> doc = new HashMap<String, Double>(); 
				String[] allWordsInDoc = Files.readString(file).split(" ");
				
				for(String word: allWordsInDoc) {
					String formattedWord = word.replaceAll("[\\s.,(–)]", "").toLowerCase();
					doc.compute(formattedWord, (key,val) -> (val == null)? val=1.0 : val+1 );
					docsize++;
				}
				documentsMap.put(file.getFileName().toString(),doc);
				documentsSizes.put(file.getFileName().toString(), docsize);
				//System.out.println("Success in loading " + file.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	public void loadWithThread() {
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/main/resources/arquivostxt"));
			int count = 0;
			
			Iterator<Path> iterator = stream.iterator();
		    while (iterator.hasNext()) {
		        Path file = iterator.next();
		        //Do stuff
		        Thread t = new loadFileThread(file, documentsMap, documentsSizes); 
				t.start();
				count++;
				if(count %8 == 0 ) {
					t.join();
				}
		     }
		    
		    while (documentsSizes.size() < count){
				//System.out.println("Waiting loading conclusion...");
				Thread.currentThread();
				Thread.sleep(600);
		}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}*/

	public void calculateTFiDF(){
		//System.out.println("Calculating TFIDF");
		for (Map.Entry<String, Map<String, Double>> document : documentsMap.entrySet()) {
	        Thread t = new TFiDFCalculator(document.getKey(), documentsMap, documentsSizes); 
			t.start();
	    }
		//System.out.println("Finish TFIDF");
	}
}
