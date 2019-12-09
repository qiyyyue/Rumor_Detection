package word2vec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

//哈弗曼树中的叶子结点（单词）
public class LeafNode extends Node {
  public String name;
  public double[] syn0 = null; 									// input->hidden，词向量
 // public Map<Integer, double[]> topic_vec = new HashMap<>();	//改进的词向量
  
  public List<Node> nodes = null;		// 路径神经元
  public int[] codeArr = null;

  public List<Node> make_HaffmanPath() {
    if (nodes != null) 	{return nodes;}
    
    Node neuron = this;
    nodes = new LinkedList<>();
    while ((neuron = neuron.parent) != null) {
      nodes.add(neuron);
    }
    Collections.reverse(nodes);	//降序排序，反转
    codeArr = new int[nodes.size()];
    
    //获取路径上所有结点的哈夫曼编码
    for (int i = 1; i < nodes.size(); i++) {
      codeArr[i - 1] = nodes.get(i).code;		
    }
    codeArr[codeArr.length - 1] = this.code;

    return nodes;
  }

  public LeafNode(String name, double freq, int layerSize) {
    
	  this.name = name;    
	  this.freq = freq;    
	  Random random = new Random();   
	  this.syn0 = new double[layerSize];
    
	  for (int i = 0; i < syn0.length; i++) {       
		  syn0[i] = (random.nextDouble() - 0.5) / layerSize;         
	  }   
   
//	  for(double[] syn:topic_vec.values()){   	
//		  syn = new double[layerSize];    	
//		  for (int i = 0; i < syn.length; i++) {  	        
//			  syn[i] = (random.nextDouble() - 0.5) / layerSize;        	    
//		  }
//	  }       
    
  }
  
  public LeafNode(int topicNum,String name, double freq, int layerSize) {
	    
	  this.name = name;    
	  this.freq = freq;
	  this.syn0 = new double[layerSize];
	  Random random = new Random();
	  
	  for (int i = 0; i < syn0.length; i++) {       
		  syn0[i] = (random.nextDouble() - 0.5) / layerSize;         
	  } 
   
//	  for(int i=0;i<topicNum;i++){   	
//		  double[] syn = new double[layerSize];    	
//		  for (int j = 0; j < syn.length; j++) {  	        
//			  syn[j] = (random.nextDouble() - 0.5) / layerSize;        	    
//		  }
//		  topic_vec.put(i, syn);
//	  }       
    
  }

  //用于有监督的创造hoffman tree
  public LeafNode(String name, double freq, int category, int layerSize) {
    this.name = name;
    this.freq = freq;
    this.syn0 = new double[layerSize];
    this.category = category;
    Random random = new Random();
    for (int i = 0; i < syn0.length; i++) {
      syn0[i] = (random.nextDouble() - 0.5) / layerSize;
    }
  }

}