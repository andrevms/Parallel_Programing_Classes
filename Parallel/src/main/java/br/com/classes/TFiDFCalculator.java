package br.com.classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class TFiDFCalculator extends Thread{
	
	private String file;
	private Map<String, Map<String, Double>> documentsMap;
	private Map<String, Double> documentsSizes;
	
	public TFiDFCalculator(String file, Map<String, Map<String, Double>> documentsMap, Map<String, Double> documentsSizes) {
		this.file = file;
		this.documentsMap = documentsMap;
		this.documentsSizes = documentsSizes;
	}
	
	public void run() {
		tfidf(file);
		//save(file);
	}
	
	private void tfidf(String fileName) {
		
		for(Map.Entry<String, Double> docWord: documentsMap.get(fileName).entrySet()) {
			Double aux = tfidf(fileName, docWord.getKey());
        	tfidfSetResult(aux, fileName, docWord.getKey()); 
        }
	}
	
	private synchronized void tfidfSetResult(Double value, String fileName, String docWord) {
		documentsMap.get(fileName).put(docWord, value);
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
