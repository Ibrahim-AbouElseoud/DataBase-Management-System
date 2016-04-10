package es_que_l;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;

import bpTree.BTree;

public class Indexing implements Serializable {
	private Hashtable<String,BTree> index= new Hashtable<String, BTree>();; //(colnameIndexed,Index)
	
	public void initilizeIndex(String colName,String colType){
		BTree a = null;
		switch(colType){
		case "Integer":
			a = new BTree<Integer, Pointer>(); break;
		case "String":
			a = new BTree<String, Pointer>(); break;
		case "Double":
			a = new BTree<Double, Pointer>(); break;
		case "Date":
			a = new BTree<Date, Pointer>(); break;
		case "Boolean":
			a = new BTree<Boolean, Pointer>(); break;
		}
		index.put(colName, a);
	}
	
	public void insertInIndex(String colName,Comparable key,Pointer ptr){
		BTree a=index.get(colName);
		a.insert(key, ptr);
	}
	public Pointer deleteInIndex(String colName,Comparable key){
		BTree a=index.get(colName);
		Pointer ptr=(Pointer) a.search(key);
		a.delete(key);
		return ptr;
	}
	public void updateInIndex(String colName,Comparable key,Comparable newKey){
		BTree a=index.get(colName);
		Pointer ptr=(Pointer) a.search(key);
		a.delete(key);
		a.insert(newKey, ptr);
	}
	/** Gives back the B+ tree of that colName
	 *  
	 * @param colName
	 * @return BTree
	 */
	public BTree getColIndex(String colName){ //gives back the B+ tree of that colName
		return index.get(colName);
	}
	
	public Pointer getFromIndex(String colName,Comparable key){
		BTree a=index.get(colName);
		return (Pointer) a.search(key);
	}
}
