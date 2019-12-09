package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dataClass.Dictionary;
import dataClass.Doc;
import dataClass.Word;
import dataClass.dataSet;
import dataClass.SyntaxIndex;
import nlpir.NlpirMethod;


public class WdProcess extends FileProcess {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static String driver = "com.mysql.jdbc.Driver";     			 // 驱动程序名
    static String url = "jdbc:mysql://127.0.0.1:3306/test";     	 // URL指向要访问的数据库名scutcs
    static String user = "root",password = "123456";			// MySQL配置时的用户名和密码 
	
	public WdProcess(){}
	
	public void getCorpus(){
		getTxts(false);
	}
	
	public void get_TestTxt(){
		getTxts(true);
	}
	
	public void getTxts(boolean isTest){
		System.out.println("Txts loading start:"+formatter.format(new Date().getTime()));		
		String limit = isTest?" limit 0,500":"";
	
        try {			
        	Class.forName(driver);														// 加载驱动程序
			Connection conn = DriverManager.getConnection(url, user, password);		  // 连接数据库
		 
			if(!conn.isClosed()){
            	System.out.println("Succeeded connecting to the Database!");
            }
			
			Statement statement = conn.createStatement();          	            
			String sql = "select * from food_corpus" + limit;			 	// 要执行的SQL语句			
			ResultSet rs = statement.executeQuery(sql);						// 结果集
			
            while (rs.next()){
            	Doc doc = new Doc();
            	String content = rs.getString("body");           	
            	doc.wordNum = doc.wordBag.size();
            	doc.content = content;
            	doc.docName = rs.getString("title");
            	
            	//getWord("食品添加剂被检出不合格", doc, 1);
            	getWord(content, doc, 1);
            	getWord(doc.docName, doc, 3);
            	doc.isTest = isTest; 
            	if(!isTest)
            		doc.tag = Integer.parseInt(rs.getString("type"));
            	          	               
            	documents.add(doc);
				vocabulary.addDoc(doc);
            }
            rs.close();
            conn.close();
            System.out.println("Txts loading finish:"+formatter.format(new Date().getTime()));
		} catch (ClassNotFoundException e) {			
			e.printStackTrace();
		}catch (SQLException e) {			
			e.printStackTrace();
		}
       
    }
	
	//词语提取	
	public void getWord(String paragraph, Doc doc, int isTitle){
		
		Dictionary dic = dataSet.getDictionary();
		SyntaxIndex si = new SyntaxIndex();									//语法标记（转折、否定、程度修饰）
		String ps = NlpirMethod.NLPIR_ParagraphProcess(paragraph, 1);		//分词
		String[] words = ps.split(" ");										
		List<List<Word>> sen = doc.sentence;
		List<Word> tempList = new ArrayList<>();
		
		for(int i=0;i<words.length;i++){			
			if(words[i].length()==0)	continue;
			
			boolean filter = false;
			String[] word = words[i].split("/");
			String[] next = (i+1<words.length)?words[i+1].split("/"):new String[2];
			
			if(word.length!=2) continue;
			String content = new String(word[0]);
			String type = word[1];
			if(content.matches(punctuation)){si.reflush();}			//标点符号，刷新标记，更新句子									
			
			si.twistHandle(content, dic.twistDic);						//转折句处理
			
			if(dic.stopWord.contains(content))	{						//停用词滤除
				if(content.matches(punc)&&tempList.size()>0){
					doc.wordBag.addAll(tempList);
					sen.add(tempList);
					tempList = new ArrayList<>();
				}
				continue; 				
			}
			//过滤
			if(content.matches("真|真的")&&(next.length>1)){		//副词：真、真的
				if(next[1].matches("^[a|v].*")){
					si.degree += 1.1;
					filter = true;
				}else if(next[0].equals("的")){
					i++;
					si.degree += 1.1;
					continue;
				}
			}
			if(dic.degreeW.containsKey(content)){								//程度词处理								
				si.degree += dic.degreeW.get(content);
				filter = true;
			}
			
			if(dic.denyWord.contains(content)){									//否定词处理（待改）					
				si.deny = true;
				filter = true;				
			}
			
			if(!filter){																					
				Word w = new Word(content,type,si.setWeight(type,isTitle));	
				tempList.add(w);
				vocabulary.addWord(w.content);								//词库加入	
			}			
		}
		if(tempList.size()>0){
			doc.wordBag.addAll(tempList);
			sen.add(tempList);			
		}
	}
	
	//读取文本内容
	public static String readFile(File file){

		String result = new String();
		Reader reader = null;
		try{
			if(file.isFile()&&file.exists()){
				reader = new InputStreamReader(new FileInputStream(file),"utf-8");
				BufferedReader br = new BufferedReader(reader);
				String line = null;
				while((line = br.readLine())!=null){
					result += line.trim();
				}
				reader.close();
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

}

