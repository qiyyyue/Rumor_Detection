package pmi;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dataClass.Dictionary;
import dataClass.Doc;
import dataClass.Vocabulary;
import dataClass.Word;
import dataClass.dataSet;
import word2vec.WordSimilarity;


public class Pmi {
   	
	Map<String, Float> pos = new HashMap<>();
	Map<String, Float> neg = new HashMap<>();
	
	Map<String,Float> pos_WPF = new HashMap<>();		//正向共现情感词对
	Map<String,Float> neg_WPF = new HashMap<>();		//负向共现情感词对
	
	Map<String,distanceCounter> posDis = new HashMap<>();			//正向情感词距离计算
	Map<String,distanceCounter> negDis = new HashMap<>();			//负向情感词距离计算
		
    
    private double pmi_thresold = 2;									//新词加入词典的阈值
    double chi_thresold = 5*4E-5;									//卡方检验的阈值
    private int word_threshold = 10;								//词语阈值窗口
    private int windowSize = 12;									//共现窗口大小   
    private String validword_rex = "^[n|v|a|d|r].*";
    
    static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");	//时间戳（调试用）
        
    //计算情感词到词典中种子词的平均距离
    private double avgDistance(distanceCounter dc){
    	if(dc==null||dc.count==0||dc.sum==0) return 0;
    	if(dc.count==dc.sum) return 1;
    	//return (dc.count*1.0)/(dc.sum*1.0);
    	double avg_dis = (dc.sum*1.0)/(dc.count*1.0);
    	double log = Math.log(avg_dis)/Math.log(2.0);
    	return 1/log;
    }   
    
    //da_so_pmi主函数，返回情感特征词
    public int calc_soPMI(){
    	System.out.println("pmi start:"+formatter.format(new Date().getTime()));   	
		for (Doc doc : dataSet.getDocuments()) {
            calc_WPF(doc.wordBag, true);			//计算正向情感词语pair的共现频率 
            calc_WPF(doc.wordBag, false);			 //~~~~负向~~~~~~~~~~~~~~~~~~~~           
    	}		
		System.out.println("pmi over:"+formatter.format(new Date().getTime()));
    	return calc_dasoPmi();
    }      
    
    public int calc_dasoPmi(){

     	System.out.println("ds_pmi start:"+formatter.format(new Date().getTime()));
     	Map<String, Boolean> result = new HashMap<>();   	
    	Set<String> wordSet = new HashSet<>();
    	Vocabulary vb = dataSet.getVocabulary();
    	
		for(String word:vb.id_word){			
			if(wordSet.contains(word)) continue;			
			double pos_pmi = ds_Pmi(word,true);
			double neg_pmi = ds_Pmi(word,false);
			double daso_pmi = pos_pmi - neg_pmi;
			//vb.word_pmi.put(word, daso_pmi);
			//pmi值超过阈值，则作为新词添加入词典
			if(daso_pmi>pmi_thresold){				
				result.put(word, true);
			}else if(daso_pmi<(-pmi_thresold)){				
				result.put(word, false);
			}
			wordSet.add(word);   			
		}
		System.out.println("ds_pmi over:"+formatter.format(new Date().getTime())); 
		select(result);
		//return WordSimilarity.word_hanlde();
		return WordSimilarity.wordHanlde();				  	
    }
            
             
    //计算目标词与词典中词语的共现频率 & 统计词间距离
    public void calc_WPF(List<Word> wordList,boolean dic_polar) {
    	Dictionary dic = dataSet.getDictionary();
    	List<String> seWords = (dic_polar)?dic.positive:dic.negative;			//情感词（正向 or负向）
    	Map<String,Float> wpfTable = (dic_polar)?pos_WPF:neg_WPF;   			//共现词语总表
    	Map<String, distanceCounter> disMap = (dic_polar)?posDis:negDis;		//记录词间距离
    	    	    	   	
    	Word[] words = (Word[])wordList.toArray(new Word[wordList.size()]);
        int length = words.length;
        int i = 0,end = -1;      				// 共现窗口的开始与结束位
     
        Float pairFreq = 0.0f;
        String wordPair = "";
        HashMap<Integer, HashSet<String>> alreadyDone = new HashMap<>();		//记录已处理过的共现词语，防止共现窗口内重复处理

        for (i = 0; i < length - 1; i++) {
        	String wi = words[i].content;
        	String type = words[i].type;
        	if(!type.matches(validword_rex))	continue;
        	if(seWords.contains(wi))			continue;			//种子词
        											       	
            end = (length-i >= windowSize)?(i+windowSize-1):(length - 1);          
           
            if (!alreadyDone.containsKey(i)) {
                alreadyDone.put(i, new HashSet<String>());
            }

            // for each remaining words in the window
            for (int j = i + 1; j <= end; j++) {
            	String wj = words[j].content;
            	String wjt = words[j].type;
            	 //make sure that the word is valid in the target lexicon and do not form pair with itself
            	if(!wjt.matches(validword_rex)||!seWords.contains(wj)||wi.equals(wj)) {
            		continue;
            	}
        
                if (!alreadyDone.containsKey(j)) {
                    alreadyDone.put(j, new HashSet<String>());
                }
                
                // asurring this word is appearing first time in the current window
                boolean repeatWordj = alreadyDone.get(i).contains(wj);
                boolean repeatWordi = alreadyDone.get(j).contains(wi);
                if (repeatWordi||repeatWordj) 	continue;   
                
                //sort the word pair in the lexical order, and update word pair frequency
                wordPair = wi.compareTo(wj) <= 0 ? wi + " " + wj : wj + " " + wi;
                                             
                pairFreq = wpfTable.get(wordPair);                   
                wpfTable.put(wordPair, pairFreq != null ? ++pairFreq : 1);
                
                distanceCounter dc = disMap.get(wi);				//统计词间距离                   
                if(dc==null){
                	dc = new distanceCounter(j-i,1);
                	disMap.put(wi, dc);
                }else{
                	dc.sum +=j-i;
                	dc.count++;
                }                                      
                
                alreadyDone.get(j).add(wi);
                alreadyDone.get(i).add(wj);                
            }                
            alreadyDone.remove(i);			// we are done with the current word
        }              
    }  
              
    //加入了平均距离的情感pmi值计算
    private double ds_Pmi(String wName, boolean dic_polar){
    	if(wName == null||wName.equals("")) return 0;
    	Map<String, Integer> wfTable = dataSet.getWordFrequencyTable();	//词频表  	
    	Map<String,Float> wpfTable = (dic_polar)?pos_WPF:neg_WPF;		//正（负）向共现词语对
    	Map<String, distanceCounter> dc = (dic_polar)?posDis:negDis;	//词间距离
    	
    	int w1Freq = wfTable.get(wName);   	
    	if(w1Freq<2*word_threshold) return 0;
    	
    	int wordsNum = wfTable.size();    	
    	double sum = 0;
    	
    	for(Map.Entry<String, Float> entry:wpfTable.entrySet()){
    		String pairName = entry.getKey();
    		String[] word = pairName.split(" ");
    		if(!word[0].equals(wName)&&!word[1].equals(wName)) continue;   			
			float wpFreq = entry.getValue();
			if(wpFreq<0.5*word_threshold) continue;
				
			int w2Freq = wfTable.get(wName.equals(word[0])?word[1]:word[0]);
			double tempF = (wordsNum * 1.0)/(w1Freq*w2Freq);
			tempF *= wpFreq;
			
			if(tempF<1) continue;
			
			tempF = Math.log(tempF) / Math.log(2.0);	//pmi 
			sum += avgDistance(dc.get(wName))*tempF;    	   		
    	}
    	return sum;
    }               

    //特征筛选
  	private void select(Map<String, Boolean> words){
  		Map<String,Boolean> map = dataSet.getFeature().based_word;
  		for(Map.Entry<String, Boolean> entry:words.entrySet()){
  			String wName = entry.getKey();
  			boolean polar = entry.getValue();
  			double value = chi_square_test(wName,polar);
  			if(value>=chi_thresold){
  				map.put(wName,polar);
  			}
  		} 		
  	}
  	
  	//卡方检验
  	private static double chi_square_test(String word,boolean polar){
  		Map<String, Set<Integer>> dcw = dataSet.get_DocCountsForWord();
  		Vocabulary vb = dataSet.getVocabulary();
  		List<Doc> docList =  vb.id_doc;				
  		
  		int A_C = polar?vb.posDocs.size():vb.negDocs.size();
  		int B_D = polar?vb.negDocs.size():vb.negDocs.size();
  		int tag = polar?0:1;
  		int A = 0,B=0;
  		
  		Set<Integer> docs = dcw.get(word);
  		for(int doc_id:docs){						//统计词语在正负语料中各自出现的频数 			
  			Doc doc = docList.get(doc_id);
  			if(doc.tag == tag){
  				A++;
  			}else{
  				B++;
  			}			
  		}
  		int C = A_C - A;
  		int D = B_D - B;
  		long cs1 = (A_C+B_D)*Math.abs(A*D - B*C);
  		long temp1 = A_C*(A+B);
  		long temp2 = B_D*(C+D);
  		long cs2 = 2*temp1*temp2;
  		double chi_square = (cs1*1.0)/(cs2*1.0);
  		return chi_square;
  	}
}

class distanceCounter{
	long sum = 0;
	long count = 0;
	
	public distanceCounter(long sum,long count){
		this.sum = sum;
		this.count = count;
	}
}
