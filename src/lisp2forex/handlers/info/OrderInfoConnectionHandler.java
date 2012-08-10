package lisp2forex.handlers.info;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.ConnectionHandler;
import lisp2forex.handlers.formatters.FormatterFactory;

import org.apache.log4j.Logger;

import com.dukascopy.api.JFException;

public class OrderInfoConnectionHandler extends ConnectionHandler {

	public OrderInfoConnectionHandler(Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger) {
		this(true, clientSocket, input, output, messenger);
	}

	public OrderInfoConnectionHandler(boolean listen, Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger) {
		super(listen, null, clientSocket, input, output, messenger, Logger
				.getLogger(OrderInfoConnectionHandler.class));
	}

	@Override
	protected boolean handle() throws Exception {
		if (first) {
			try {
				output.println(FormatterFactory.getDefaultInstance().format(
						publisher.context.getEngine().getOrders()));
			} catch (JFException e) {
				logger.warn(e);
			}finally{
				first = false;
				if (!listen) return false;
			}
		}
		String result = null;
		synchronized (publisher.orderMonitor) {
			publisher.orderMonitor.wait();
			result = FormatterFactory.getDefaultInstance().format(
					publisher.context.getEngine().getOrders());

		}
		output.println(result);
		return !output.checkError();
	}

	private boolean first = true;

}
