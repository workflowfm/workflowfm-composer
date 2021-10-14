package piviz.exceptions;


/**
 * Exceptions occuring during parsing.
 * @author Anja
 *
 */
public class PiParserError extends Error{
	public PiParserError(String message) {
		super(message);
	}
}
