package lisp2forex.handlers.info;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.ConnectionHandler;

import org.apache.log4j.Logger;

import com.dukascopy.api.Instrument;

public class TickInfoConnectionHandler extends ConnectionHandler {

	public TickInfoConnectionHandler(Iterator<String> it, Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger){
		this(true, it, clientSocket, input, output, messenger);
	}
	
	public TickInfoConnectionHandler(boolean listen, Iterator<String> it, Socket clientSocket, 
			BufferedReader input, PrintWriter output,
			PublisherStrategy publisher) {
		super(listen, it, clientSocket, input, output, publisher, Logger
				.getLogger(TickInfoConnectionHandler.class));
	}
	private Instrument instrument;

	@Override
	protected boolean handle() throws Exception {
		if (null == instrument) instrument = Instrument.valueOf(it.next());
		logger.debug("waiting for ITick.");
		String result = null;
		
		synchronized (publisher.tickMonitor[instrument.ordinal()]) {
			publisher.tickMonitor[instrument.ordinal()].wait();
			result = formatter.format(publisher.ticks[instrument.ordinal()]);
		}
		output.println(result);
		logger.debug("got a tick.");
		
		return !output.checkError();
	}
	
}
