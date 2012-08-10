package lisp2forex.handlers.order;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.Callable;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.ConnectionHandler;

import org.apache.log4j.Logger;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

public class CloseCommandHandler extends ConnectionHandler {

	public CloseCommandHandler(Iterator<String> it, Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger) {
		super(it, clientSocket, input, output, messenger, Logger
				.getLogger(CloseCommandHandler.class));
	}
	final private Object waitExecutionOfOrder = new Object();

	@Override
	protected boolean handle() throws Exception {
		synchronized (waitExecutionOfOrder) {
			publisher.context.executeTask(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					executeClose(it);
					return null;
				}
			});
			waitExecutionOfOrder.wait();
		}
		return false;
	}

	private void executeClose(Iterator<String> it) throws JFException {
		String label = it.next();
		double amount = 0;

		if (it.hasNext()) {
			amount = Double.valueOf(it.next());
		}

		if ("ALL".equals(label)) {
			for (IOrder order : publisher.context.getEngine().getOrders()) {
				publisher.context.getEngine().getOrder(order.getLabel())
						.close(amount);
			}
		} else {
			publisher.context.getEngine().getOrder(label).close(amount);
		}
		
		synchronized (waitExecutionOfOrder) {
			waitExecutionOfOrder.notifyAll();
		}
	}

}
