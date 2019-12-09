package dataClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vocabulary {
	public Map<String, Integer> doc_id = new HashMap<>();	//文档-数值 映射
	public Map<String, Integer> word_id = new HashMap<>();	//词-数值映射
	
	public Map<String, Double> word_pmi = new HashMap<>();
	
	public List<Integer> posDocs = new ArrayList<>();		//正向语料
	public List<Integer> negDocs = new ArrayList<>();		//负向语料
	
	public List<String> id_word = new ArrayList<>();
	public List<Doc> id_doc = new ArrayList<>();
	
	public int wordSize = 0;
	public int docSize = 0;
	
    
    public void addWord(String word){     	
    	if(word_id.containsKey(word)) return;    	  	
		synchronized (this) {   		 
			if (!word_id.containsKey(word)){	 	//发现新词,插入词库	         	  			            			
				word_id.put(word, wordSize);	              			 
				id_word.add(word);               			 
				wordSize++;    	           		
			}    		
		}         	 
    }
    
    public void addDoc(Doc doc){ 
    	String docName = doc.docName;
    	if (doc_id.containsKey(docName)) return;   	
    	synchronized (this) { 	       
			if (!doc_id.containsKey(docName)){	 //插入一条文档记录	        	  				            
				doc_id.put(docName, docSize);	           
				id_doc.add(doc);
				if(doc.tag == 0){
					posDocs.add(docSize);
				}else{
					negDocs.add(docSize);
				}
				docSize++;	        
			}
		}   	
    }
    
}
