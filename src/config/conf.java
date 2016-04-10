package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * @author Joe3141
 * @author Ibrahim
 * Contains the configurations for the Database application.
 */
public class conf {
	public static int pageSize = 200;
	 public static int maxNumberOfTables = 200;
	 public static double BPlusTreeN = 20;
	public static int getPageSize(){
		return pageSize;
	}
	public static void readProperties(){
		 Properties props = new Properties();
		 InputStream is = null;
		 
		    
		    	try{
		        File f = new File("config\\DBApp.properties");
		        is = new FileInputStream( f );

		        try {
					props.load( is );
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();System.out.println("IO Exception!");
				}
		    	}
		    	catch(FileNotFoundException e){e.printStackTrace();System.out.println("File not found!");}
		 
		     pageSize = new Integer(props.getProperty("MaximumRowsCountinPage", "200"));
		     BPlusTreeN = new Integer(props.getProperty("BPlusTreeN", "20"));
	 }
}
