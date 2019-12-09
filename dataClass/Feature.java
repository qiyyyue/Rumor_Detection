package dataClass;

import java.util.HashMap;
import java.util.Map;

public class Feature {
	public Map<String, Boolean> based_word = new HashMap<>();
	public Map<String, Integer> feature = new HashMap<>();
	 
//	public void add(String word,boolean polar){
//		if(feature.containsKey(word)) return;
//		seedWord.put(word, (polar)?count:(-count));
//		count++;
//	}
}
