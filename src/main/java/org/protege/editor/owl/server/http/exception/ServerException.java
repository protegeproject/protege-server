package org.protege.editor.owl.server.http.exception;

public class ServerException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2081045061970691687L;
	protected final int mErrorCode;

	public ServerException() {
		this(500);   // borrowing from HTTP status code 500
	}

	public ServerException(final int theErrorCode) {
		super();
		mErrorCode = theErrorCode;
	}

	public ServerException(final int theErrorCode, final String theMessage) {
		super(theMessage);
		mErrorCode = theErrorCode;
	}

	public ServerException(final int theErrorCode, final String theMessage, final Throwable theCause) {
		super(theMessage, theCause);
		mErrorCode = theErrorCode;
	}

	public ServerException(final int theErrorCode, final Throwable theCause) {
		super(theCause);
		mErrorCode = theErrorCode;
	}

	public int getErrorCode() {
		return mErrorCode;
	}
}
