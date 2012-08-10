package lisp2forex.handlers;

public class UnknownCommandException extends Exception {
	
	public UnknownCommandException(String message) {
		super(message);
	}
	
	public UnknownCommandException() {
		super();
	}

	private static final long serialVersionUID = 1L;

}
