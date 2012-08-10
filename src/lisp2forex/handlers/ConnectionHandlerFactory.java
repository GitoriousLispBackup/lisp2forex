package lisp2forex.handlers;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Iterator;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.formatters.FormatterFactory;
import lisp2forex.handlers.info.AccountingInfoConnectionHandler;
import lisp2forex.handlers.info.MessageInfoConnectionHandler;
import lisp2forex.handlers.info.OrderInfoConnectionHandler;
import lisp2forex.handlers.info.TickInfoConnectionHandler;
import lisp2forex.handlers.order.CloseCommandHandler;
import lisp2forex.handlers.order.SubmitOrderCommandHandler;

import org.apache.log4j.Logger;

public class ConnectionHandlerFactory {

	private static final Logger logger = Logger
			.getLogger(ConnectionHandlerFactory.class);

	public static void runInstance(Socket clientSocket, BufferedReader input,
			PrintWriter output, PublisherStrategy publisher) throws Exception {

		String[] commandAndArgs = input.readLine().toUpperCase().split("\\s+");
		final Iterator<String> it = Arrays.asList(commandAndArgs).iterator();
		String command = it.next();
		logger.info("command is " + command);
		if ("TICK".equals(command)) {
			new TickInfoConnectionHandler(false, it, clientSocket, input, output,
					publisher).start();

		} else if ("MESSAGE".equals(command)) {
			new MessageInfoConnectionHandler(false, clientSocket, input, output,
					publisher).start();

		} else if ("ACCOUNT".equals(command)) {
			new AccountingInfoConnectionHandler(false, clientSocket, input, output,
					publisher).start();

		} else if ("ORDERS".equals(command)) {
			new OrderInfoConnectionHandler(false, clientSocket, input, output,
					publisher).start();

		}else if ("LISTEN-TICKS".equals(command)) {
			new TickInfoConnectionHandler(it, clientSocket, input, output,
					publisher).start();

		} else if ("LISTEN-MESSAGES".equals(command)) {
			new MessageInfoConnectionHandler(clientSocket, input, output,
					publisher).start();

		} else if ("LISTEN-ACCOUNT".equals(command)) {
			new AccountingInfoConnectionHandler(clientSocket, input, output,
					publisher).start();

		} else if ("LISTEN-ORDERS".equals(command)) {
			new OrderInfoConnectionHandler(clientSocket, input, output,
					publisher).start();

		} else if ("SUBMITORDER".equals(command)) {
			new SubmitOrderCommandHandler(it, clientSocket, input, output,
					publisher).start();

		} else if ("CLOSEORDER".equals(command)) {
			new CloseCommandHandler(it, clientSocket, input, output, publisher)
					.start();

		} else {
			UnknownCommandException e = new UnknownCommandException(command);
			try {
				output.println(FormatterFactory.getDefaultInstance().format(e));
				output.close();
				input.close();
			} finally {
				try {
					clientSocket.close();
				} catch (Exception e2) {
				}
			}
			throw e;
		}

	}
}
