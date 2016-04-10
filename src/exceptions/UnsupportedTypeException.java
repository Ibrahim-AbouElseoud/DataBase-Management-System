package exceptions;

import exceptions.DBAppException;

/**
 *Thrown when attempting to create a table with unsupported data types.
 * @author Joe3141
 *
 */
public class UnsupportedTypeException extends DBAppException{
	
	public UnsupportedTypeException(String message) {
		super(message);
	}

}