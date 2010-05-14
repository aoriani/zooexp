package br.unicamp.ic.zooexp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import br.unicamp.ic.zooexp.Operation;
import br.unicamp.ic.zooexp.Reply;

public class WorkerThread implements Runnable {

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

		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	    }

	} finally {
	    try {
		connection.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}

    }

}
