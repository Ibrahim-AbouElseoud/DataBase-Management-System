package BPIndex;

import java.io.Serializable;

 class INode extends Node implements Serializable{

	public INode(int maxK, int maxP, INode parent) {
		super(maxK, maxP, parent);
		
	}

	@Override
	public Comparable search(Comparable key) {
		return this.getKeys()[this.getIndex(key)];
	}

	public void insert(Comparable key, Reference ref) {
		if(size>-1){
			Comparable[] nwKeys = new Comparable[keys.length];
			boolean inserted = false;
			for(int i = 0, k=0; i<=size; i++){
				if(keys[i] != null)
				if(key.compareTo(keys[i])<0){
					nwKeys[k++] = key;
					nwKeys[k++] = keys[i];
					inserted = true;
					size++;
				}else{
					nwKeys[k++] = keys[i];
				}
			}
			if(!inserted)
				nwKeys[++size] = key;
			keys  = nwKeys;
			}else
				keys[++size] = key;
	}

	@Override
	public Reference delete(Comparable key) {
		Comparable [] nwKeys = new Comparable[this.keys.length];	// TODO delete in place 
		for(int i = 0, k = 0; i<=size; i++){
			if(!(keys[i].equals(key))){
				nwKeys[k++] = keys[i];
			}
		}
		size--;
		keys = nwKeys;
		return null;
	}
	
	public void deletePointer(Node a){
		Node [] nwptrs = new Node[this.pointers.length];	// TODO delete in place 
		int s = this.numberOfPointers();
		for(int i = 0, k = 0; i<s; i++)
			if(pointers[i] != a){
				nwptrs[k++] = pointers[i];
			}
		pointers = nwptrs;
	}

}
