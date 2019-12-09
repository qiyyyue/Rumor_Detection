package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import dataClass.*;


public class FileProcess {
	static String txtRex = ".*[.]txt$";
	static String adnvRex = "^[a|d|n|v].*";
	static String punctuation = "。|，|、|；|：|:|？|！|《|》|‘|’|“|”";
	static String punc = "。|，|；|？|！";
	
	static String featurePath = "G:\\neural network\\data\\feature_words.txt";
	static String featureValue = "G:\\neural network\\data\\feature_pmiValue.txt";
	static String stopWrod_path  = "G:\\neural network\\data\\stopwords.txt";
	static String degreeWord_path = "G:\\neural network\\data\\degree_words.txt";
	
	static String[] down_words = {"黑","黑名单","恶心","超标","不合格","不符合","过期","曝","滥用","涉嫌"};
	static String[] up_words = {"合格","符合"};

	static List<Doc> documents = dataSet.getDocuments(); 			//文本集
	static Vocabulary vocabulary = dataSet.getVocabulary();			//词库
	Dictionary dic = dataSet.getDictionary();						//词典集
		
	
	public void load_dictionary(boolean isTrain){
		if(isTrain)		wordVector_DB.load_vecIndex();
        loadDictionary(true);	//正向情感词典        
        loadDictionary(false);	//负向情感词典
        load_stopWords();
        load_degreeWords();
       
		dic.twistDic.put("不是", "而是");
		dic.twistDic.put("虽然", "但|但是|可是");
		dic.twistDic.put("尽管", "但|但是|还是|");
		dic.twistDic.put("然而", "|");
	}
	
	//载入词典:正向情感词典（positive）、负向情感词典（negative）
	private void loadDictionary(boolean type){
		Map<String,Integer> dictionary = type?dic.origin_pDic:dic.origin_nDic;
		String file = type?"positive.txt":"negative.txt";
		String path = "G:\\neural network\\data\\"+file;		
		
        try {
            FileInputStream fstream = new FileInputStream(path);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader swFile = new BufferedReader(new InputStreamReader(in, "UTF-8"));

            String word = null;
            while ((word = swFile.readLine()) != null) { 
            	String temp = word.trim();
            	if(temp.length()>0){
            		dictionary.put(temp,0);             
            	}
            }
            swFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	 }
	
	//载入程度词典（含否定词）
	public void load_degreeWords(){
		File file = new File(degreeWord_path);	
		Reader reader = null;
		try{			
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			double weight = 0;
			while((line = br.readLine())!=null){
				String s = line.trim();
				if(s.length()<=0) continue;
				if(s.matches("\\d")){
					int degree = Integer.parseInt(s);					
					switch(degree){
					case 0:weight = -1;break;
					case 1:weight = 0.2;break;
					case 2:weight = 2.0;break;
					case 3:weight = 1.5;break;
					case 4:weight = 1.2;break;
					case 5:weight = 1.1;break;
					case 6:weight = 0.9;break;
					case 7:weight = 0.8;break;
					default:weight = 0;break;
					}
				}else if(weight==-1){
					dic.denyWord.add(s);						
				}else {
					dic.degreeW.put(s, weight);
				}											
			}							
			reader.close();						
		}catch(Exception e){
			e.printStackTrace();
		}		
	}		
	
	//载入停用词	
	public void load_stopWords(){
		File file = new File(stopWrod_path);		
		Reader reader = null;
		try{			
			reader = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader br = new BufferedReader(reader);
			String line = null;
			
			while((line = br.readLine())!=null&&(line.length()>0)){
				String s = line.trim();
				if(s.length()>0){											
					dic.stopWord.add(s);
				}				
			}
			reader.close();						
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
			
	public void load_testTxt(){
		WdProcess cp = new WdProcess();	
		FreqProcess fp = new FreqProcess();
		cp.get_TestTxt();
		fp.FrequencyCount();
	}
	
	public void load_trainTxts(){
		WdProcess cp = new WdProcess();	
		FreqProcess fp = new FreqProcess();
		cp.getCorpus();
		fp.FrequencyCount();
		fp.sort_Dictionary();
	}	
			
	//保存特征词
	public static void save_features( Map<String, Integer> words){
		StringBuffer fBubber = new StringBuffer();
		
		for(Map.Entry<String, Integer> entry:words.entrySet()){
			fBubber.append(entry.getValue()+":"+ entry.getKey() + "\r\n");			
		}
		WriteFile(new File(featurePath), fBubber.toString());
	}
	
	//载入特征词
	public static int load_features(){
		int num = 0;
		Map<String,Integer> result = dataSet.getFeature().feature;
		Reader reader = null;
		File file = new File(featurePath);
		try{
			if(file.isFile()&&file.exists()){
				reader = new InputStreamReader(new FileInputStream(file),"utf-8");
				BufferedReader br = new BufferedReader(reader);
				String line = null;
				while((line = br.readLine())!=null){					
					line.trim();
					String[]w = line.split(":");
					int value = Integer.parseInt(w[0]);
					int id = Math.abs(value);
					result.put(w[1], value);
					if(id>num){
						num=id;
					}				
				}
				reader.close();
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
		return num;
	}
			
	//文件写入
	public static void WriteFile(File file,String message){
		byte[] buffer = message.getBytes(); 
		try {
			file.createNewFile();
			FileOutputStream fout = new FileOutputStream(file);
			fout.write(buffer);
			fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	//文件内容添加
	public static void appendFile(String file,String message){
		if(message.length()<=0||file==null) return;
		
		try {
			FileWriter writer = new FileWriter(file, true);
			writer.write(message);
            writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
															
}

