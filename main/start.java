package main;


import java.util.List;
import java.util.Map;

import cnn.CNNUtil;
import dataClass.Doc;
import dataClass.dataSet;
import pmi.*;


public class start {
	
	static String trainPath = "G:\\neural network\\data\\education";		//语料库路径
	static String testPath = "G:\\neural network\\data\\test";			//测试集路径
	
	//static String[] test  = null;
	
	public static void main(String[] args) {						
		
//		String x = "未检出不合格";
//		String ps = NlpirMethod.NLPIR_ParagraphProcess(x, 1);
//		System.out.println(ps);
		
		
		//System.out.println("开始生成训练集...");
		train();
		//System.out.println("训练集生成完毕.开始生成测试集..");
		test();
		//System.out.println("测试集生成完毕...开始训练神经网络");
    	
	}
	
	//语料训练
	public static void train(){
		FileProcess tp = new FileProcess();
		tp.load_dictionary(true);					
  		tp.load_trainTxts();					//获取训练集&词频统计							
		
		//特征选取
//		Pmi pmi = new Pmi();
// 		int num = pmi.calc_soPMI();//Pmi计算 
// 		FileProcess.save_features(dataSet.getFeature().feature);
		
  		int num = FileProcess.load_features();
		Map<Doc, List<double[]>> result = Tf_Idf.set_feature(num); 	//选取特征值	
		mix_vector(result,num);
		CNNUtil.runTrain();
	}
	
	//向量融合
	public static void mix_vector(Map<Doc, List<double[]>> sens_docs,int length){
		List<Doc> docs = dataSet.getDocuments();
		for(Doc doc:docs){
			List<double[]> sentence = sens_docs.get(doc);
			doc.vector = new double[length];
			if(sentence.size()<=0)	continue;
			
			for(double[] value:sentence){			
				for(int i=0;i<doc.vector.length;i++){
					doc.vector[i] += value[i];
				}
			}
		}		
	}
	
	public static void test(){
 		FileProcess tp = new FileProcess();	
		tp.load_dictionary(false);
		//tp.loadFiles(testPath,false);				//获取测试集&词频统计	
 		tp.load_testTxt();
    	
    	//选取特征值
		int num = FileProcess.load_features();
    	Map<Doc, List<double[]>> result = Tf_Idf.set_feature(num);
    	mix_vector(result,num);
    	CNNUtil.runTest();
    	//FileProcess.wirte_txt(outputPath,result,false);
	}	

}

