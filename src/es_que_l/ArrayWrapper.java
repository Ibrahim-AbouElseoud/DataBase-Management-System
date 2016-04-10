package es_que_l;

import java.util.Arrays;
/**
 * 
 * @author Joe3141
 * @author Ibrahim
 * 
 * A utility class that wraps a 2d array and has a "toString" method that prints each array
 * line by line to simulate the printing of records.
 */
public class ArrayWrapper {
	private Object[][] data;
	
	public ArrayWrapper(Object[][] in){
		data = in;
	}
	
	public String toString(){
		String res = "";
		for(int i = 0; i<data.length; i++){
			if(data[i].length!=0)
			res += Arrays.toString(data[i]) + "\n";
		}
		return res;
	}
}
