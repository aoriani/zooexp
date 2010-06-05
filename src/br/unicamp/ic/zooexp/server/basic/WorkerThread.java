package br.unicamp.ic.zooexp.server.basic;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.core.Operation;
import br.unicamp.ic.zooexp.core.Reply;
import br.unicamp.ic.zooexp.core.server.Data;

public class WorkerThread implements Runnable {

    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);

    private Socket connection;
    private Data data;
    private InputStream fromClientStream;
    private OutputStream toClientStream;
    private final String clientId;

    public WorkerThread(Socket connection, Data data) throws IOException {
	this.connection = connection;
	this.data = data;
	this.fromClientStream = connection.getInputStream();
	this.toClientStream = connection.getOutputStream();
	// We use IP:Port to identify a client. It works even on same host
	this.clientId = connection.getInetAddress().getHostAddress() + ":"
		+ connection.getPort();
    }

    private void processRequest() throws IOException {
	Operation op = new Operation();
	op.parse(fromClientStream);
	Reply reply = data.executeOperation(op);

	// Send reply to client
	reply.serialize(toClientStream);
	toClientStream.flush();
    }

    @Override
    public void run() {
	try {
	    boolean clientConnected = true;

	    while (clientConnected) {
		try {
		    log.info("Processing a new request from " + clientId);
		    processRequest();
		} catch (EOFException e) { // Some error or client disconnected
		    clientConnected = false;
		    log.info("Connection with client " + clientId + " ended");
		} catch (IOException e) { // Some error or client disconnected
		    clientConnected = false;
		    log.warn("Connection with client " + clientId
			    + " ended by failure", e);
		}
	    }

	} finally {
	    try {
		connection.close();
	    } catch (IOException e) {
		log.error("Problem when closing connection to client "
			+ clientId, e);
	    }

	}

    }

}
