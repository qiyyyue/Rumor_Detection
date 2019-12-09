package dataClass;

public class Word {
	public String content;
	public String type;
	public double weight = 0;
	
	public Word(String content,String type){
		this.content = content;
		this.type = type;		
	}
	
	public Word(String content,String type,double weight){
		this.content = content;
		this.type = type;
		this.weight = weight;
	}
}
