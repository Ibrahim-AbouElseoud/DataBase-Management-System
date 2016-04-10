package BPIndex;

import java.io.Serializable;

public class Reference implements Comparable, Serializable{
	private int pageNo;
	private int pageIdx;
	protected Comparable key;
	
	public Reference(int pageNo, int pageIdx, Comparable key){
		this.setPageNo(pageNo);
		this.setPageIdx(pageIdx);
		this.key = key;
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageIdx() {
		return pageIdx;
	}

	public void setPageIdx(int pageIdx) {
		this.pageIdx = pageIdx;
	}
	
	public String toString(){
		return this.pageNo + " " + this.pageIdx;
	}

	public int compareTo(Object arg0) {
		Reference x = (Reference) arg0;
		return this.key.compareTo(x.key);
	}
	
}
