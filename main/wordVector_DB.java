package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import dataClass.dataSet;

public class wordVector_DB {
	//数据库账号密码
	static String driver = "com.mysql.jdbc.Driver";     			// 驱动程序名
	static String url = "jdbc:mysql://127.0.0.1:3306/test";     	// URL指向要访问的数据库名
	static String user = "root",password = "123456";				// MySQL配置时的用户名和密码 
	
	//加载词向量所在的数据库，获取词向量索引
	public static void load_vecIndex(){
		Map<String, Integer> word_vec_id = dataSet.get_wordVec_id(); 
		try {			
	        	Class.forName(driver);													// 加载驱动程序
				Connection conn = DriverManager.getConnection(url, user, password);		  // 连接数据库
			 
				if(!conn.isClosed()){
	            	System.out.println("Succeeded connecting to the Database!");
	            }
				
				Statement statement = conn.createStatement();          	            
				String sql = "select id,name from words_vec";	 // 要执行的SQL语句
				
				ResultSet rs = statement.executeQuery(sql);		   // 结果集
				
	            while (rs.next()){
	            	String name = rs.getString("name");
	            	String id = rs.getString("id");
	            	word_vec_id.put(name,Integer.parseInt(id));
	            }
	            rs.close();
	            conn.close();
			} catch (ClassNotFoundException e) {			
				e.printStackTrace();
			}catch (SQLException e) {			
				e.printStackTrace();
			}
	}
		
	//根据词语索引，查询数据库，获取词向量
	public static double[] get_wordVector(int id){
		 double[] result = null;
		 try {          
	            Class.forName(driver);          										 				// 加载驱动程序
	            Connection conn = DriverManager.getConnection(url, user, password);		 //连接数据库
	           	          
	            Statement statement = conn.createStatement();			// statement用来执行SQL语句
	                	            
	            String sql = "select * from words_vec where id = '"+id+"'";		 // 将要执行的SQL语句
	            ResultSet rs = statement.executeQuery(sql);
	           
	            while(rs.next()){
	            	String vec = rs.getString("vector");
		            String[] a = vec.trim().split(" ");
		            result = new double[a.length];
		            for(int i=0;i<a.length;i++){
		            	result[i]=Double.parseDouble(a[i]);
		            }
		            break;
	            }	            
	            conn.close();	        
		 } catch(ClassNotFoundException e) {	           
			 System.out.println("Sorry,can`t find the Driver!");	            
			 e.printStackTrace();	        
		 } catch(SQLException e){	            
			 e.printStackTrace();	        	        
		 }
		 return result;
	}
}
