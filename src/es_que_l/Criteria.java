package es_que_l;

import java.io.Serializable;

/**
 * 
 * @author Joe3141
 * @author Ibrahim
 * 
 * A utility class containing the index of the column to be compared and the value of the object
 * to be compared with.
 */
public class Criteria implements Serializable{
	public Object val;
	public int col; 
}
