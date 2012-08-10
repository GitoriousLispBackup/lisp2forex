package lisp2forex.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.formatters.Formatter;
import lisp2forex.handlers.formatters.FormatterFactory;

import org.apache.log4j.Logger;

abstract public class ConnectionHandler extends Thread {

	protected final BufferedReader input;
	protected final PrintWriter output;
	protected final PublisherStrategy publisher;
	protected final Socket clientSocket;
	protected final Iterator<String> it;
	protected final boolean listen;
	
	protected final Logger logger;
	protected final static Formatter formatter = FormatterFactory.getDefaultInstance();
	
	public ConnectionHandler(Iterator<String> it, Socket clientSocket, BufferedReader input, PrintWriter output,
			PublisherStrategy messenger, Logger logger){
		this(true, it, clientSocket, input, output, messenger, logger);
	}
	
	public ConnectionHandler(boolean listen, Iterator<String> it, Socket clientSocket, BufferedReader input, PrintWriter output,
			PublisherStrategy messenger, Logger logger) {
		this.publisher = messenger;
		this.logger = logger;
		this.input = input;
		this.output = output;
		this.clientSocket = clientSocket;
		this.listen = listen;
		this.it = it;
	}

	@Override
	public void run() {
		try {
			if (listen)
			while (!isInterrupted() && handle());
			else handle();
		} catch (Exception e) {
			logger.error(e);
			output.println(FormatterFactory.getDefaultInstance().format(e));
		} finally {
			try {
				output.close();
				input.close();
			} catch (IOException e) {
				logger.error(e);
			} finally{
				try {
					clientSocket.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	abstract protected boolean handle() throws Exception ;
}