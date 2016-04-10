package exceptions;
/**
 * Exception for when the user tries to update or delete a non existant entry
 */
public class EntryNotFoundException extends DBEngineException {
	public EntryNotFoundException(String message){
		 super(message);
		 }
}
