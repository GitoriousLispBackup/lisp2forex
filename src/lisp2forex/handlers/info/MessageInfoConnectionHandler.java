package lisp2forex.handlers.info;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.ConnectionHandler;

import org.apache.log4j.Logger;

//ORDER_SUBMIT_REJECTED, ORDER_SUBMIT_OK, ORDER_FILL_REJECTED, ORDER_FILL_OK, 
//ORDER_CLOSE_REJECTED, ORDER_CLOSE_OK, ORDERS_MERGE_REJECTED, ORDERS_MERGE_OK, 
//ORDER_CHANGED_OK, ORDER_CHANGED_REJECTED,
//MAIL, NEWS, CALENDAR, NOTIFICATION,
//INSTRUMENT_STATUS, CONNECTION_STATUS, STRATEGY_BROADCAST

public class MessageInfoConnectionHandler extends ConnectionHandler {

	public MessageInfoConnectionHandler (Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger){
		this(true, clientSocket, input, output, messenger);
	}
	
	public MessageInfoConnectionHandler(boolean listen, Socket clientSocket,BufferedReader input,
			PrintWriter output, PublisherStrategy publisher) {
		super(listen, null, clientSocket, input, output, publisher, Logger
				.getLogger(MessageInfoConnectionHandler.class));
	}

	@Override
	protected boolean handle() throws Exception {
		logger.debug("waiting for message update.");
		
		String result = null; 
		synchronized (publisher.messageMonitor) {
				publisher.messageMonitor.wait();
				if(null != publisher.message.getContent()){
					result = formatter.format(publisher.message);
				}
		}
		if (null != result) output.println(result);
		logger.debug("got message update.");

		return !output.checkError();
	}

}
