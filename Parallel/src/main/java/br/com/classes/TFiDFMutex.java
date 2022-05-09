package br.com.classes;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TFiDFMutex implements Runnable{

	private volatile Map<String, Map<String, Double>> documentsMap;
	private volatile Map<String, Double> documentsSizes;
	
	public static void main(String[] args) {
		Runnable r = new TFiDFMutex();
		Thread t = new Thread(r); 
		t.start();
			
	}
	
	public TFiDFMutex(){
		this.documentsMap = new HashMap<String, Map<String, Double>>();
		this.documentsSizes = new HashMap<String, Double>();
	}
	
	@Override
	public void run() {
		loadWithThread();
		calculateTFiDF();
	}

	private void loadWithThread() {
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/main/resources/arquivostxt"));
			int count = 0;
			
			for (Path file: stream) {
				Thread t = new loadFileThread(file, documentsMap, documentsSizes); 
				t.start();
				count++;
			}
			
			while (documentsSizes.size() < count){
					System.out.println("Waiting loading conclusion...");
					Thread.currentThread();
					Thread.sleep(5000);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	}

	public void calculateTFiDF(){
		//System.out.println("Calculating TFIDF");
		for (Map.Entry<String, Map<String, Double>> document : documentsMap.entrySet()) {
	        Thread t = new TFiDFCalculator(document.getKey(), documentsMap, documentsSizes); 
			t.start();
	    }
		//System.out.println("Finish TFIDF");
	}
}
