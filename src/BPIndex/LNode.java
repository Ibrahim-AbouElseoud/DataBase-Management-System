package BPIndex;

import java.io.Serializable;

 class LNode extends Node implements Serializable{ // I know it already extends from a serializable 
	 												//class but I don't trust java.
	private Reference[] tuples;
	//pointers in a leaf node corresponds to its siblings, index 0 points right and 
	//index 1 points left
	public LNode(int maxK, int maxP, INode parent) {
		super(maxK, maxP, parent);
		tuples = new Reference[maxK];
	}
	
	public Reference[] getTuples(){
		return tuples;
	}

	@Override
	public Reference search(Comparable key) {
		int idx = this.getIndex(key);
		Reference res = idx > -1 ? tuples[idx] : null;
		return res;
	}
	
	
	public void insert(Comparable key, Reference ref){
		if(size>-1){
			Comparable[] nwKeys = new Comparable[keys.length];
			Reference[] nwBuk  = new Reference[keys.length];
			boolean inserted = false;
			int i, k;
			for( i = 0, k=0; i<=size; i++){
				if(keys[i] != null)
				if(key.compareTo(keys[i])<0){
					nwKeys[k] = key;
					nwBuk[k++] = ref;
					nwKeys[k] = keys[i];
					nwBuk[k++] = tuples[i];
					inserted = true;
					size++;
					break;
				}else{
					nwKeys[k] = keys[i];
					nwBuk[k++] = tuples[i];
				}
			}
			for(int l = i, o = k; l<=size && o<keys.length; l++, o++){
				nwKeys[o] = keys[l];
				nwBuk[o] = tuples[l];
			}
			if(!inserted){
				nwKeys[++size] = key;
				nwBuk[size] = ref;
			}
			keys  = nwKeys;
			tuples = nwBuk;
			}else{
				keys[++size] = key;
				tuples[size] = ref;
			}
	}

	@Override
	public Reference delete(Comparable key) {
		Comparable [] nwKeys = new Comparable[this.keys.length];
		Reference  [] nwBuk =  new Reference[this.keys.length];// TODO delete in place 
		Reference res = tuples[getIndex(key)];
		for(int i = 0, k = 0; i<=size; i++)
			if(keys[i] != key){
				nwKeys[k] = keys[i];
				nwBuk[k++] = tuples[i];
			}
		size--;
		keys = nwKeys;
		tuples = nwBuk;
		return res;
	}
	
}
