package br.com.classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TFiDFExecutor{

	private volatile Map<String, Map<String, Double>> documentsMap;
	private volatile Map<String, Double> documentsSizes;
	
	public static void main(String[] args) {
		System.out.println("Init loading ...\n");
		TFiDFExecutor t = new TFiDFExecutor();
		t.loadWithThread();
		System.out.println(" loading concluded...\n");
		System.out.println(" Init calculating TFiDF...\n");
		t.calculateTFiDF();
		System.out.println(" Calculating TFiDF concluded...");
		t.save("src/main/resources/result");
		System.out.println("Finish");
	}
	
	public TFiDFExecutor(){
		this.documentsMap = new HashMap<String, Map<String, Double>>();
		this.documentsSizes = new HashMap<String, Double>();
	}
	
	public void loadWithThread() {
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/main/resources/arquivostxt"));
			ExecutorService exec_service = Executors.newFixedThreadPool(4);
			stream.forEach(path -> exec_service.execute(new Runnable() {

				@Override
				public void run() {
					loadFile(path); 
				}}));
			
			exec_service.shutdown();
			exec_service.awaitTermination(60, TimeUnit.SECONDS);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void calculateTFiDF(){
		
			System.out.println("Calculating TFIDF");
			documentsMap.forEach( (nameFile, fileMap) -> {
				ExecutorService exec_service = Executors.newFixedThreadPool(4);
				fileMap.forEach( (word, valor) -> exec_service.execute(new Runnable() {

					@Override
					public void run() {
						tfidfSetResult(tfidf(nameFile, word),nameFile,word);
					}}));
				
				exec_service.shutdown();
				try {
					exec_service.awaitTermination(60, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("Finish TFIDF " + nameFile + "\n");
			});
	}
	
	private Double tfidf(String fileName, String word) {
		// TODO Auto-generated method stub
		return tf(fileName, word) * idf(fileName, word);
	}

	private double idf(String fileName, String word) {
		double count = 0;
		for (Map.Entry<String, Map<String, Double>> doc : documentsMap.entrySet()) {
			if (doc.getValue().containsKey(word)) {
				count++;
			}
		}
		return Math.log(Double.valueOf(documentsMap.size()) / count);
	}

	private double tf(String fileName, String word) {
		return documentsMap.get(fileName).get(word)/documentsSizes.get(fileName);
	}
	
	private synchronized void tfidfSetResult(Double value, String fileName, String docWord) {
		documentsMap.get(fileName).put(docWord, value);
	}

	private void loadFile(Path file) {
		try {
			Double docsize = 0.0;
			System.out.println("Loading ... " + file.toString());
				
			//Loading words from file
			Map<String, Double> doc = new HashMap<String, Double>(); 
			final String[] allWordsInDoc = Files.readString(file).split(" ");
				
			for(String word: allWordsInDoc) {
				final String formattedWord = word.replaceAll("[\\s.,(–)]", "").toLowerCase();
					
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

	private void saveFile(String fileName) {
		//System.out.println("Saving file " + fileName);
		File file = new File("src/main/resources/result/" + fileName + ".txt");
        BufferedWriter bf = null;
        try{
        	
            //create new BufferedWriter for the output file
            bf = new BufferedWriter( new FileWriter(file) );
 
            //Write fileName on beginning of file
            bf.write(fileName);
            bf.newLine();
            
            //iterate map entries
            for(Map.Entry<String, Double> docWord: documentsMap.get(fileName).entrySet()) {
            		//put key and value separated by a equal
                    bf.write( docWord.getKey() + " = " + docWord.getValue() );
                    bf.newLine();
			}
             
            bf.flush();
            
           // System.out.println("Save " + fileName +" Successfully");
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try{
                //always close the writer
                bf.close();
            }catch(Exception e){}
        }
}

	public void save(String path) {
	    //System.out.println("Saving files\n Beginning");
	    for(Entry<String, Map<String, Double>> doc : documentsMap.entrySet()){
	    	saveFile(doc.getKey());
	    }
	    //System.out.println("Finish");
	}
}
