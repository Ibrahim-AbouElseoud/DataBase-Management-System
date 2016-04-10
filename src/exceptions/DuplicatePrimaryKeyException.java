package exceptions;

import exceptions.DBEngineException;

/**
 * Thrown when attempting to insert a tuple with an existing primary key to preserve the
 * integrity constraints. 
 * @author Joe3141
 *
 */
public class DuplicatePrimaryKeyException extends DBEngineException{

	public DuplicatePrimaryKeyException(String message) {
		super(message);
		
	}

}
