package dataClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Doc {
	public String docName;
	public String content;
	
	public int wordNum;
	public int topic;
	public int tag;
	public boolean isTest;
	
	public double[] vector;							//篇章级-特征向量
	public List<Word> wordBag;						//词袋
	public List<List<Word>> sentence;				//句子序列
	public Map<String, Integer> wordCount;			//文本内词频
	
	public Doc(){
		docName = new String();
		content = new String();
		wordBag = new ArrayList<>();
		wordCount = new HashMap<>();
		sentence = new ArrayList<>();
		topic = -1;
		tag = -1;
	}
	
	@Override
	public boolean equals(Object obj){
		Doc doc = (Doc) obj;
		if(!doc.docName.equals(this.docName))	return false;
		if(!doc.content.equals(this.content)) return false;
		return true;
	}
}
