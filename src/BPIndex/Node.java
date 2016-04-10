package BPIndex;

import java.io.Serializable;
import java.util.Arrays;

abstract class Node implements Serializable{
	protected Comparable[] keys;
	protected Node[] pointers;
	protected int size;
	public INode parent;
	
	public Node(int maxK, int maxP, INode parent){
		keys 	 	= new Comparable[maxK];
		pointers 	= new Node[maxP];
		this.parent = parent;
		size 	 	= -1;
	}

	public Comparable[] getKeys() {
		return keys;
	}
	
	public Node[] getPointers() {
		return pointers;
	}	
	
	public boolean isFull(){
		return size == keys.length - 1;
	}
	
	public abstract void insert(Comparable key, Reference ref);
	
	
	public int getIndex(Comparable key){
		for (int i = 0; i<=size; i++)
			if(keys[i].equals(key))
				return i;
		return -1;
	}
	
	public int getIndexPtr(Node ptr){  // TODO binary search.
		for(int i = 0; i<pointers.length; i++)
			if(pointers[i].equals(ptr))
				return i;
		return -1;
	}
	
	public int getSize(){
		return this.size;
	}
	
	public void setSize(int x){
		this.size = x;
	}
	
	public String toString(){
		return Arrays.toString(this.getKeys());
	}
	
	public int numberOfPointers(){
		int n = 0;
		for(int i = 0; i<this.pointers.length; i++, n++)
			if (this.pointers[i] == null)
				break;
			
		return n;
	}
	
	public abstract Object search(Comparable key);

//	public Object delete(Comparable key) {
//		Comparable [] nwKeys = new Comparable[this.keys.length];	// TODO delete in place 
//		for(int i = 0, k = 0; i<=size; i++)
//			if(keys[i] != key)
//				nwKeys[k++] = keys[i];
//		size--;
//		keys = nwKeys;
//		return key;
//	}
	
	public abstract Reference delete(Comparable key);
}