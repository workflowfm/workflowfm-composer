package com.workflowfm.composer.exceptions;

/** An exception that represents an error that should be reported to the user. */
public class UserError extends Exception
{
	private static final long serialVersionUID = -8434323793060705016L;

	public UserError(String error)
	{
		super(error);
	}

	public UserError(String error, Throwable cause)
	{
		super(error, cause);
	}

}
