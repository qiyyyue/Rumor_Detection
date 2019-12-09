package pmi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import dataClass.Doc;
import dataClass.Word;
import dataClass.dataSet;


public class Tf_Idf {
	private static double thresold = 5;	
	
	//根据tf-idf，为句子向量设值
	public static Map<Doc,List<double[]>> set_feature(int vec_size){
		
		List<Doc> documents = dataSet.getDocuments();
		Map<Doc,List<double[]>> vec_result = new HashMap<>();		
		Map<String, Integer> feature = dataSet.getFeature().feature;	//特征值		
		
		for(Doc doc:documents){
			List<double[]> sentences = new ArrayList<>();
			List<String> repeat = new ArrayList<>();
			for(List<Word> words:doc.sentence){
				//遍历每个文本中的句子
				if(words.size()<=0) continue;
				boolean hasFeature = false;
				double[] word_vec = new double[vec_size];
				for(Word w:words){
					String wName = w.content;
					if(!feature.containsKey(wName))		continue;
					if(repeat.contains(wName)) 		continue;
					
					double Tf_Idf = calc_TF_IDF(wName, doc);
					if(Tf_Idf<=thresold)			continue;
					hasFeature = true;
					
					//double tfidf_value = weightMix(w.weight)*Tf_Idf;					
					double tfidf_value = w.weight*Tf_Idf;	
					int id=feature.get(wName);
					int polar = (id>0)?1:-1;
					word_vec[polar*id-1] += polar*tfidf_value;
					repeat.add(wName);					
				}
				if(hasFeature){
					sentences.add(word_vec);				
				}
			}						
			vec_result.put(doc, sentences);
		}		
		return vec_result;
	}

	
	private static double calc_TF_IDF(String word,Doc doc){
		Map<String, Set<Integer>> dcw = dataSet.get_DocCountsForWord();	
		Map<String, Integer> wordTF= doc.wordCount;
		//统计单文本词频；
		int TFw = wordTF.get(word);
		int Dw = dcw.get(word).size();		//出现了单词word的文本数
		int D = dataSet.getDocuments().size();
		double temp = (D*1.0)/(Dw*1.0);
		double result =TFw*Math.log(temp)/Math.log(2.0);
		return result;
	}
	
	public Map<String,Double> extract_feature(){		
							
		List<Doc> documents = dataSet.getDocuments();
		Map<String,Double> repeat = new HashMap<>();
		for(Doc doc:documents){
			for(Word word:doc.wordBag){
				//遍历每个文本中的句子									
				String wName = word.content;					
				if(repeat.containsKey(wName)) 		continue;
				
				double Tf_Idf = calc_TF_IDF(wName, doc);
				if(Tf_Idf<=thresold)			continue;																														
				
				repeat.put(wName,Tf_Idf);											
			}
		}
		
		valueComparator vc = new valueComparator(repeat);
    	Map<String, Double> all_words = new TreeMap<String,Double>(vc);
    	all_words.putAll(repeat);
    	
    	Iterator<Map.Entry<String, Double>> it = all_words.entrySet().iterator();
    	Map<String,Double> result =  new HashMap<>(); 	
    	
    	for(int i=0;i<50;i++){
    		Map.Entry<String, Double> entry = it.next();
    		result.put(entry.getKey(),entry.getValue());
    	} 	
		return result;
	}
	
		
}

class valueComparator implements Comparator<String>{
	Map<String, Double> base;
	public valueComparator(Map<String, Double> base){
		this.base = base;
	}
	public int compare(String a, String b){	
		return (base.get(a) >= base.get(b))?-1:1;		
	}
}
