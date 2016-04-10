package es_que_l;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import bpTree.BTree;
import exceptions.AlreadyExistantIndexException;
import exceptions.DBEngineException;
import exceptions.DeleteFromEmptyTableException;
import exceptions.DuplicatePrimaryKeyException;
import exceptions.EntryNotFoundException;
import exceptions.InvalidInsertionException;
import exceptions.InvalidOperatorException;
import exceptions.NonExistantColumnException;



/**
 * 
 * @author Joe3141
 * @author Ibrahim
 * 
 * Table has a name, a hashtable mapping names of attributes to types and another
 * hashtable mapping names of attributes to possible references to other tables.
 * It has a string objecct indicating the primary key
 * A counter keeping track of the number of pages
 * An array list of the file names of pages indexed by the page counter
 * and finally an array of headers specifying the type of each column number in the page.
 */
public class Table implements Serializable{
	private String name;
	private Hashtable<String,String> htblColNameType;
	private Hashtable<String,String> htblColNameRefs;
	private String strKeyColName;
	private int pageCounter;
	private ArrayList<String> pages;

	private int PrimKeyIndex;
	private Schema[] headers;
	private ArrayList<Object> keys;
	
	private Indexing indexing; //(colnameIndexed,Index)
	private ArrayList<String> indexedCols;
	

	/**
	 * Resets all counters(page and record), assigns the meta data, creates an initial page,
	 * makes the headers array with the corresponding values of the name of the attributes 
	 * keeping in mind the touch.
	 * @param name
	 * @param colType
	 * @param colRef
	 * @param key
	 * @throws IOException 
	 */
	public Table(String name,Hashtable<String,String> colType, Hashtable<String,String> colRef, 
			String key) throws IOException{
		this.name = name;
		this.htblColNameType = colType;
		this.htblColNameRefs = colRef;
		this.strKeyColName = key;
		this.pageCounter = 0;

		headers = new Schema[colType.size()+1];
		keys = new ArrayList<Object>();
		pages = new ArrayList<String>();
		this.MakeMetaData();
		pages.add("classes\\es_que_l\\" + name + 
				pageCounter + ".class");
		Page p = new Page(colType.size()+1);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(
				"classes\\es_que_l\\" + name + pageCounter + ".class")));
		oos.flush();
		oos.close();

		
		//Primary key is automatically indexed
		indexing = new Indexing();
		indexing.initilizeIndex(key, colType.get(key));
		ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(new File("classes\\es_que_l\\" + name + "Index" + ".class")));
		oos2.writeObject(indexing);
		oos2.flush();
		oos2.close();
		//add PK to indexed columns
		indexedCols= new ArrayList<String>();
		indexedCols.add(key);
		p = null;
	}
	
	
	
	
	/**
	 * transforms the hashtable parameter into an array of objects carrying the values of the 
	 * tuple to be inserted into the page. then it adds the touch date to the tuple, followed by
	 * the process of finding the current page, if it's empty then it will create a new page object
	 * to be inserted into the current file(page), otherwise it will just read the object in the 
	 * current file. If the page is full then a new page object will be created, the page counter 
	 * incremented and this tuple will be inserted in the new page and finally will be written to disk
	 * with updating the array list page of the new file location. If it's not full then the 
	 * same will happen but no new page or file would be created.
	 * @param record
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidInsertionException 
	 * @throws DuplicatePrimaryKeyException 
	 */
	public void insert(Hashtable<String,Object> record) throws FileNotFoundException, IOException, ClassNotFoundException, InvalidInsertionException, DuplicatePrimaryKeyException{
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
				new File(pages.get(pageCounter))));
		Object[] in = new Object[headers.length];

		checkInsertion(record);
		if(keys.contains(record.get(strKeyColName)))
			throw new DuplicatePrimaryKeyException("Primary key " + record.get(strKeyColName) +" already exists");
		keys.add(record.get(strKeyColName));
		tableToArray(in, record);
		in[headers.length-1] = new Date();
//		in[0] = new Date();
		Page p;
		try{
		 p = (Page) ois.readObject();
		}catch(EOFException e){
			p = new Page(headers.length);
		}
		ois.close();
		if(p.isFull()){
			pageCounter++;
			pages.add("classes\\es_que_l\\" + name + pageCounter + ".class");
			p = null;
			Page s = new Page(headers.length);
			s.insert(in);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(pageCounter))));
			oos.writeObject(s);
			oos.flush();
			oos.close();
		

			Pointer ptr=new Pointer(pageCounter,s.getPointer());
			for(String itm: indexedCols){
				if(record.get(itm)!=null)
				indexing.insertInIndex(itm, (Comparable)record.get(itm), ptr);
			}
			ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(new File("classes\\es_que_l\\" + name + "Index" + ".class")));
			oos2.writeObject(indexing);
			oos2.flush();
			oos2.close();
			
			s = null;
		}else{
			p.insert(in);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(pageCounter))));
			oos.writeObject(p);
			oos.flush();
			oos.close();
			
			
			
			Pointer ptr=new Pointer(pageCounter,p.getPointer());
			for(String itm: indexedCols){
				if(record.get(itm)!=null)
				indexing.insertInIndex(itm, (Comparable)record.get(itm), ptr);
			}
			ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(new File("classes\\es_que_l\\" + name + "Index" + ".class")));
			oos2.writeObject(indexing);
			oos2.flush();
			oos2.close();
			
			p = null;
		}
//		
	}
	
	/**
	 * Checks Correct type Insertions in the corresponding columns. If it detected any invalid insertions
	 * It will throw an error and terminate.
	 * @param record
	 * @throws InvalidInsertionException
	 */
	private void checkInsertion(Hashtable<String, Object> record) throws InvalidInsertionException {
		for(String itm: record.keySet()){
			String type = "";
			for(int i = 0; i<headers.length; i++){
				if(itm.equals(headers[i].label)){
					type = headers[i].type;
					break;
				}
					
			}
			switch(type){
			case "Integer":
				if(!(record.get(itm) instanceof Integer))
					throw new InvalidInsertionException("Invalid Insertion. Inserting " + record.get(itm)  +
							"into a column of type " + type);
				break;
			case "Date":
				if(!(record.get(itm) instanceof Date))
					throw new InvalidInsertionException("Invalid Insertion. Inserting " + record.get(itm)  +
							"into a column of type " + type);
				break;
			case "Boolean":
				if(!(record.get(itm) instanceof Boolean))
					throw new InvalidInsertionException("Invalid Insertion. Inserting " + record.get(itm)  +
							"into a column of type " + type);
			default:
				break;
			}
		}
	}




	/**
	 * A utility function to fetch the values from a hashtable and copying them to an array.
	 * @param in
	 * @param test
	 */
	private static void tableToArray(Object[] in, Hashtable<String, Object> test){
		int i = 0;
//		for(String itm: test.keySet()){
//			in[i++] = test.get(itm);
//		}
		Enumeration<Object> enu =  test.elements();
		while(enu.hasMoreElements()){
			in[i++] = enu.nextElement();
		}
			
	}
	
	/**
	 * It initialized an arraylist of array wrappers(see the documentation on array wrappers),
	 * then adding an object containing the names of the attributes. It transforms the query 
	 * parameter into an array of criteria (see the documentation on the Criteria class) and loops
	 * on all the pages, calling their corresponding select method and passing to it the criteria
	 * array and the operator and finally it returns an Iterator over this array list.
	 * @param query
	 * @param op
	 * @return Iterator
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws DBEngineException 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	public Iterator select(Hashtable<String,Object> query, 
            String op) throws FileNotFoundException, IOException, ClassNotFoundException, DBEngineException{
		if(!(op.equalsIgnoreCase("and")||op.equalsIgnoreCase("or"))){
			throw new InvalidOperatorException();
		}
		ArrayList<ArrayWrapper> matches = new ArrayList<>();
		Object labels[][] = new Object[1][headers.length];
		labels[0] = headers;
		matches.add(new ArrayWrapper(labels));
		Criteria[] c = new Criteria[query.size()];
		int i = 0;

		 for(String itm : query.keySet()){
			 for(int j = 0; j<headers.length; j++){
				 if(headers[j].label.equals(itm)){
					 Criteria t = new Criteria();
					 t.col = j;
					 t.val = query.get(itm);
					 c[i++] = t;
					 break;
				 }
			 }
		 }
//		 	System.out.println("criteria length"+ c.length);
		 	int icnt = 0;  //index counter
		 	for(int o = 0; o<c.length; o++){
//		 		System.out.println("headers: "+headers[c[o].col].label);
		 		if(indexedCols.contains(headers[c[o].col].label)){
		 			icnt++;
		 		}
		 	}
//			ArrayList<Pointer>[]indexedColRes=new ArrayList[2]; //since 2 columns at max to query
		 	Pointer[] ptrs = new Pointer[2];
		 	Object[][] tuples;
//		 	System.out.println("indexedCol: "+ indexedCols);
//		 	System.out.println("icnt="+icnt);
			if(icnt == 2){
				if(op.equalsIgnoreCase("or")){
					tuples = new Object[2][headers.length];
					int o = 0;
					for(String itm :query.keySet())
						ptrs[o++] = indexing.getFromIndex(itm, (Comparable) query.get(itm));
					for(int y = 0; y<ptrs.length; y++){  // fetching tuples //TODO:
						 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptrs[y].pageIndex))));
						 Page temp = (Page) ois.readObject();
						 tuples[y] = temp.data[ptrs[y].row];
						 temp = null;
						 ois.close();
					}
					if(ptrs[0].equals(ptrs[1])){
						Object[][] res = new Object[1][headers.length]; 
						matches.add(new ArrayWrapper(res));
					}else
						matches.add(new ArrayWrapper(tuples));
			}else{
			
				tuples = new Object[1][headers.length];
				String key = headers[c[0].col].label;
				Pointer ptr=indexing.getFromIndex(key, (Comparable) query.get(key));
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptr.pageIndex))));
				Page temp = (Page) ois.readObject();
				tuples[0] = temp.data[ptr.row];
				temp = null;
				ois.close();
				if(tuples[0][c[1].col].equals(c[1].val))
					matches.add(new ArrayWrapper(tuples));
			}
			}else if (icnt == 1){
				if(op.equalsIgnoreCase("or")){
					 for(int p = 0; p<=pageCounter; p++){
						 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(p))));
						 Page temp = (Page) ois.readObject();
						 matches.add(new ArrayWrapper(temp.select(c, op)));
						 temp = null;
						 ois.close();
					 }
				}else{
			
					tuples = new Object[1][headers.length];
					String key = "";
					int loc = 0;
					for(int k = 0; k<c.length; k++)
						if(indexedCols.contains(headers[c[k].col].label)){
							key = headers[c[0].col].label;
							loc = k;
						}
					Pointer ptr =indexing.getFromIndex(key, (Comparable) query.get(key));
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptr.pageIndex))));
					Page temp = (Page) ois.readObject();
					tuples[0] = temp.data[ptr.row];
					temp = null;
					ois.close();
					if(tuples[0][c[1-loc].col].equals(c[1-loc].val))
						matches.add(new ArrayWrapper(tuples));
				}
			}
			else{ //contains neither indexed	
				
				 for(int p = 0; p<=pageCounter; p++){
					 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(p))));
					 Page temp = (Page) ois.readObject();
					 matches.add(new ArrayWrapper(temp.select(c, op)));
					 temp = null;
					 ois.close();
				 }
		 
	}
			return matches.iterator();
	}
		 
	
	
	/**
	 * Deletes from the table based on the input query and the operator. It behaves like select
	 * but instead of returning the matches, it deletes.
	 * @param query
	 * @param op
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws DeleteFromEmptyTableException
	 * @throws InvalidOperatorException 
	 */
	public void delete(Hashtable<String,Object> query, String op) throws FileNotFoundException, IOException
	, ClassNotFoundException, DeleteFromEmptyTableException, InvalidOperatorException{
		if(isEmpty())
			throw new DeleteFromEmptyTableException("Attempting to delete from " + this.name 
					+ "while it's empty");
		if(!(op.equalsIgnoreCase("and")||op.equalsIgnoreCase("or"))){
			throw new InvalidOperatorException();
		}


		Criteria[] c = new Criteria[query.size()];
		int i = 0;
		for(String itm : query.keySet()){
			for(int j = 0; j<headers.length; j++){
				if(headers[j].label.equals(itm)){
					Criteria t = new Criteria();
					t.col = j;
					t.val = query.get(itm);
					c[i++] = t;
					break;
				}
			}
		}
		int icnt = 0;  //index counter
		for(int o = 0; o<c.length; o++)
			if(indexedCols.contains(headers[c[o].col].label))
				icnt++;
		//			ArrayList<Pointer>[]indexedColRes=new ArrayList[2]; //since 2 columns at max to query
		Pointer[] ptrs = new Pointer[2];
		Object[][] tuples;
		if(icnt == 2){

			if(op.equalsIgnoreCase("or")){
				//					tuples = new Object[2][headers.length];
				//					int o = 0;
				for(String itm :query.keySet()){
					Pointer ptr =indexing.deleteInIndex(itm, (Comparable) query.get(itm));
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptr.pageIndex))));
					Page temp = (Page) ois.readObject();
					temp.delete(ptr.row);
					ois.close();
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(ptr.pageIndex))));
					oos.writeObject(temp);
					oos.flush();
					oos.close();
					temp = null;
				}

			}else{ //and
				//				Iterator keys=query.keySet().iterator();
				//				String key=(String) keys.next();
				tuples = new Object[1][headers.length];
				String key = headers[c[0].col].label;
				Pointer ptr=indexing.getFromIndex(key, (Comparable) query.get(key));
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptr.pageIndex))));
				Page temp = (Page) ois.readObject();
				tuples[0] = temp.data[ptr.row];
				ois.close();
				if(tuples[0][c[1].col].equals(c[1].val)){
					temp.delete(ptr.row);
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(ptr.pageIndex))));
					oos.writeObject(temp);
					oos.flush();
					oos.close();
					temp = null;
				}

			}
		}else if (icnt == 1){ //if only 1 indexed
			if(op.equalsIgnoreCase("or")){
				for(int p = 0; p<=pageCounter; p++){
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(p))));
					Page temp = (Page) ois.readObject();
					ois.close();
					temp.delete(c, op);
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(p))));
					oos.writeObject(temp);
					oos.flush();
					oos.close();
					temp = null;
				}

			}else{
				tuples = new Object[1][headers.length];
				String key = "";
				int loc = 0;
				for(int k = 0; k<c.length; k++)
					if(indexedCols.contains(headers[c[k].col].label)){
						key = headers[c[0].col].label;
						loc = k;
					}
				Pointer ptr =indexing.getFromIndex(key, (Comparable) query.get(key));
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptr.pageIndex))));
				Page temp = (Page) ois.readObject();
				tuples[0] = temp.data[ptr.row];
				ois.close();
				if(tuples[0][c[1-loc].col].equals(c[1-loc].val)){
					//						matches.add(new ArrayWrapper(tuples));
					temp.delete(ptr.row);
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(ptr.pageIndex))));
					oos.writeObject(temp);
					oos.flush();
					oos.close();
					temp = null;
				}
			}
		} else{ // if neither indexed
			for(int p = 0; p<=pageCounter; p++){
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(p))));
				Page temp = (Page) ois.readObject();
				ois.close();
				temp.delete(c, op);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(p))));
				oos.writeObject(temp);
				oos.flush();
				oos.close();
				temp = null;
			}
		}
	}
	
	/**
	 * sets values to the header array and writes the metadata to the "metadata.csv" file. 
	 * @throws IOException 
	 */
	private void MakeMetaData() throws IOException{
		String[] mData = new String[6];
		int m = 0;
		for(String itm : this.htblColNameType.keySet()){
			String colName = itm;
			String colType = "";
			String key = itm.equals(this.strKeyColName) ? "True":"False";
			if(key.equals("True")){
				PrimKeyIndex=m;
			}
			headers[m] = new Schema();
			headers[m].label = colName;
			String indexed = key;
			String references = "null";
			String curr = "";
			//Handling colType name
			switch(htblColNameType.get(itm)){
			case "Integer":
				colType = "java.lang.Integer";
				break;
			case "String":
				colType = "java.lang.String";
				break;
			case "Double":
				colType = "java.lang.Double";
				break;
			case "Date":
				colType = "java.util.Date";
				break;
			case "Boolean":
				colType = "java.lang.Boolean";
				break;
			}

			headers[m++].type = colType;
			if(htblColNameRefs.containsKey(colName))
				references = htblColNameRefs.get(colName);
			
			curr = this.name + ", " + colName + ", " + colType + ", " + key + ", " + indexed + ", "
					+ references + "\n"; // useless since open csv takes a string array anyway.
			mData = curr.split(","); //too lazy to make a string array by myself again.
			FileWriter fw = new FileWriter("data\\metadata.csv", true);
			fw.append(name); fw.append(",");
			fw.append(colName); fw.append(",");
			fw.append(colType); fw.append(",");
			fw.append(key); fw.append(",");
			fw.append(indexed); fw.append(",");
			fw.append(references); fw.append(",");
			fw.append("\n");
//			CSVWriter writer = new CSVWriter(new FileWriter("data\\metadata.csv"), '\n');
//			writer.writeNext(mData);
//			writer.close();
			fw.flush();
			fw.close();
		}
		headers[headers.length-1] = new Schema();
		headers[headers.length-1].label = "TouchDate";
		headers[headers.length-1].type = "Date";
//		CSVWriter writer = new CSVWriter(new FileWriter("data\\metadata.csv"), '\n');
//		writer.writeNext(mData);
//		writer.close();
		
		
	}
	/**
	 * utility method that just returns true if key is found in header (column name is found in table)
	 * @param String key
	 * @return Boolean
	 */
	public boolean ColNameIsFound(String key){
		for(int i=0;i<headers.length;i++){
			if(headers[i].label.equals(key))
				return true;
		}
		return false;
		
	}
	/**
	 * This method is used to update an entry for the DBApp
	 * @param strKey
	 * @param htblColNameValue
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws DBEngineException
	 */
	
	public void update(Object strKey, Hashtable<String,Object> htblColNameValue) throws ClassNotFoundException, IOException, DBEngineException{
//OLD IMPLEMENTATION WITHOUT INDEX
//		for (int i=0 ; i<=pageCounter;i++){
//			 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(i))));
//			 Page p = (Page) ois.readObject();
//			 ois.close();
//			 int res=p.searchPrimayKey(strKey, PrimKeyIndex);
//			 if(res!=-1){
//				 p.updateEntry(res,headers,htblColNameValue);
//				 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(i))));
//				 oos.writeObject(p);
//				 oos.flush();
//				 oos.close();
//				 p=null;
//				 return;
//			 }
//		}
//		throw new EntryNotFoundException("Entry for primary key: "+ this.strKeyColName+"= "+strKey+" not Found!");
		
		//WITH INDEX
		Pointer ptr=indexing.getFromIndex(strKeyColName, (Comparable) strKey);
		if(ptr==null) throw new EntryNotFoundException("Entry for primary key: "+ this.strKeyColName+"= "+strKey+" not Found!");
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(ptr.pageIndex))));
		 Page p = (Page) ois.readObject();
		 ois.close();
		 p.updateEntry(ptr.row,headers,htblColNameValue);
		 //Updates Index key if the change or update was on the PK itself
		 if(htblColNameValue.containsKey(strKeyColName)){
			 indexing.updateInIndex(strKeyColName, (Comparable) strKey, (Comparable) htblColNameValue.get(strKeyColName));
			 ObjectOutputStream oos= new ObjectOutputStream(new FileOutputStream(new File("classes\\es_que_l\\" + name + "Index" + ".class")));
				oos.writeObject(indexing);
			 oos.flush();
			 oos.close();
		 }
		 
		 ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(pages.get(ptr.pageIndex))));
		 oos.writeObject(p);
		 oos.flush();
		 oos.close();
		 p=null;
	}
	
	
	public void createIndex(String colName) throws NonExistantColumnException, FileNotFoundException, IOException, ClassNotFoundException, AlreadyExistantIndexException{
		if(indexedCols.contains(colName))
			throw new AlreadyExistantIndexException("Index for column "+colName+" already exists");
		String type=null;
		int pos = 0;
		for(int i = 0; i<headers.length; i++){
			if(colName.equals(headers[i].label)){
				type = headers[i].type;
				pos=i;
				break;
			}
		}
		if(type==null)
			throw new NonExistantColumnException("Column "+colName+" doesn't exist");
		
		indexedCols.add(colName);
		indexing.initilizeIndex(colName, type.substring("java.lang.".length()));
		for (int i=0 ; i<=pageCounter;i++){
			 ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(i))));
			 Page p = (Page) ois.readObject();
			 ois.close();
			 for(int j=0;j<=p.pointer;j++){
				 Comparable key= (Comparable) p.data[j][pos];
				 Pointer ptr=new Pointer(i,j);
				 indexing.insertInIndex(colName, key, ptr);
			 }
		}
		ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream(new File("classes\\es_que_l\\" + name + "Index" + ".class")));
		oos2.writeObject(indexing);
		oos2.flush();
		oos2.close();
	}


	public String getStrKeyColName() {
		return strKeyColName;
	}


	public void setStrKeyColName(String strKeyColName) {
		this.strKeyColName = strKeyColName;
	}
	/**
	 * Checks whether the table is empty, by checking if it only has one page and this page is empty.
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public boolean isEmpty() throws FileNotFoundException, IOException, ClassNotFoundException{
		if(pageCounter == 0){
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(pages.get(pageCounter))));
			Page temp = (Page) ois.readObject();
			boolean x = temp.isEmpty();
			temp = null;
			return x;
		}
		return false;
	}


//	public static void main(String[] args){
//		
//		Hashtable<String, Object> test = new Hashtable<String, Object>();
//		test.put("a", 1);
//		test.put("b", 2);
//		test.put("c", 3);
//		test.put("d", 4);
//		ArrayList<Integer> x = new ArrayList<Integer>();
//	
//		Object[] in = new Object[4];
//		tableToArray(in, test);
//		System.out.println(Arrays.toString(in));
//	}
	
}
