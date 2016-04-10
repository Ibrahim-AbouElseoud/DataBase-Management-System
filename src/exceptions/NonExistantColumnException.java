package exceptions;
/**
 * Exception for when the user tries to insert , select or access a non existent table
 */
public class NonExistantColumnException extends DBAppException {
	public NonExistantColumnException(String message){
		super(message);
	}
}
