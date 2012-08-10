package lisp2forex.handlers.info;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.ConnectionHandler;
import lisp2forex.handlers.formatters.FormatterFactory;

import org.apache.log4j.Logger;

public class AccountingInfoConnectionHandler extends ConnectionHandler {

	public AccountingInfoConnectionHandler(Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger){
		this(true, clientSocket, input, output, messenger);
	}
	
	public AccountingInfoConnectionHandler(boolean listen, Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger) {
		super(listen, null, clientSocket, input, output, messenger, Logger
				.getLogger(AccountingInfoConnectionHandler.class));

	}
	
	
	@Override
	protected boolean handle() throws Exception {
		if (first){
			output.println(FormatterFactory.getDefaultInstance().format(publisher.context.getAccount()));
			first = false;
			if (!listen) return false;
		}
		logger.debug("waiting for account update.");

		String result = null;
		synchronized (publisher.accountMonitor) {
			publisher.accountMonitor.wait();
			result = formatter.format(publisher.account);
		}
		output.println(result);
		logger.debug("got account update.");

		return !output.checkError();
	}
	private boolean first = true;

}
