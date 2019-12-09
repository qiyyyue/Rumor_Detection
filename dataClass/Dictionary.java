package dataClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {
	public Map<String,Integer> origin_pDic = new HashMap<>();		//原始正向词典
	public Map<String,Integer> origin_nDic = new HashMap<>();		//原始反向词典
	
	public List<String> positive = new ArrayList<>();		//正向词典
	public List<String> negative = new ArrayList<>();		//反向词典
	
	public List<String> stopWord = new ArrayList<>();		//停用词典
	public List<String> denyWord = new ArrayList<>();		//否定词
	
	public Map<String, String> twistDic = new HashMap<>();			//转折词典
	public Map<String, Double> degreeW =  new HashMap<>();			//程度词典
}
