package word2vec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import dataClass.dataSet;
import main.wordVector_DB;

public class WordSimilarity {

	static Map<String, Integer> vec_id = dataSet.get_wordVec_id();
	static Map<String, Node> wordMap = dataSet.getNodeMap();
	static int size = 50;
	static int topNSize = 40;
	static double threshold = 2.5;
	
	
	//合并特征词中的近义词（对比方法1）
	public static int wordHanlde(){
		List<String> wordList = new ArrayList<>();
		Map<String,Integer> map = dataSet.getFeature().feature;
		Map<String,Boolean> words = dataSet.getFeature().based_word;
		
		for(Map.Entry<String,Boolean> entry:words.entrySet()){
			String temp = entry.getKey();
			wordList.add(temp);
			map.put(temp,0);
		}		
		
		int index = 1;
		for(int i=0;i<wordList.size();i++){
			String word1 = wordList.get(i);
			if(map.get(word1)>0) continue;
							
			double[] vec1 = get_wordVector(word1);			
			int weight1 = (words.get(word1))?1:-1;
			boolean isConbine = false;

			for(int j=i+1;j<wordList.size();j++){
				String word2 = wordList.get(j);				
				double[] vec2 = get_wordVector(word2);
				int weight2 = (words.get(word1))?1:-1;
				double dist =distance(vec1, vec2);
				if(dist<threshold) continue;	//计算词向量的余弦距离						
				//距离大于阈值，表示两个词语相似，可合并为一个特征								
				isConbine = true;					
				int index2 = map.get(word2);
				if(map.get(word1)==0){
					int id = (index2>0)?index2:index;
					map.put(word1,weight1*id);
				}
				if(index2==0){						
					map.put(word2,weight2*index);
				}								
			}
			if(!isConbine){						//没有相似词，则将该词单独作为一个特征
				map.put(word1, weight1*index);
			}
			System.out.println(index);
			index++;
		}
		return index;
	}
	
	
	//不合并（对比方法2）
	public static int word_hanlde(){
		Map<String,Integer> map = dataSet.getFeature().feature;
		Map<String,Boolean> words = dataSet.getFeature().based_word;
								
		int index = 1;
		for(Map.Entry<String, Boolean> entry:words.entrySet()){
										
			String word = entry.getKey();			
			int weight = (entry.getValue())?1:-1;														
			
			map.put(word, weight*index);											
			System.out.println(index);
			index++;
		}
		return index;
	}
	

	//计算两个向量间的余弦距离（除去分母）
	public static double distance(double[] vec1,double[] vec2){
		if(vec1==null||vec2==null) return 0;
		
		double dist = 0;
		for(int i=0;i<vec2.length;i++){
			dist += vec1[i]*vec2[i];
		}
		return dist;
	}
	
	//获取词向量	 
	public static double[] getWordVector(String word) {
		LeafNode node = (LeafNode)wordMap.get(word);
		return (node==null)?null:node.syn0;
	}	
	public static double[] get_wordVector(String word){
		if(!vec_id.containsKey(word)) return null;
		int id = vec_id.get(word);
		double[] result = wordVector_DB.get_wordVector(id);
		return result;
	}

}
