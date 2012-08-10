package lisp2forex.connector;

import java.util.HashSet;
import java.util.Set;

import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;

public class Main {
	private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
	private static String userName = ""; //your demo account name
	private static String password = ""; // your demo account password

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		Set instruments = new HashSet();
		instruments.add(Instrument.EURUSD);
		IStrategy publisherStrategy = new PublisherStrategy(1971);
		new SimpleStrategyRunner(userName, password, jnlpUrl).startStrategy(
				publisherStrategy, instruments);
	}
}
