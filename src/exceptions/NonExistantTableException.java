package exceptions;
/**
 * Exception for when the user tries to insert , select or access a non existent table
 */
public class NonExistantTableException extends DBAppException {
	public NonExistantTableException(String message){
		super(message);
	}
}
