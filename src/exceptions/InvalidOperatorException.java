package exceptions;
/**
 * Exception for when the user uses non supported Operators other than OR or AND
 */
public class InvalidOperatorException extends DBEngineException {
 public InvalidOperatorException(){
 super("Invalid Operator: only possilbe operators OR and AND");
 }
}
