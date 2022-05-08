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
import java.util.Map;
import java.util.Map.Entry;

public class TFiDF {

	private Map<String, Map<String, Double>> documentsMap;
	private Map<String, Double> documentsSizes;
	
	public static void main(String[] args) {
		TFiDF test = new TFiDF();
		test.loadDirectory("src/main/resources/arquivostxt");
		test.calculateTFiDF();
		test.save("src/main/resources/result");
		System.out.println("Finish");
	}
	
	public TFiDF(){
		documentsMap = new HashMap<String, Map<String, Double>>();
		documentsSizes = new HashMap<String, Double>();
	}
	
	public void loadDirectory(String directoryName) {
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
	
	public void calculateTFiDF(){
		//System.out.println("Calculating TFIDF");
		for (Map.Entry<String, Map<String, Double>> document : documentsMap.entrySet()) {
	        String fileName = document.getKey();
	        for(Map.Entry<String, Double> docWord: document.getValue().entrySet()) {
	        	docWord.setValue(tfidf(fileName, docWord.getKey())); 
	        }
	    }
		//System.out.println("Finish TFIDF");
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
