package exceptions;
/**
 * Exception for when the user tries to create an already existent table
 * 
 *
 */
public class TableAlreadyExistsException extends DBAppException{
	public TableAlreadyExistsException(String message){
		super(message);
	}
}
