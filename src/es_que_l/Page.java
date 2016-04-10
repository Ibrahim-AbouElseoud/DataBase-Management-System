package es_que_l;
import java.io.Serializable;
import java.util.Hashtable;

import exceptions.DBEngineException;
import exceptions.InvalidOperatorException;

/**
 * 
 * @author Joe3141
 * @author Ibrahim
 * It has a pointer indicating the index of the last tuple inserted or -1 if it's empty.
 * the data 2d array that holds the tuples.
 * the number of columns
 */
public class Page implements Serializable{
	int pointer;
	Object[][] data;
	int cols;
	
	/**
	 * initializes the pointer to -1.
	 * initializes the data array to have the maximum number of tuples from the config file
	 * as the x dimension size and the number of columns as the y direction size and assigns the
	 * number of columns instance variables for later use.(I know it can be retrieved from the data
	 * array I just think this is easier).
	 * @param cols
	 */
	public Page(int cols){
		pointer = -1;
		data = new Object[config.conf.getPageSize()][cols];
		this.cols = cols;
	}
	
	/**
	 * Inserts an array of objects(tuple) into the data array and increments the counter.
	 * @param in
	 */
	public void insert(Object[] in){
		data[++pointer] = in;
	}
	
	/**
	 * It initially makes a 2d array called 'res' having the same size as the data array that will hold the matching tuples.
	 * then it will search the whole page trying to find matching tuples and storing them in 'res'.
	 * Finally, for presentation reasons a new array is made with a size of only the matching tuples
	 * to be returned.
	 * @param query
	 * @param op
	 * @return
	 * @throws InvalidOperatorException 
	 */
	public Object[][] select(Criteria[] query, String op) throws DBEngineException{

		Object res[][] = new Object[config.conf.getPageSize()][cols];
		int index = 0; 
		for(int i = 0; i<=pointer; i++){
			Object[] curr = data[i];
			boolean wants = false;
			if(op.equals("AND")){
				wants = true;
				for(int j = 0; j<query.length; j++){
					if(curr[query[j].col]==null)
						wants=false;
					else if(!(curr[query[j].col].equals(query[j].val))){
						wants = false;
								break;
					}
				}
			}else if(op.equals("OR")){
				wants = false;
				for(int j = 0; j<query.length; j++){
					if(curr[query[j].col]!=null)
					if((curr[query[j].col].equals(query[j].val))){
						wants = true;
								break;
					}
				}
			}
//			else throw new InvalidOperatorException();

			if(wants)
				res[index++] = curr;
		}
		Object[][] res2 = new Object[index][cols];
		for(int i = 0; i<index; i++){
			res2[i] = res[i];
		}
		
		return res2;
	}
	//array of ptr
	//update
	//delete
	
	/**
	 * Checks whether the page is full.
	 * @return
	 */
	public boolean isFull() {
		return pointer == config.conf.getPageSize() - 1;
	}
	
	/**
	 * Checks whether the page is empty.
	 * @return boolean
	 */
	public boolean isEmpty(){
		return pointer == -1;
	}
	public static boolean checkNull(Object[] a){
		for(int i = 0; i<a.length; i++){
			if(a[i] != null)
				return false;
		}
		return true;
	}
	/**
	 * Searches for primary key value if it exists in page , if not it returns -1 , if yes returns its index
	 * @param pk
	 * @param pkIndex
	 * @return int
	 */
	public int searchPrimayKey(Object pk , int pkIndex){ // doo search method in page if it finds it returns index if not gives null then use it to update
		for(int i=0;i<=pointer;i++){
			if(data[i][pkIndex].equals(pk))
				return i;
		}
		return -1;
	}
	/**
	 * Updates the required entry with the given values , by taking the row index of the item to be updated and headers
	 * @param rowIndex
	 * @param headers
	 * @param htblColNameValue
	 */
	public void updateEntry(int rowIndex ,Schema[] headers,Hashtable<String,Object> htblColNameValue){
		for(String col :htblColNameValue.keySet()){
			for(int i=0;i<headers.length;i++){
				if(headers[i].label.equals(col)){
					data[rowIndex][i]=htblColNameValue.get(col);
				}
			}
		}
	}
/** Deletes tuples from the page according to the input query and the operator by making 
	 * the tuple hold only null values (see makeNull) rendering it obsolete from any usage.
	 * @param c
	 * @param op
	 */
	public void delete(Criteria[] c, String op) {
		for(int i = 0; i<=pointer; i++){
			Object[] curr = data[i];
			boolean wants = false;
			if(op.equals("AND")){
				wants = true;
				for(int j = 0; j<c.length; j++){
					if(curr[c[j].col]!=null)
					if(!(curr[c[j].col].equals(c[j].val))){
						wants = false;
								break;
					}
				}
			}else if(op.equals("OR")){
				wants = false;
				for(int j = 0; j<c.length; j++){
					if(curr[c[j].col]!=null)
					if((curr[c[j].col].equals(c[j].val))){
						wants = true;
								break;
					}
				}
			}
			if(wants)
				makeNull(curr);
		}
	}
	/**
	 * Takes an array and sets everything in it to null.
	 * @param tuple
	 */
	public void makeNull(Object[] tuple){
		for(int i = 0; i<tuple.length; i++)
			tuple[i] = null;
	}
	public void delete(int row){
		Object[] deleted = data[row];
		makeNull(deleted);
	}

	public int getPointer() {
		return pointer;
	}
}
