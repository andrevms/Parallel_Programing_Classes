package br.com.classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TFiDFCallableAndFuture {

	private volatile Map<String, Map<String, Double>> documentsMap;
	private volatile Map<String, Double> documentsSizes;
	
	public static void main(String[] args) {
		System.out.println("Init loading ...\n");
		TFiDFCallableAndFuture t = new TFiDFCallableAndFuture();
		t.loadWithThread();
		System.out.println(" loading concluded...\n");
		System.out.println(" Init calculating TFiDF...\n");
		t.calculateTFiDF();
		System.out.println(" Calculating TFiDF concluded...");
		System.out.println("Finish");
	}
	
	public TFiDFCallableAndFuture(){
		this.documentsMap = new HashMap<String, Map<String, Double>>();
		this.documentsSizes = new HashMap<String, Double>();
	}
	
	public void loadWithThread() {
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("src/main/resources/arquivostxt"));
			ExecutorService exec_service = Executors.newFixedThreadPool(4);

			List<Callable<SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>>>> callables = new ArrayList<>(); 
			stream.forEach(path -> callables.add(new Callable<SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>>>() {

				@Override
				public SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>> call() throws Exception {
					return new java.util.AbstractMap.
							SimpleEntry<String,SimpleEntry<Double,Map<String, Double>>>
							(path.getFileName().toString(), loadFile(path));
				}}));
			
			List<Future<SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>>>> futures = exec_service.invokeAll(callables);
			
			for(Future<SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>>> future : futures) {
				addDocInMap(future);
				addDocsizeInMap(future);
			}
			
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

	private synchronized void addDocsizeInMap(Future<SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>>> future) {
		try {
			//add fileName and docsize
			documentsSizes.put(future.get().getKey(),future.get().getValue().getKey());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void addDocInMap(Future<SimpleEntry<String, SimpleEntry<Double, Map<String, Double>>>> future) {
		try {
			//add fileName and doc
			documentsMap.put(future.get().getKey(), future.get().getValue().getValue());
			//add fileName and docsize
			documentsSizes.put(future.get().getKey(),future.get().getValue().getKey());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void addDocInMap(String fileName, Map<String, Double> doc, Double docsize) {
		documentsMap.put(fileName,doc);
		documentsSizes.put(fileName,docsize);
	}

	public void calculateTFiDF(){
		
			System.out.println("Calculating TFIDF");
			documentsMap.forEach( (nameFile, fileMap) -> {
				List<Callable<SimpleEntry<String,Double>>> callables = new ArrayList<>();
				ExecutorService exec_service = Executors.newFixedThreadPool(4);
				fileMap.forEach( (word, valor) -> callables.add(new Callable<SimpleEntry<String,Double>>() {

					@Override
					public SimpleEntry<String,Double> call() {
						return new java.util.AbstractMap.SimpleEntry<String,Double>(word, tfidf(nameFile, word));
					}}));
				
				try {
					List<Future<SimpleEntry<String,Double>>> futures = exec_service.invokeAll(callables);
					for(Future<SimpleEntry<String,Double>> future : futures) {
						tfidfSetResult(future.get().getValue(), nameFile, future.get().getKey());
					}
				} catch (InterruptedException | ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				exec_service.shutdown();
				try {
					exec_service.awaitTermination(60, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Finish TFIDF " + nameFile);
				save(nameFile);
			});
	}
	
	public Double tfidf(String fileName, String word) {
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
			
	private  SimpleEntry<Double, Map<String, Double>> loadFile(Path file) {
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
			
			System.out.println("Success in loading " + file.toString());
			return new java.util.AbstractMap.SimpleEntry<Double, Map<String, Double>>(docsize, doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private synchronized void save(String fileName) {
		//System.out.println("Saving file " + fileName);
		File file = new File("src/main/resources/result/" + fileName);
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
}
