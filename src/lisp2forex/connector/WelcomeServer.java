package lisp2forex.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lisp2forex.handlers.ConnectionHandlerFactory;

import org.apache.log4j.Logger;

// ties  the Publisher with an newly created ConnectionHandler instance.
public class WelcomeServer extends Thread {

	private final ServerSocket server;
	private final PublisherStrategy publisher;
	private final Logger logger = Logger.getLogger(WelcomeServer.class);

	public WelcomeServer(int port, PublisherStrategy publisher)
			throws IOException {
		this.server = new ServerSocket(port);
		this.publisher = publisher;
	}

	private String allowedIPStartsWith = null;

	public WelcomeServer(int port, PublisherStrategy publisher,
			String allowedIPStartsWith) throws IOException {
		this(port, publisher);
		this.allowedIPStartsWith = allowedIPStartsWith;
	}

	@Override
	public void run() {
		Socket clientSocket = null;

		while (! (isInterrupted() || server.isClosed()) ) {
			try {
				logger.info("waiting for clients");
				clientSocket = server.accept();
				if (null != allowedIPStartsWith) {
					if (!clientSocket.getRemoteSocketAddress().toString()
							.startsWith(allowedIPStartsWith)) {
						logger.warn("client ip "
								+ clientSocket.getRemoteSocketAddress()
								+ " is forbidden to access");
						throw new RuntimeException("forbidden ip");
					}
				}
				logger.info("got a connection from "
						+ clientSocket.getRemoteSocketAddress());

				BufferedReader input = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				PrintWriter output = new PrintWriter(
						clientSocket.getOutputStream(), true);

				ConnectionHandlerFactory.runInstance(clientSocket, input,
						output, publisher);
			} catch (Exception e1) {
				logger.error(e1);
			}
		}

	}
}