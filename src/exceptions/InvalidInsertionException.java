package exceptions;

import exceptions.DBEngineException;

/**
 * Thrown when inserting in a column with a different type than what the schema of the table 
 * defines and thus violating the schema.
 * @author Joe3141
 *
 */
public class InvalidInsertionException extends DBEngineException{

	public InvalidInsertionException(String message) {
		super(message);
		
	}

}
