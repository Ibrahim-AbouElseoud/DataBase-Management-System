package es_que_l;

import java.io.Serializable;

/**
 * A utility class to help in detecting invalid insertions and to add more 
 * info for the table.
 * @author Joe3141
 *
 */
public class Schema implements Serializable{
	String label = "";
	String type = "";
	
	public Schema(){
		
	}
	
	public Schema(String l, String t){
		label = l;
		type = t;
	}
	
	public String toString(){
		return " " + this.label + " ";
	}
}
