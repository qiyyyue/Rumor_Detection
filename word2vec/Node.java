package word2vec;

public abstract class Node implements Comparable<Node> { 
	public double freq; 
	public Node parent; 
	public int code;				//haffman编码的值
  
	// 语料预分类
	public int category = -1;
  
	@Override    
	public int compareTo(Node neuron) {  	 
		if (this.category == neuron.category) {   		
			return (this.freq > neuron.freq)?1:-1;    	 
		} else if (this.category > neuron.category) {      	
			return 1;   	 
		} else {     	
			return 0;    	
		}   
	}
}
