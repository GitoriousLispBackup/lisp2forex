package lisp2forex.handlers.formatters;

public class FormatterFactory {

	public static Formatter getDefaultInstance(){
		return new MyLispFormatter();
	}
}
