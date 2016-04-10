package exceptions;

import exceptions .DBEngineException;

/**
 * Thrown when attempting to delete from an empty table. 
 * @author Joe3141
 *
 */
public class DeleteFromEmptyTableException extends DBEngineException{

	public DeleteFromEmptyTableException(String message) {
		super(message);
	}
	
}
