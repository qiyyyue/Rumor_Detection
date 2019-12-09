package word2vec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dataClass.*;


public class Learn {
	
	public int EXP_TABLE_SIZE = 1000;
	private int MAX_EXP = 6;
	
	private Map<String, Node> nodeMap;  	 
	private int layerSize = 50;	 //训练多少个特征	
	private int window = 5;			//上下文窗口大小
	private int trainWordsCount = 0;
	//private int topicNum = 1;
	
	private double sample = 1e-3;
	private double alpha = 0.025;
	private double startingAlpha = alpha;
	
	private Boolean isCbow = false;
	
	private double[] expTable = new double[EXP_TABLE_SIZE];
	
	public Learn(){
		//topicNum = k;
		nodeMap = dataSet.getNodeMap();
		createExpTable();
	}
	
		
	private void trainModel(){
		List<Doc> docs = dataSet.getDocuments();
		for(Doc doc:docs){
			
			long nextRandom = 5;
		    int wordCount = 0;
		    int lastWordCount = 0;
		    int wordCountActual = 0;
		  
	        if (wordCount - lastWordCount > 10000) {	        	
	        	System.out.println("alpha:" + alpha + "\tProgress: "
	              + (int)(wordCountActual / (double)(trainWordsCount + 1) * 100) + "%");
	         
	        	wordCountActual += wordCount - lastWordCount;	         
	        	lastWordCount = wordCount;	         
	        	alpha = startingAlpha * (1 - wordCountActual/(double)(trainWordsCount + 1));
	         
	        	double temp = startingAlpha * 0.0001;
	        	if (alpha < temp) {alpha = temp;}	       
	        }
	       
	        Word[] words = doc.wordBag.toArray(new Word[doc.wordBag.size()]);	       	       
	        wordCount += words.length;		        	      
	        List<LeafNode> sentence = new ArrayList<LeafNode>();		             
	        for (int i = 0; i < words.length; i++) {		           	  
	        	//单词对应的哈弗曼树结点（含词向量）
	        	Node entry = nodeMap.get(words[i].content);    	   
	        	if (entry == null) continue;		          	          
	        	// randomly discards frequent words while keeping the ranking same
	        	//随机丢弃一些词(why?),形成上下文
	        	if (sample > 0) {
	        		double tempa = sample * trainWordsCount;	    		   
	        		double ran = (Math.sqrt(entry.freq/tempa) + 1)* (tempa)/entry.freq;           	    		   
	        		
	        		nextRandom = nextRandom * 25214903917L + 11;           	    
	        		double tempb = (nextRandom & 0xFFFF) / (double) 65536;
	        		if (ran < tempb) 	continue;   	              	   
	        	}	             	   
	        	sentence.add((LeafNode) entry);	      	      
	        }
        	       
	        for (int index = 0; index < sentence.size(); index++) {        
	    	   nextRandom = nextRandom * 25214903917L + 11;          
	    	   if (isCbow) {	         
	    		   cbowGram(index, sentence, (int) nextRandom % window);        
	    	   } else {	       
	    		   skipGram(index, sentence, (int) nextRandom % window);	       	    		   	    		   
	    	   }	       
	        }						        	        	      	    
		}
		System.out.println("Vocab size: " + nodeMap.size());	     
        System.out.println("Words in train file: " + trainWordsCount);	     
        System.out.println("sucess train over!");
	}
	
	//根据词库训练模型
	public void learn(){
		 
		readVocab();	   
		Haffman hm = new Haffman(layerSize);	   
		hm.make(nodeMap.values());				//生成哈夫曼树

	     // 查找并设置每个叶子结点的路径	    
		for (Node neuron : nodeMap.values()) {	    
			((LeafNode) neuron).make_HaffmanPath();	   
		}
		trainModel();	  
	} 	

	 //统计词频 	     
	 private void readVocab(){
		 Map<String,Integer> wordFreq = dataSet.getWordFrequencyTable();
		 for(Doc doc:dataSet.getDocuments()){
			 trainWordsCount += doc.wordBag.size();
		 }
	
		 for (Entry<String, Integer> element : wordFreq.entrySet()) {
			 double nomalize_freq = (double) element.getValue()/wordFreq.size();	//归一化词频
			 LeafNode wn = new LeafNode(element.getKey(),nomalize_freq, layerSize);			
			 nodeMap.put(element.getKey(), wn);     		
		 }	 
	 }
	
	 //模型训练
	 private void skipGram(int index, List<LeafNode> sentence, int seed) {
		 LeafNode word = sentence.get(index);		    
		 int a, c = 0;		   
		 for (a = seed; a < window * 2 + 1 - seed; a++) {	      
			 if (a == window) {continue;}
		      
			 c = index - window + a;		      			 
			 if (c < 0 || c >= sentence.size()) continue;		      
		     
			 double[] neu1e = new double[layerSize];// 误差项
		      
			 // HIERARCHICAL SOFTMAX		      
			 List<Node> neurons = word.nodes;		      
			 LeafNode we = sentence.get(c);
			 //double[] syn0 = we.topic_vec.get(topic);
			 
			 for (int i = 0; i < neurons.size(); i++) {		        
				 HiddenNode out = (HiddenNode) neurons.get(i);
				 //double[] syn1 = out.topic_vec.get(topic);
				 
				 double f = 0;
		        // Propagate hidden -> output			 
				 for (int j = 0; j < layerSize; j++) {		         
					 f += we.syn0[j] * out.syn1[j];
					 //f += syn0[j] * syn1[j];		
				 }
		        
				 if (f <= -MAX_EXP || f >= MAX_EXP) {		          
					 continue;		        
				 } else {		          
					 f = (f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2);		          
					 f = expTable[(int) f];		        
				 }
		        // 'g' is the gradient multiplied by the learning rate		        
				 double g = (1 - word.codeArr[i] - f) * alpha;
		        // Propagate errors output -> hidden	       
				 for (c = 0; c < layerSize; c++) {		          
					//neu1e[c] += g * syn1[c];
					 neu1e[c] += g * out.syn1[c];
				 }
		        // Learn weights hidden -> output		       
				 for (c = 0; c < layerSize; c++) {		         
					 //syn1[c] += g * syn0[c];
					 out.syn1[c] += g * we.syn0[c];
				 }
				 //out.topic_vec.put(topic, syn1);
			 }

		      // Learn weights input -> hidden
			 for (int j = 0; j < layerSize; j++) {		      
				 we.syn0[j] += neu1e[j];
				 //syn0[j] +=neu1e[j];
			 }
			 //we.topic_vec.put(topic, syn0);
		 }		  
	 }	 
	 private void cbowGram(int index, List<LeafNode> sentence, int seed) {
		   
		 LeafNode word = sentence.get(index);	
		 LeafNode last_word;
		 
		 int a, c = 0;
		 List<Node> neurons = word.nodes;
		 double[] neu1e = new double[layerSize];	// 误差项	   
		 double[] neu1 = new double[layerSize]; 	// 误差项？上下文向量累加和		    
		   
		 //求上下文向量累加和	
		 for (a = seed; a < window * 2 + 1 - seed; a++){	      			 
			 if (a != window) {	        
				 c = index - window + a;		       
				 if (c < 0||c >= sentence.size())	continue;		       
		       
				 last_word = sentence.get(c);		        
				 if (last_word == null)	continue;		       
				 for (c = 0; c < layerSize; c++)		          
					 neu1[c] += last_word.syn0[c];		      
			 }
		 }
	  
		 // HIERARCHICAL SOFTMAX	   
		 for (int d = 0; d < neurons.size(); d++) {	     
			 HiddenNode out = (HiddenNode) neurons.get(d);		      
			 double f = 0;
		      // Propagate hidden -> output		     
			 for (c = 0; c < layerSize; c++){
				 f += neu1[c] * out.syn1[c];		      
			 }
			 //近似计算，f的有效范围为[-MAX_EXP,MAX_EXP]
			 if (f <= -MAX_EXP||f >= MAX_EXP){		    	
				 continue;		      
			 }else{	    	  
				 f = expTable[(int) ((f + MAX_EXP) * (EXP_TABLE_SIZE / MAX_EXP / 2))];	      
			 }
			 
		      // 'g' is the gradient multiplied by the learning rate		      		      
		     double g = f * (1 - f) * (word.codeArr[d] - f) * alpha;		      
		     //		     
		     for (c = 0; c < layerSize; c++) {		        
		    	 neu1e[c] += g * out.syn1[c];		      
		     }		      
		     //更新隐含结点向量	      
		     for (c = 0; c < layerSize; c++) {		      
		    	 out.syn1[c] += g * neu1[c];		     
		     }		    
		 }
		 //更新词向量
		 for (a = seed; a < window * 2 + 1 - seed; a++) {		      		    	
			 if (a != window) {	       
				 c = index - window + a;		     
				 if (c < 0||c >= sentence.size())	continue;		       
		        		        
				 last_word = sentence.get(c);	        
				 if (last_word == null)	continue;		        
				 for (c = 0; c < layerSize; c++)	         
					 last_word.syn0[c] += neu1e[c];	      		      
			 }	    		    	   
		 }		  	 
	 }
	 
	 private void createExpTable() {
		 for (int i = 0; i < EXP_TABLE_SIZE; i++) {
		      double temp = Math.exp(((i / (double) EXP_TABLE_SIZE * 2 - 1) * MAX_EXP));
		      expTable[i] = temp / (temp + 1);		    
		 }	 
	 }
}

