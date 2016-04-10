package es_que_l;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import exceptions.*;


//class DBAppException extends Exception {
//	public DBAppException(String message){
//		super(message);
//	}
//}										//Moved to the exceptions package
//
//class DBEngineException extends Exception {
//	public DBEngineException(String message){
//		super(message);
//	}
//
//}


public class DBApp {
	Hashtable<String, Table> tables;
	String tblFile = "classes\\es_que_l\\tables.class";

	@SuppressWarnings("unchecked")
	public void init( ){
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(tblFile)));
			tables = (Hashtable<String, Table>) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			tables = new Hashtable<String, Table>();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(tblFile)));
				oos.writeObject(tables);
				oos.flush();
				oos.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void createTable(String strTableName, Hashtable<String,String> htblColNameType, 
			Hashtable<String,String> htblColNameRefs, String strKeyColName)  throws DBAppException, FileNotFoundException, IOException{
		//
		    	if(!tables.isEmpty())
		    	if (tables.containsKey(strTableName)) { //TODO: either get out exception or print table already exists and don't continue
		 			throw new TableAlreadyExistsException("Table " + strTableName+ " Already Exists in database!");
		 		}
		for(String itm : htblColNameType.values()){
			if(!(itm.equalsIgnoreCase("Integer")|| itm.equalsIgnoreCase("String") || itm.equalsIgnoreCase("Double") || itm.equalsIgnoreCase("Boolean")))
				throw new UnsupportedTypeException("The type " + itm + "is unsupported");	
		}
		
//		    	for(String value:htblColNameRefs.values()){
//		    		String [] a=value.split("\\.");
//		    		if(!htblColNameRefs.isEmpty())
//		    		if(tables.containsKey(a[0])){
//		    			if(tables.get(a[0]).getStrKeyColName()!=a[1]);
//		    			throw new NotPrimaryKeyRefrenceException("You must refrence this tables Primary key !");
//		    		}else throw new NonExistantTableException("Table " + a[0]+ " Does not Exist in database!");
//		    		
//		    	}

		Table t = new Table(strTableName, htblColNameType, htblColNameRefs, strKeyColName);
		tables.put(strTableName, t);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(tblFile)));
		oos.writeObject(tables);
		oos.flush();
		oos.close();
	}

	public void createIndex(String strTableName, String strColName)  throws DBAppException,DBEngineException, FileNotFoundException, ClassNotFoundException, IOException{
		Table t = tables.get(strTableName);
		t.createIndex(strColName);
	}

	public void insertIntoTable(String strTableName, Hashtable<String,Object> htblColNameValue)  throws DBAppException, FileNotFoundException, IOException, ClassNotFoundException, InvalidInsertionException, DBEngineException{
		if (!tables.containsKey(strTableName)) {
			throw new NonExistantTableException("Table " + strTableName+ " Does not Exist in database!");
		}
		for(Object itm : htblColNameValue.values()){
			if(!(itm instanceof Integer|| itm instanceof String || itm instanceof Double || itm instanceof Boolean))
				throw new UnsupportedTypeException("The type " + itm + "is unsupported");
		}
		Table t = tables.get(strTableName);
		for(String colName: htblColNameValue.keySet())
			if(!t.ColNameIsFound(colName))
				throw new NonExistantColumnException("Sorry, column: "+colName+" does not exist in this table!");	
		t.insert(htblColNameValue);
	}

	public void updateTable(String strTableName, Object strKey,
			Hashtable<String,Object> htblColNameValue)  throws DBAppException, ClassNotFoundException, IOException, DBEngineException{
		if (!tables.containsKey(strTableName)) {
			throw new NonExistantTableException("Table " + strTableName+ " Does not Exist in database!");
		}
		Table t = tables.get(strTableName);
		for(String colName: htblColNameValue.keySet())
			if(!t.ColNameIsFound(colName))
				throw new NonExistantColumnException("Sorry, column: "+colName+" does not exist in this table!");
		t.update(strKey,htblColNameValue); // key is Col name and value is the value we want
		System.out.println("Update was Succesful");					// strKey is PK column it's entry is what we want to find
	}


	public void deleteFromTable(String strTableName, Hashtable<String,Object> htblColNameValue, 
			String strOperator) throws DBEngineException, FileNotFoundException, ClassNotFoundException, IOException{
		Table t = tables.get(strTableName);
		t.delete(htblColNameValue, strOperator);
		System.out.println("Deletion was Succesful");
	}

	public Iterator selectFromTable(String strTable,  Hashtable<String,Object> htblColNameValue, 
			String strOperator) throws DBEngineException, FileNotFoundException, ClassNotFoundException, IOException, DBAppException, DBEngineException{
		if (!tables.containsKey(strTable)) {
			throw new NonExistantTableException("Table " + strTable+ " Does not Exist in database!");
		}
		Table t = tables.get(strTable);
		return t.select(htblColNameValue, strOperator);
	}


	public static void main(String [] args) throws DBAppException, DBEngineException, FileNotFoundException, IOException, ClassNotFoundException {

		// create a new DBApp

		DBApp myDB = new DBApp();

		// initialize it
		myDB.init();

		// creating table "Faculty"

		Hashtable<String, String> fTblColNameType = new Hashtable<String, String>();
		fTblColNameType.put("ID", "Integer");
		fTblColNameType.put("Name", "String");

		Hashtable<String, String> fTblColNameRefs = new Hashtable<String, String>();

		myDB.createTable("Faculty", fTblColNameType, fTblColNameRefs, "ID");


		/*	///////////////////////////////Exceptions start/////////////////////	
		//table already exists error
		Hashtable<String, String> lTblColNameType = new Hashtable<String, String>();
		lTblColNameType.put("ID", "Integer");
		lTblColNameType.put("Name", "String");

		Hashtable<String, String> lTblColNameRefs = new Hashtable<String, String>();

		myDB.createTable("Faculty", lTblColNameType, lTblColNameRefs, "ID");
		// non existent column error
		Hashtable<String,Object> l1tblColNameValue1 = new Hashtable<String,Object>();
		l1tblColNameValue1.put("ID", Integer.valueOf( "1" ) );
		l1tblColNameValue1.put("age", "Media Engineering and Technology");
		myDB.insertIntoTable("Faculty", l1tblColNameValue1);
		// not pk reference error
		Hashtable<String, String> m1TblColNameType = new Hashtable<String, String>();
		m1TblColNameType.put("ID", "Integer");
		m1TblColNameType.put("Name", "String");
		m1TblColNameType.put("Faculty_ID", "Integer");	

		Hashtable<String, String> m1TblColNameRefs = new Hashtable<String, String>();
		m1TblColNameRefs.put("Faculty_ID", "Faculty.Name");

		myDB.createTable("Major", m1TblColNameType, m1TblColNameRefs, "ID");
		//Referencing non existent table
		Hashtable<String, String> m2TblColNameType = new Hashtable<String, String>();
		m2TblColNameType.put("ID", "Integer");
		m2TblColNameType.put("Name", "String");
		m2TblColNameType.put("Faculty_ID", "Integer");	

		Hashtable<String, String> m2TblColNameRefs = new Hashtable<String, String>();
		m2TblColNameRefs.put("Faculty_ID", "Matter.ID");

		myDB.createTable("Major", m1TblColNameType, m2TblColNameRefs, "ID");
		//invalid operator exception
		Hashtable<String,Object> st1blColNameValue = new Hashtable<String,Object>();
		st1blColNameValue.put("ID", Integer.valueOf( "550" ) );
		st1blColNameValue.put("Age", Integer.valueOf( "20" ) );
		 myDB.selectFromTable("Student", st1blColNameValue,"NOT");
		 //non existant table
		Hashtable<String,Object> st2blColNameValue = new Hashtable<String,Object>();
		st2blColNameValue.put("ID", Integer.valueOf( "550" ) );
		st2blColNameValue.put("Age", Integer.valueOf( "20" ) );
		myDB.selectFromTable("Matter", st2blColNameValue,"AND");

		//unsupported type exception
		Hashtable<String, String> l2TblColNameType = new Hashtable<String, String>();
		l2TblColNameType.put("ID", "Integer");
		l2TblColNameType.put("Name", "VARCHAR");
		Hashtable<String, String> l2TblColNameRefs = new Hashtable<String, String>();
		myDB.createTable("Faculty", l2TblColNameType, lTblColNameRefs, "ID");
		// incorrect insertion type
		Hashtable<String,Object> l3tblColNameValue1 = new Hashtable<String,Object>();
		l3tblColNameValue1.put("ID", Integer.valueOf( "1" ) );
		l3tblColNameValue1.put("name", new Date());
		myDB.insertIntoTable("Faculty", l3tblColNameValue1);
		//duplicate primary key 

		//update , entry not found exception
		Hashtable<String,Object> l4tblColNameValue1 = new Hashtable<String,Object>();
		l4tblColNameValue1.put("ID", Integer.valueOf( "4" ) );
		l4tblColNameValue1.put("name", new Date());
		myDB.updateTable("Faculty", "1", l4tblColNameValue1); //TODO: SIGNATURE OF ID SHOULD BE OBJECT !!!!!! ??????
		//update test


    /////////////////////////////////Exceptions END//////////////////////////
		 */	


		// creating table "Major"

		Hashtable<String, String> mTblColNameType = new Hashtable<String, String>();
		mTblColNameType.put("ID", "Integer");
		mTblColNameType.put("Name", "String");
		mTblColNameType.put("Faculty_ID", "Integer");	

		Hashtable<String, String> mTblColNameRefs = new Hashtable<String, String>();
		mTblColNameRefs.put("Faculty_ID", "Faculty.ID");

		myDB.createTable("Major", mTblColNameType, mTblColNameRefs, "ID");

		// creating table "Course"

		Hashtable<String, String> coTblColNameType = new Hashtable<String, String>();
		coTblColNameType.put("ID", "Integer");
		coTblColNameType.put("Name", "String");
		coTblColNameType.put("Code", "String");
		coTblColNameType.put("Hours", "Integer");
		coTblColNameType.put("Semester", "Integer");
		coTblColNameType.put("Major_ID", "Integer");

		Hashtable<String, String> coTblColNameRefs = new Hashtable<String, String>();
		coTblColNameRefs.put("Major_ID", "Major.ID");

		myDB.createTable("Course", coTblColNameType, coTblColNameRefs, "ID");

		// creating table "Student"

		Hashtable<String, String> stTblColNameType = new Hashtable<String, String>();
		stTblColNameType.put("ID", "Integer");
		stTblColNameType.put("First_Name", "String");
		stTblColNameType.put("Last_Name", "String");
		stTblColNameType.put("GPA", "Double");
		stTblColNameType.put("Age", "Integer");

		Hashtable<String, String> stTblColNameRefs = new Hashtable<String, String>();

		myDB.createTable("Student", stTblColNameType, stTblColNameRefs, "ID");

		// creating table "Student in Course"

		Hashtable<String, String> scTblColNameType = new Hashtable<String, String>();
		scTblColNameType.put("ID", "Integer");
		scTblColNameType.put("Student_ID", "Integer");
		scTblColNameType.put("Course_ID", "Integer");

		Hashtable<String, String> scTblColNameRefs = new Hashtable<String, String>();
		scTblColNameRefs.put("Student_ID", "Student.ID");
		scTblColNameRefs.put("Course_ID", "Course.ID");

		myDB.createTable("Student_in_Course", scTblColNameType, scTblColNameRefs, "ID");

		// insert in table "Faculty"

		Hashtable<String,Object> ftblColNameValue1 = new Hashtable<String,Object>();
		ftblColNameValue1.put("ID", Integer.valueOf( "1" ) );
		ftblColNameValue1.put("Name", "Media Engineering and Technology");
		myDB.insertIntoTable("Faculty", ftblColNameValue1);

		Hashtable<String,Object> ftblColNameValue2 = new Hashtable<String,Object>();
		ftblColNameValue2.put("ID", Integer.valueOf( "2" ) );
		ftblColNameValue2.put("Name", "Management Technology");
		myDB.insertIntoTable("Faculty", ftblColNameValue2);

		for(int i=1;i<=1000;i++)
		{
			Hashtable<String,Object> ftblColNameValueI = new Hashtable<String,Object>();
			ftblColNameValueI.put("ID", Integer.valueOf( (""+(i+2)) ) );
			ftblColNameValueI.put("Name", "f"+(i+2));
			myDB.insertIntoTable("Faculty", ftblColNameValueI);
		}

		// insert in table "Major"

		Hashtable<String,Object> mtblColNameValue1 = new Hashtable<String,Object>();
		mtblColNameValue1.put("ID", Integer.valueOf( "1" ) );
		mtblColNameValue1.put("Name", "Computer Science & Engineering");
		mtblColNameValue1.put("Faculty_ID", Integer.valueOf( "1" ) );
		myDB.insertIntoTable("Major", mtblColNameValue1);

		Hashtable<String,Object> mtblColNameValue2 = new Hashtable<String,Object>();
		mtblColNameValue2.put("ID", Integer.valueOf( "2" ));
		mtblColNameValue2.put("Name", "Business Informatics");
		mtblColNameValue2.put("Faculty_ID", Integer.valueOf( "2" ));
		myDB.insertIntoTable("Major", mtblColNameValue2);

		for(int i=1;i<=1000;i++)
		{
			Hashtable<String,Object> mtblColNameValueI = new Hashtable<String,Object>();
			mtblColNameValueI.put("ID", Integer.valueOf( (""+(i+2) ) ));
			mtblColNameValueI.put("Name", "m"+(i+2));
			mtblColNameValueI.put("Faculty_ID", Integer.valueOf( (""+(i+2) ) ));
			myDB.insertIntoTable("Major", mtblColNameValueI);
		}


		// insert in table "Course"

		Hashtable<String,Object> ctblColNameValue1 = new Hashtable<String,Object>();
		ctblColNameValue1.put("ID", Integer.valueOf( "1" ) );
		ctblColNameValue1.put("Name", "Data Bases I");
		ctblColNameValue1.put("Code", "CSEN 604");
		ctblColNameValue1.put("Hours", Integer.valueOf( "4" ));
		ctblColNameValue1.put("Semester", Integer.valueOf( "6" ));
		ctblColNameValue1.put("Major_ID", Integer.valueOf( "1" ));
		myDB.insertIntoTable("Course", ctblColNameValue1);

		Hashtable<String,Object> ctblColNameValue2 = new Hashtable<String,Object>();
		ctblColNameValue2.put("ID", Integer.valueOf( "2" ) );
		ctblColNameValue2.put("Name", "Data Bases II");
		ctblColNameValue2.put("Code", "CSEN 604");
		ctblColNameValue2.put("Hours", Integer.valueOf( "4" ) );
		ctblColNameValue2.put("Semester", Integer.valueOf( "6" ) );
		ctblColNameValue2.put("Major_ID", Integer.valueOf( "2" ) );
		myDB.insertIntoTable("Course", ctblColNameValue2);

		for(int i=1;i<=1000;i++)
		{
			Hashtable<String,Object> ctblColNameValueI = new Hashtable<String,Object>();
			ctblColNameValueI.put("ID", Integer.valueOf( ( ""+(i+2) )));
			ctblColNameValueI.put("Name", "c"+(i+2));
			ctblColNameValueI.put("Code", "co "+(i+2));
			ctblColNameValueI.put("Hours", Integer.valueOf( "4" ) );
			ctblColNameValueI.put("Semester", Integer.valueOf( "6" ) );
			ctblColNameValueI.put("Major_ID", Integer.valueOf( ( ""+(i+2) )));
			myDB.insertIntoTable("Course", ctblColNameValueI);
		}

		// insert in table "Student"

		for(int i=0;i<1000;i++)
		{
			Hashtable<String,Object> sttblColNameValueI = new Hashtable<String,Object>();
			sttblColNameValueI.put("ID", Integer.valueOf( ( ""+i ) ) );
			sttblColNameValueI.put("First_Name", "FN"+i);
			sttblColNameValueI.put("Last_Name", "LN"+i);
			sttblColNameValueI.put("GPA", Double.valueOf( "0.7" ) ) ;
			sttblColNameValueI.put("Age", Integer.valueOf( "20" ) );
			myDB.insertIntoTable("Student", sttblColNameValueI);
			//changed it to student instead of course
		}
		// update     ############################ 7amada was here############################
		Hashtable<String,Object> l4tblColNameValue1 = new Hashtable<String,Object>();
		l4tblColNameValue1.put("First_Name","7amada" );
		l4tblColNameValue1.put("GPA", Integer.valueOf(2));
		myDB.updateTable("Student", Integer.valueOf( "550" ), l4tblColNameValue1);

		//delete       3############################ WE DELETED SOMETHING HERE############################
		Hashtable<String,Object> l5tblColNameValue1 = new Hashtable<String,Object>();
		l5tblColNameValue1.put("Name","m7" );
		myDB.deleteFromTable("Major", l5tblColNameValue1 ,"OR");


		// selecting


		Hashtable<String,Object> stblColNameValue = new Hashtable<String,Object>();
		stblColNameValue.put("ID", Integer.valueOf( "550" ) );
		stblColNameValue.put("Age", Integer.valueOf( "20" ) );

		long startTime = System.currentTimeMillis();
		Iterator myIt = myDB.selectFromTable("Student", stblColNameValue,"AND");
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("searching on one indexed and other not ,totalTime:"+totalTime);
		while(myIt.hasNext()) {
			System.out.println(myIt.next());
		}

		// feel free to add more tests
		Hashtable<String,Object> stblColNameValue3 = new Hashtable<String,Object>();
		stblColNameValue3.put("Name", "m7");
		stblColNameValue3.put("Faculty_ID", Integer.valueOf( "7" ) );

		long startTime2 = System.currentTimeMillis();
		Iterator myIt2 = myDB.selectFromTable("Major", stblColNameValue3,"AND");
		long endTime2   = System.currentTimeMillis();
		long totalTime2 = endTime2 - startTime2;
		System.out.println("Searching on both non indexed , totalTime:"+totalTime2);
		System.out.println("Note it doesn't have m7 since that entry was deleted above so result is empty");
		while(myIt2.hasNext()) {
			System.out.println(myIt2.next());
		}

		//Creating new index
		myDB.createIndex("Course", "Name");
		
		//searching on neither indexed
				Hashtable<String,Object> stblColNameValue6 = new Hashtable<String,Object>();
				stblColNameValue6.put("Code", "co 200" );
				stblColNameValue6.put("Hours", Integer.valueOf( "4" ) );
				long startTime5 = System.currentTimeMillis();
				Iterator myIt5 = myDB.selectFromTable("Course", stblColNameValue6,"AND");
				long endTime5   = System.currentTimeMillis();
				long totalTime5 = endTime5 - startTime5;
				System.out.println("searching on neither indexed ,totalTime: "+totalTime5);
				while(myIt5.hasNext()) {
					System.out.println(myIt5.next());
				}
				
		

		//searching on one indexed and other not
		Hashtable<String,Object> stblColNameValue5 = new Hashtable<String,Object>();
		stblColNameValue5.put("Major_ID", Integer.valueOf( "40" ));
		stblColNameValue5.put("ID", Integer.valueOf( "500" ) );
		long startTime4 = System.currentTimeMillis();
		Iterator myIt4 = myDB.selectFromTable("Course", stblColNameValue5,"AND");
		long endTime4   = System.currentTimeMillis();
		long totalTime4 = endTime4 - startTime4;
		System.out.println("searching on one indexed and other not (and doing AND) ,totalTime: "+totalTime4);
		System.out.println("Note: might be faster than searching on both indexed due to geting one indexed only and then seeing if it satisfies other condition for AND (optimization");
		System.out.println("Empty since there is no entry of Major_ID=40 AND ID=500");
		while(myIt4.hasNext()) {
			System.out.println(myIt4.next());
		}
		
		//searching on new index
				Hashtable<String,Object> stblColNameValue4 = new Hashtable<String,Object>();
				stblColNameValue4.put("Name", "c7");
				stblColNameValue4.put("ID", Integer.valueOf( "500" ) );
				long startTime3 = System.currentTimeMillis();
				Iterator myIt3 = myDB.selectFromTable("Course", stblColNameValue4,"OR");
				long endTime3   = System.currentTimeMillis();
				long totalTime3 = endTime3 - startTime3;
				System.out.println("searching on both indexed ,totalTime: "+totalTime3);
				while(myIt3.hasNext()) {
					System.out.println(myIt3.next());
				}
				
				
		
	}


}
