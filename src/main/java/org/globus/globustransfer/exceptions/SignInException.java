package org.globus.globustransfer.exceptions;

public class SignInException extends RuntimeException {

	private static final long serialVersionUID = -4479584156426399530L;

	public SignInException(String message) {
		super("Sign In Exception: " + message);
	}

}
