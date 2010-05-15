package br.unicamp.ic.zooexp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.Operation;
import br.unicamp.ic.zooexp.Reply;

public class WorkerThread implements Runnable {
    
    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);
    
    Socket connection;
    Data data;
    DataInputStream fromClientStream;
    DataOutputStream toClientStream;

    public WorkerThread(Socket connection, Data data) throws IOException {
	this.connection = connection;
	this.data = data;
	this.fromClientStream = new DataInputStream(connection.getInputStream());
	this.toClientStream = new DataOutputStream(connection.getOutputStream());
    }

    private void processRequest() throws IOException {
	Operation op = Operation.parse(fromClientStream);
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
		    processRequest();
		} catch (IOException e) { //Some error or client disconnected
		    clientConnected = false;
		    log.info("Connection with client ended", e);
		}
	    }

	} finally {
	    try {
		connection.close();
	    } catch (IOException e) {
		log.error("Problem when closing connection to cliente", e);
	    }

	}

    }

}
