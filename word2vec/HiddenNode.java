package word2vec;


public class HiddenNode extends Node{
    
    public double[] syn1 ; 										//hidden->out
    //public Map<Integer, double[]> topic_vec = new HashMap<>();	//topic - hidden->out
    
   
    public HiddenNode(int layerSize){      
    	syn1 = new double[layerSize] ;     
    }
    
    public HiddenNode(int layerSize,int topicNum){      
    	syn1 = new double[layerSize] ;
//        System.out.println(layerSize);
//    	for(int i=0;i<topicNum;i++){   	 		  
//    		double[] syn = new double[layerSize];
//    		topic_vec.put(i, syn);
//    	}
    }
    
}
 