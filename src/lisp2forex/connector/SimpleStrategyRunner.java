package lisp2forex.connector;

import java.util.Set;

import org.apache.log4j.Logger;

import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;

public class SimpleStrategyRunner {

	private final Logger logger = Logger.getLogger(SimpleStrategyRunner.class);
	private IClient client;
	private long ID;

	private final String userName;
	private final String password;
	private final String jnlpUrl;
	private int lightReconnects = 3;

	public SimpleStrategyRunner(String userName, String password, String jnlpUrl) {
		this.userName = userName;
		this.password = password;
		this.jnlpUrl = jnlpUrl;

	}

	class SimpleListener implements ISystemListener{
		@Override
		public void onStart(long processId) {
			logger.info("started: " + processId);
		}

		@Override
		public void onStop(long processId) {
			logger.info("stopped: " + processId);
			if (client.getStartedStrategies().size() == 0) {
				System.exit(0);
			}
		}

		@Override
		public void onConnect() {
			logger.info("Connected");
			lightReconnects = 3;
		}

		@Override
		public void onDisconnect() {
			logger.warn("Disconnected");
			if (lightReconnects > 0) {
				client.reconnect();
				--lightReconnects;
			} else {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
				try {
					client.connect(jnlpUrl, userName, password);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
	}
	
	

	public long startStrategy(IStrategy strategy,
			Set<Instrument> subscribeInstruments) {

		logger.info("LispConnector connecting ...");

		if (null == client) {
			try {
				client = ClientFactory.getDefaultInstance();
			} catch (Exception e) {
				logger.error(e);
				logger.error("can not get default IClient, quitting !");
				System.exit(-1);
			}

			client.setSystemListener(new SimpleListener());
		}

		if (!client.isConnected()) {
			try {
				client.connect(jnlpUrl, userName, password);
				int i = 10;
				while (i > 0 && !client.isConnected()) {
					Thread.sleep(1000);
					i--;
				}
				if (!client.isConnected()) {
					logger.error("Failed to connect Dukascopy servers");
					throw new RuntimeException(
							"Failed to connect Dukascopy servers");
				}
			} catch (Exception e) {
				logger.error(e);
				logger.error("can not connect to dukascopy servers, quitting !");
				System.exit(-1);
			}
		}
		client.setSubscribedInstruments(subscribeInstruments);
		return ID = client.startStrategy(strategy);
	}

	public void stopStrategy() {
		logger.info("stopping strategy id : " + ID);
		client.stopStrategy(ID);
	}

	public void stopStrategy(long id) {
		logger.info("stopping strategy id : " + id);
		client.stopStrategy(id);
	}
}