package exceptions;
/**
 * Exception for when the user tries to reference a column in the table that isn't the primary key
 */
public class NotPrimaryKeyRefrenceException extends DBAppException {
public NotPrimaryKeyRefrenceException(String message){
	super(message);
}
}
