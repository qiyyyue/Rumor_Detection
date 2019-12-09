package dataClass;

import java.util.Map;

public class SyntaxIndex {
	String tartgetCon = "";
	boolean underCheck = false;
	public boolean turnWord = false;
	public boolean deny = false;
	
	public double degree = 0;
	
	public void reflush(){
		if(turnWord){
			turnWord = false;
			if(underCheck)	underCheck = false;			
		}
	}
	
	public void twistHandle(String content,Map<String, String> twistDic){
		if (twistDic.containsKey(content)){  				//识别到转折连接词.激活转折句检查								
			tartgetCon = twistDic.get(content);
			underCheck = true;
			if(content.equals("然而")) turnWord = true;
		}else if(underCheck){								//转折句式识别
			if(tartgetCon.contains(content)){
				turnWord = true;								//转折句处理开始.
			}					
		}
	}
	//权重设置
	public double setWeight(String type,int isTitle){		
		double result = (turnWord)?1.5:1;
		if(type.matches("^[a|d|v].*"))
		{
			result += degree;
			degree = 0;		
			if(deny){
				result += -1;
				deny = false;
			}
		}		
		return isTitle*result;
	}
}
