package lisp2forex.connector;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;

public class PublisherStrategy implements IStrategy {

	public static final Logger logger = Logger
			.getLogger(PublisherStrategy.class);

	public PublisherStrategy(int listenPort) {
		this.portToListen = listenPort;
	}

	final private int portToListen;

	public PublisherStrategy(int listenPort, String allowedIpStartsWith) {
		this.portToListen = listenPort;
		this.allowedIpStartsWith = allowedIpStartsWith;
	}

	private String allowedIpStartsWith = null;

	@Override
	public void onStart(IContext context) throws JFException {
		logger.info("strategy started, enabling welcome server");
		this.context = context;
		try {
			new WelcomeServer(portToListen, this, allowedIpStartsWith).start();
		} catch (IOException e) {
			logger.error(e);
			logger.error("can not start welcome server, quitting !");
			System.exit(-1);
		}
	}

	public volatile IContext context;

	@Override
	public void onStop() throws JFException {
		logger.info("stopping");

	}

	@Override
	public void onTick(Instrument instrument, ITick tick) throws JFException {
		logger.debug("waiting for new tick data.");
		int id = instrument.ordinal();
		synchronized (tickMonitor[id]) {
			ticks[id] = tick;
			tickMonitor[id].notifyAll();
		}
		logger.debug("new tick published." + tick);
	}

	public final ITick[] ticks = new ITick[Instrument.values().length];
	public final Object[] tickMonitor = new Object[Instrument.values().length];
	{
		for (int i = 0; i < tickMonitor.length; i++)
			tickMonitor[i] = new Object();
	}

	@Override
	public void onMessage(IMessage message) throws JFException {
		logger.debug("waiting for new message.");
		synchronized (messageMonitor) {
			this.message = message;
			messageMonitor.notifyAll();
			if(message.getType().toString().startsWith("ORDER")){
				synchronized (orderMonitor) {
					orderMonitor.notifyAll();
				}
			}
		}
		logger.debug("new message published : " + message);
	}

	public volatile IMessage message;
	public final Object messageMonitor = new Object();
	public final Object orderMonitor = new Object();

	@Override
	public void onAccount(IAccount account) throws JFException {
		logger.info("waiting for new accounting info.");
		synchronized (accountMonitor) {
			this.account = account;
			accountMonitor.notifyAll();
		}
		logger.debug("new accounting info published : " + account);
	}

	public volatile IAccount account;
	public final Object accountMonitor = new Object();

	@Override
	public void onBar(Instrument instrument, Period period, IBar askBar,
			IBar bidBar) throws JFException {
	}

}
