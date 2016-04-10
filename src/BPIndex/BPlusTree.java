package BPIndex;

import java.io.Serializable;
import java.util.Arrays;
/**
 * 
 * BPlus tree for submission 3 , it works well and efficiently with integers , however not other values 
 * and you can also set its order ( you can test the BPlus tree by running the main method in this class)
 *
 */
public class BPlusTree implements Serializable{
	private double order;
	private int maxPtrs;
	private int maxKeys;
	private int minPR;
	private int minKR;
	private int minPL;
	private int minKL;
	private int minPI;
	private int minKI;
	private int size;
	private Node root;
	
	public BPlusTree(double order){
		 this.order = order;
		 maxPtrs = (int) (order + 1);
		 maxKeys = (int) order;
		 minPR   = 1;
		 minKR   = 1;
		 minPL   = (int) Math.floor((order+1.0) / 2.0);
		 minKL   = (int) Math.floor((order+1.0) / 2.0);
		 minPI   = (int) Math.ceil((order+1.0) / 2.0);
		 minKI   = (int) (Math.ceil(((order+1.0) / 2.0) - 1.0));
		 size = 0;
		 root = new LNode(maxKeys, maxPtrs, null);
	}
	
	public Reference search(Comparable key){
		return getLeaf(key).search(key);
	}
	
	/**
	 * Inclusive range searching on both boundaries.
	 * @param lo
	 * @param hi
	 * @return
	 */
	public Reference[] searchRange(Comparable lo, Comparable hi){
		LNode curr =  getLeaf(lo);
		Comparable[] cKeys = curr.getKeys();
		Reference[] res = new Reference[size];
		Reference[] res_begad;	// true res
		int k = 0;
		while(!(curr == null)){
			boolean done = false;
			for(int i = 0; i<=curr.size; i++){
				if(cKeys[i].compareTo(hi) > 0){
					done = true;
					break;
				}
				else if(cKeys[i].compareTo(lo) >= 0)
					res[k++] = curr.getTuples()[i];
			}
			if(done)
				break;
			else{
				curr = (LNode) curr.getPointers()[0];
				cKeys = curr.getKeys();
			}
		}
		res_begad = new Reference[k];
		for(int i = 0; i<k; i++)
			res_begad[i] = res[i];
		return res_begad;
	}
	
	public void insert(Comparable key, Reference ref){
		LNode curr = getLeaf(key);
		if(!(curr.isFull()))
			curr.insert(key, ref);
		else
			insertSplit(curr, key, ref);
		size++;
	}
	
	private void insertSplit(LNode curr, Comparable key, Reference ref){
		if(root.equals(curr) && curr instanceof LNode){
			// constructing the new root and child
			INode futRoot = new INode(maxKeys, maxPtrs, null);
			LNode futChild = new LNode(maxKeys, maxPtrs, futRoot); //small vals - key
			//getting the all the values and distributing it on the two children
			Comparable[] vals = new Comparable[maxPtrs];
			Comparable[] big = curr.getKeys();
			Reference[] buckets = new Reference[maxPtrs];
			Reference[] bigR = curr.getTuples();
			getVals(vals, big, key); // it's sorted here
			getVals(buckets, bigR, ref);
			int midIdx = vals.length / 2;
			futChild.setSize(-1);
			curr.setSize(-1);
			Comparable newKey = vals[midIdx];
			Comparable[] small = futChild.getKeys();
			Reference[] smallR = futChild.getTuples();
			for(int i = 0; i<midIdx; i++)
				futChild.insert(vals[i], buckets[i]);
			makeNull(big);
			for(int i = midIdx; i<vals.length; i++)
				curr.insert(vals[i], buckets[i]);
			//Inserting the mid in the new root and assigning the pointers
			futRoot.insert(newKey, null);
			Node[] rtPntrs = futRoot.getPointers();
			rtPntrs[0] = futChild;
			rtPntrs[1] = curr;
			curr.parent = futRoot;
			root = futRoot;
			futChild.getPointers()[0] = curr;
			curr.getPointers()[1] = futChild; // humhumhum
		}else{
			LNode futChild = new LNode(maxKeys, maxPtrs, null);
			Comparable[] vals = new Comparable[maxPtrs];
			Comparable[] big = curr.getKeys();
			Reference[] buckets = new Reference[maxPtrs];
			Reference[] bigR = curr.getTuples();
			getVals(vals, big, key);
			getVals(buckets, bigR, ref);
			makeNull(big);
			makeNull(bigR);
			curr.size = -1;
			int midIdx = vals.length / 2;
			Comparable newKey = vals[midIdx];
			Comparable[] small = futChild.getKeys();
			Reference[] smallR = futChild.getTuples();
			for(int i = 0; i<midIdx; i++)
				futChild.insert(vals[i], buckets[i]);
			for(int i = midIdx; i<vals.length; i++)
				curr.insert(vals[i], buckets[i]);
			futChild.getPointers()[0] = curr;
			futChild.getPointers()[1] = curr.getPointers()[1];
			futChild.getPointers()[1].getPointers()[0] = futChild;
			curr.getPointers()[1] = futChild;
			insertInternaly(newKey, curr.parent, futChild, curr);
		}
	}
	
	private void insertInternaly(Comparable key, INode curr, Node small, Node big){
		 if(!(curr.isFull())){
			curr.insert(key, null);
			int idx = curr.getIndex(key);
			Node[] ptrs = curr.getPointers();
			ptrs[idx] = small;
			ptrs[idx+1] = big;
			small.parent = curr;
			big.parent = curr;
		}else{
			Node[] ptrs = new Node[maxPtrs+1];
			Node[] currPtrs = curr.getPointers(); // also big pointers
			int ptrIdx = curr.getIndexPtr(big);
			int k = 0;
			for(int i = 0; i<currPtrs.length; i++){
				if(i==ptrIdx){
					ptrs[k++] = small;
					ptrs[k++] = big;
				}else
					ptrs[k++] = currPtrs[i];
			}
			INode futChild = new INode(maxKeys, maxPtrs, null);
			Node[] sPtrs = futChild.getPointers();
			Comparable[] vals = new Comparable[maxPtrs];
			Comparable[] ibig = curr.getKeys();
			getVals(vals, ibig, key); // it's sorted here
			int midIdx = vals.length / 2;
			Comparable newKey = vals[midIdx];
			Comparable[] ismall = futChild.getKeys();
			curr.setSize(-1);
			for(int i = 0; i<midIdx; i++)
				futChild.insert(vals[i], null);
			makeNull(ibig);
			for(int i = midIdx+1; i<vals.length; i++) // not taking the key here
				curr.insert(vals[i], null);
			for(int i = 0; i<ptrs.length/2; i++){ 
				sPtrs[i] = ptrs[i];
				ptrs[i].parent = futChild;
			}
			makeNull(currPtrs);
			for (int i = (ptrs.length/2) , l = 0; i < ptrs.length; i++, l++){
				currPtrs[l] = ptrs[i];
				ptrs[i].parent = curr;
			}
			if(!(curr.equals(root)))
			insertInternaly(newKey, curr.parent, futChild, curr);	//Recursive call
			else{
				INode futRoot = new INode(maxKeys, maxPtrs, null);
				futRoot.insert(newKey, null);
				Node[] rtPntrs = futRoot.getPointers();
				rtPntrs[0] = futChild;
				rtPntrs[1] = curr;
				curr.parent = futRoot;
				root = futRoot;
			}
		}
	}
	
	public Reference delete(Comparable key){
		LNode curr = getLeaf(key);
		if(root.equals(curr))
			if(root.getSize() > -1){
				size--;
				return curr.delete(key);
			}
			else{
				System.out.println("Attempting to delete from an empty index");
				return null;
			}
		else if(curr.getSize() + 1 > minKL){
			size--;
			return curr.delete(key);
		}
		else{
			 Reference res = curr.delete(key);
			 deleteBorrow(curr);
			 size--;
			 return res;
		}
	}
	
	private void deleteBorrow(LNode curr){
		LNode lSib = (LNode) curr.getPointers()[1];
		LNode rSib = (LNode) curr.getPointers()[0];
		if(lSib != null && lSib.getSize() + 1 > minKL && curr.parent.equals(lSib.parent)){
			Comparable oldKey = curr.getKeys()[0];
			Reference nwKey = lSib.delete(lSib.getKeys()[lSib.getSize()]);
			curr.insert(nwKey.key, nwKey);
			curr.parent.keys[curr.parent.getIndex(oldKey)] = nwKey.key;
		}else if(rSib != null && rSib.getSize() + 1 > minKL && curr.parent.equals(rSib.parent)){
			Comparable nwKey = rSib.getKeys()[1];
			Reference oldKey = rSib.delete(rSib.getKeys()[0]);
			curr.insert(oldKey.key, oldKey);
			rSib.parent.keys[rSib.parent.getIndex(oldKey.key)] = nwKey;
		}else
			deleteMerge(curr);
	}
	
	private void deleteMerge(LNode curr) {
		Comparable[] delKeys = curr.getKeys();
		Reference[] delBuks = curr.getTuples();
		Node[] delPtrs = curr.getPointers();
		INode parent = (INode) curr.parent;
		int currSize = curr.size;
		Node mergeNode;
		if(delPtrs[1].parent.equals(parent)){ // it's not its parent's left most child
			// please don't judge on refactoring :(
			mergeNode = delPtrs[1];
			Comparable navKey = parent.keys[parent.getIndexPtr(curr)-1];
			parent.deletePointer(curr);
			parent.delete(navKey);
			curr.parent = null;
			delPtrs[1].pointers[0] = delPtrs[0];
			if(delPtrs[0] != null) //rightmost
			delPtrs[0].pointers[1] = delPtrs[1];
			curr = null;
			for(int i = 0; i<=currSize; i++)
				mergeNode.insert(delKeys[i], delBuks[i]);
		}else{ // it is, then we have to merge it with its right sibling
			mergeNode = delPtrs[0];
			Comparable navKey = parent.keys[parent.getIndexPtr(curr)];
			parent.deletePointer(curr);
			parent.delete(navKey);
			curr.parent = null;
			delPtrs[1].pointers[0] = delPtrs[0];
			delPtrs[0].pointers[1] = delPtrs[1];
			curr = null;
			for(int i = 0; i<=currSize; i++)
				mergeNode.insert(delKeys[i], delBuks[i]);
		}
		if(parent.equals(root) && (parent.size + 1 < minKR || parent.numberOfPointers() < minPR)){
			parent = null;
			root = mergeNode;
		}else if(parent.size + 1 < minKI || parent.numberOfPointers() < minPI){
			deleteInternally(parent);
		}
	}

	private void deleteInternally(INode curr) {
		//try to borrow
		INode lSib = curr.parent.getIndexPtr(curr) > 0 ? (INode) curr.parent.getPointers()[curr.parent.getIndexPtr(curr) - 1]
					: null;
		INode rSib = curr.parent.getIndexPtr(curr) < curr.parent.numberOfPointers() - 1 ? (INode) curr.parent.getPointers()[curr.parent.getIndexPtr(curr) + 1]
				: null;
		if(lSib != null && lSib.getSize() + 1 > minKI && curr.parent.equals(lSib.parent)){
			Comparable rtmstKey = lSib.keys[lSib.size];
			Node rtmstptr = lSib.pointers[lSib.numberOfPointers()-1];
			lSib.delete(rtmstKey);
			lSib.deletePointer(rtmstptr);
			curr.insert(rtmstKey, null);
			Node[] nwPtrs = new Node[curr.pointers.length];
			nwPtrs[0] = rtmstptr;
			nwPtrs[0].parent = curr;
			for(int i = 0, k = 1; i<curr.numberOfPointers(); i++, k++)
				nwPtrs[k] = curr.pointers[i];
			curr.pointers = nwPtrs;
			curr.parent.keys[curr.parent.getIndexPtr(curr) - 1] = rtmstKey;
		}else if(rSib != null && rSib.getSize() + 1 > minKI && curr.parent.equals(rSib.parent)){
			Comparable lftmstKey = rSib.keys[0];
			Node lftmstptr = rSib.pointers[0];
			rSib.delete(lftmstKey);
			rSib.deletePointer(lftmstptr);
			curr.insert(lftmstKey, null);
			curr.pointers[curr.numberOfPointers()] = lftmstptr;
			lftmstptr.parent = curr;
			curr.parent.keys[curr.parent.getIndexPtr(curr)] = lftmstKey;
		}else
			deleteMergeInternally(curr);
	}
	
	private void deleteMergeInternally(INode curr) {
		Comparable[] delKeys = curr.getKeys();
		Node[] delPtrs = curr.getPointers();
		INode parent = curr.parent;
		int currSize = curr.size;
		INode mergeNode;
		if(!(curr.equals(parent.pointers[0]))){ 
			mergeNode = (INode) parent.pointers[parent.getIndexPtr(curr)-1];
			Comparable navKey = parent.keys[parent.getIndexPtr(curr)-1];
			for(int i = 0; i<=currSize; i++)
				mergeNode.insert(delKeys[i], null);
			int nptrs = mergeNode.numberOfPointers();
			int np = curr.numberOfPointers(); 
			for(int i = nptrs, k = 0; k<np;i++, k++){
				mergeNode.pointers[i] = delPtrs[k];
				mergeNode.pointers[i].parent = mergeNode;
			}
			parent.deletePointer(curr);
			curr.parent = null;
			parent.delete(navKey);
			curr = null;
			mergeNode.insert(navKey, null);
		}else{ 
			Node[] nwPtrs = new Node[curr.pointers.length];
			mergeNode = (INode) parent.pointers[parent.getIndexPtr(curr)+1];
			Comparable navKey = parent.keys[parent.getIndexPtr(curr)];
			for(int i = 0; i<=currSize; i++)
				mergeNode.insert(delKeys[i], null);
			int nptrs = mergeNode.numberOfPointers();
			int np = curr.numberOfPointers();
			for(int i = 0; i<np; i++){
				nwPtrs[i] = delPtrs[i];
				nwPtrs[i].parent = mergeNode;
			}
			for(int i = np, k = 0; k<nptrs;i++, k++)
				nwPtrs[i] = mergeNode.pointers[k];
			mergeNode.pointers = nwPtrs;
			parent.deletePointer(curr);
			curr.parent = null;
			parent.delete(navKey);
			curr = null;
			mergeNode.insert(navKey, null);
		}
		if(parent.equals(root) && (parent.size + 1 < minKR || parent.numberOfPointers() < minPR)){
			parent = null;
			root = mergeNode;
		}else if(parent.size + 1 < minKI || parent.numberOfPointers() < minPI)
			deleteInternally(parent);
	}

	private LNode getLeaf(Comparable key){
		Node curr = root;
		Comparable[] cKeys;  // curr keys
		Node[] cPntrs;  // curr pointers
		while(!(curr instanceof LNode)){
			cKeys = curr.getKeys();
			cPntrs = curr.getPointers();
			int h = curr.getSize();
			if(curr != null)
			for(int i = 0; i <= h; i++){
				if(i == curr.getSize()){
					if(cKeys[i] != null)
					if(key.compareTo(cKeys[i])<0)
						curr = cPntrs[i];
					else
						curr = cPntrs[i+1];
				}else{
					if(cKeys[i] != null)
					if(key.compareTo(cKeys[i])<0){
						curr = cPntrs[i];
						break;
					}
				}
			}
		}
		return (LNode) curr;
	}
	
	private void getVals(Object[] vals, Object[] node, Object key){
		for(int i = 0; i<node.length; i++)
			vals[i] = node[i];
		vals[maxKeys] = key;
		Arrays.sort(vals);
	}
	
	private void makeNull(Object[] tuple){
		for(int i = 0; i<tuple.length; i++)
			tuple[i] = null;
	}
	
	public boolean isEmpty(){
		return size == 0;
	}
	
	public void printTree(){
		Node curr = root;
		while(!(curr instanceof LNode))
			curr = curr.getPointers()[0];
		while(!(curr == null)){
			System.out.println(curr);
			curr = curr.getPointers()[0];
		}
	}
	
	public String toString(){
		String res = "";
		Node curr = root;
		while(!(curr instanceof LNode))
			curr = curr.getPointers()[0];
		while(!(curr == null)){
			res += curr.toString();
			curr = curr.getPointers()[0];
		}
		return res;
	}
	
	public static void main(String[] args){
		BPlusTree a = new BPlusTree(3);
		a.insert(2, new Reference(3, 45, 2));
		a.insert(1, new Reference(1, 35, 1));
//		LNode r = (LNode) a.root;
//		System.out.println(Arrays.toString(r.getTuples()));
		a.insert(3, new Reference(7, 176, 3));
		a.insert(4, new Reference(7, 176, 4));
		a.insert(5, new Reference(7, 176, 5));
		a.insert(6, new Reference(7, 176, 6));
		a.insert(7, new Reference(7, 176, 7));
		a.insert(8, new Reference(7, 176, 8));
		a.insert(9, new Reference(7, 176, 9));
		a.delete(9);
		a.delete(8);
		a.delete(4);
		System.out.println(a.root);
//		System.out.println(Arrays.toString(a.root.pointers));
//		a.printTree();
	}
	
}
