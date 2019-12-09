package dataClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import word2vec.Node;

public class dataSet {
	
	private static List<Doc> documents = new LinkedList<>();					//文档链表
	private static List<int[]> doc_vector = new LinkedList<>();
	
	private static Map<String,Double> degreeWord = new HashMap<>();				//程度词典
	private static Feature feature = new Feature();				//特征
	
	private static List<String> stopword = new ArrayList<>();					//停用词表
	private static List<String> denyword = new ArrayList<>();
	
	private static Vocabulary voc = new Vocabulary(); 							//词库
	private static Dictionary dic = new Dictionary();							//词典
	
	private static Map<String, Integer> WF_Table = new HashMap<>();				//词频表
	private static Map<String, Set<Integer>> dcw = new HashMap<>();				//词语出现的文档数		
	
	private static Map<String, Node> haffNodeMap = new HashMap<>();
	
	private static double[][] phi;			//lda模型参数
	
	private static Map<String,Integer> wVec_id = new HashMap<>() ;
	
	
	public static void clear(){
		documents = new LinkedList<>();
		voc = new Vocabulary(); 	
		WF_Table = new HashMap<>();
		dcw = new HashMap<>();
		haffNodeMap = new HashMap<>();
	}
	
	
	public static Map<String, Integer> get_wordVec_id(){
		return wVec_id;
	}
	
	public static List<Doc> getDocuments(){
		return documents;
	}
	
	
	public static List<int[]> getDoc_Vector(){
		return doc_vector;
	}
	
	public static List<String> getStopWord(){
		return stopword;
	}
	
	public static List<String> getDenyWords(){
		return denyword;
	}
	
	public static Dictionary getDictionary() {
		return dic;
	}
	
	public static Feature getFeature(){
		return feature;
	}
	
	public static Map<String, Double> getDegreeWord(){
		return degreeWord;
	}
	
	public static double[][] get_LdaModle(){
		return phi;
	}
	
	public static void set_LdaModle(double[][] lda){
		phi = lda;
	}
	
	public static Vocabulary getVocabulary(){
		return voc;
	}
	
	public static Map<String, Integer> getWordFrequencyTable(){
		return WF_Table;
	}
	
	public static Map<String, Set<Integer>> get_DocCountsForWord(){
		return dcw;
	}
	
	public static Map<String, Node> getNodeMap(){
		return haffNodeMap;
	}
}
