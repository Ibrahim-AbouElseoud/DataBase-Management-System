package es_que_l;

import java.io.Serializable;
/**
 * 
 * Stores the location of the object in the form of (int pageIndex, int row in page)
 *
 */
public class Pointer implements Serializable{
	int pageIndex;
	int row;
	public Pointer(int pageIndex,int row){
		this.pageIndex=pageIndex;
		this.row=row;
	}
}
