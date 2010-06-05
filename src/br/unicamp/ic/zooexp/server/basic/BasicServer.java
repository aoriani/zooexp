package br.unicamp.ic.zooexp.server.basic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.core.Configuration;
import br.unicamp.ic.zooexp.core.server.Data;

/**
 * This is a basic server class that waits for connections and dispatch it to
 * worker threads
 * 
 */
public class BasicServer {

    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);

    public static void main(String[] args) {
	Data data = new Data();
	int port = Configuration.getServerPort();
	int maxconn = Configuration.getServerMaxConn();

	try {
	    ServerSocket serverSocket = new ServerSocket(port, maxconn);

	    log.info("Started server at "
		    + InetAddress.getLocalHost().getHostAddress() + ":" + port);

	    while (true) {

		log.info("Waiting for connections...");
		Socket clientSocket = serverSocket.accept();
		String clientId = clientSocket.getInetAddress()
			.getHostAddress()
			+ ":" + clientSocket.getPort();
		log.info("Connected to " + clientId);

		// set client timeout
		clientSocket.setSoTimeout(Configuration
			.getServerClientTimeout());

		// Dispatching to a work thread.
		// We use IP:port to identify client of worker thread
		Thread worker = new Thread(
			new WorkerThread(clientSocket, data), clientId);
		worker.start();

	    }
	} catch (UnknownHostException e) {
	    log.error("Could resolve client IP address", e);
	} catch (IOException e) {
	    log.error("Some network error occured", e);
	}

    }

}
