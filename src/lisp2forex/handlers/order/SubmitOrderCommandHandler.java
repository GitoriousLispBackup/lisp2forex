package lisp2forex.handlers.order;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import lisp2forex.connector.PublisherStrategy;
import lisp2forex.handlers.ConnectionHandler;

import org.apache.log4j.Logger;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;

public class SubmitOrderCommandHandler extends ConnectionHandler {

	public SubmitOrderCommandHandler(Iterator<String> it, Socket clientSocket,
			BufferedReader input, PrintWriter output,
			PublisherStrategy messenger) {
		super(it, clientSocket, input, output, messenger, Logger
				.getLogger(SubmitOrderCommandHandler.class));
	}
	final private Object waitExecutionOfOrder = new Object();
	
	@Override
	protected boolean handle() throws Exception {
		synchronized (waitExecutionOfOrder) {
			publisher.context.executeTask(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					executeSubmit(it);
					return null;
				}
			});
			waitExecutionOfOrder.wait();
		}
		return false;
	}

	private void executeSubmit(Iterator<String> it) throws JFException {
		Instrument instrument = Instrument.valueOf(it.next());
		String label = getLabel(instrument);
		OrderCommand orderCommand = OrderCommand.valueOf(it.next());
		double amount = Double.parseDouble(it.next()) / 1000000.0; // different
		double price = 0, slippage = 0, stopLossPrice = 0, takeProfitPrice = 0;
		long goodTillTime = 0;
		int whichFunction = 1;

		if (it.hasNext()) {
			price = Double.valueOf(it.next());
			whichFunction = 2;
			if (it.hasNext()) {
				slippage = Double.valueOf(it.next());
				whichFunction = 3;
				if (it.hasNext()) {
					stopLossPrice = Double.valueOf(it.next());
					takeProfitPrice = Double.valueOf(it.next());
					whichFunction = 4;
					if (it.hasNext()) {
						goodTillTime = Long.valueOf(it.next());
						whichFunction = 5;
					}
				}
			}
		}

		IOrder result = null;
		switch (whichFunction) {
		case 1:
			result = publisher.context.getEngine().submitOrder(label,
					instrument, orderCommand, amount);
			break;
		case 2:
			result = publisher.context.getEngine().submitOrder(label,
					instrument, orderCommand, amount, price);
			break;
		case 3:
			result = publisher.context.getEngine().submitOrder(label,
					instrument, orderCommand, amount, price, slippage);
			break;
		case 4:
			result = publisher.context.getEngine().submitOrder(label,
					instrument, orderCommand, amount, price, slippage,
					stopLossPrice, takeProfitPrice);
			break;
		case 5:
			result = publisher.context.getEngine().submitOrder(label,
					instrument, orderCommand, amount, price, slippage,
					stopLossPrice, takeProfitPrice, goodTillTime);
			break;
		}

		output.println(result.getLabel());
		synchronized (waitExecutionOfOrder) {
			waitExecutionOfOrder.notifyAll();
		}		
	}

	private static String getLabel(Instrument instrument) {
		String label = instrument.name();
		label = label + "_" + System.currentTimeMillis() + "_"
				+ labelCounter.getAndIncrement();
		return label;
	}

	private static final AtomicLong labelCounter = new AtomicLong(0);
}
