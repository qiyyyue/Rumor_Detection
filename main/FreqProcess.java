package main;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import dataClass.Doc;
import dataClass.Word;
import dataClass.dataSet;

public class FreqProcess extends FileProcess {
	
	private int threshold = 40;						//情感词典种子个数
	Map<String, Integer> word_freq = dataSet.getWordFrequencyTable();		//词频表
	
	
	//计算词频——单词的语料词频、单词的文本出现频率、文本的单词频率
	//词典词频统计
	public void FrequencyCount() {
    	Map<String, Integer> docID = vocabulary.doc_id;
    	Map<String, Set<Integer>> dcw= dataSet.get_DocCountsForWord();
    	
        for (Doc doc: documents) {
        	int docId = docID.get(doc.docName);
        	Map<String, Integer> wCount = doc.wordCount;
        	
			for(Word word : doc.wordBag){
				String w = word.content; 
				
        		Integer wordFreq = word_freq.get(w);
        		word_freq.put(w, wordFreq != null ? ++wordFreq : 1); 	//语料内词频更新
        		
        		Integer wf = wCount.get(w);
        		wCount.put(w, (wf!=null)? ++wf : 1);						//文本内词频统计
        		       		
        		if(dcw.containsKey(w)){
        			dcw.get(w).add(docId);									//单词的文本出现频率统计      		
        		}else{
        			Set<Integer> set = new HashSet<>();
        			set.add(docId);
        			dcw.put(w, set);
        		}
        		
        		//正负向原始词典_词频统计
        		if(dic.origin_pDic.containsKey(w)){        					
        			dic.origin_pDic.put(w, dic.origin_pDic.get(w)+1); 
        		}
        		if(dic.origin_nDic.containsKey(w)){
        			dic.origin_nDic.put(w, dic.origin_nDic.get(w)+1);
        		}        		     
			}
        }
    }
	
	public void add_words(boolean upWords){
		List<String> lex = (upWords)?dic.positive:dic.negative;
		String[] words = (upWords)?up_words:down_words;
		//String[] words = down_words;
		Map<String, Boolean> seed_words = dataSet.getFeature().based_word;
		for(String word:words){
			if(lex.contains(word)) continue;
			lex.add(word);			
    		seed_words.put(word, upWords);
		}
	}
	//分别筛选前n个高频词作为种子词，加入正负向词典
	public void sort_Dictionary(){
		sort_Dictionary(true);
		sort_Dictionary(false);
		add_words(true);
		add_words(false);
	}	
	public void sort_Dictionary(boolean polar){
		Map<String, Boolean> seed_words = dataSet.getFeature().based_word;
		Map<String, Integer> lexicon = (polar)?dic.origin_pDic:dic.origin_nDic;
		
		valueComparator vc = new valueComparator(lexicon);
    	Map<String, Integer> so_lex = new TreeMap<String,Integer>(vc);
    	so_lex.putAll(lexicon);
    	Iterator<Map.Entry<String, Integer>> it = so_lex.entrySet().iterator();
    	List<String> lex = (polar)?dic.positive:dic.negative;  	
    	
    	for(int i=0;i<threshold;i++){
    		Map.Entry<String, Integer> entry = it.next();
    		lex.add(entry.getKey());
    		seed_words.put(entry.getKey(), polar);
    	} 	
	}

}


class valueComparator implements Comparator<String>{
	Map<String, Integer> base;
	public valueComparator(Map<String, Integer> base){
		this.base = base;
	}
	public int compare(String a, String b){	
		return (base.get(a) >= base.get(b))?-1:1;		
	}
}
