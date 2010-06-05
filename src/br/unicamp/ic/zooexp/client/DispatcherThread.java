package br.unicamp.ic.zooexp.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.core.client.Client;
import br.unicamp.ic.zooexp.core.client.ServerException;
import br.unicamp.ic.zooexp.core.server.Data;

/**
 * A dispatcher thread to send request to server, so UI is not blocked
 * 
 */
public class DispatcherThread extends Thread {
    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);

    public static interface ClientEventListener {
	public void onClientError(Exception e);

	public void onResult(int value);
    }

    private static class PendingRequest {
	public enum Type {
	    SET, ADD, SUB, READ, END
	}

	public Type type;
	public int arg;

	public PendingRequest(Type type, int arg) {
	    this.type = type;
	    this.arg = arg;
	}

	public PendingRequest(Type type) {
	    this(type, 0);
	}
    }

    private Queue<PendingRequest> queue = new ConcurrentLinkedQueue<PendingRequest>();
    Client client;
    ClientEventListener listener;

    public DispatcherThread(ClientEventListener listener) {
	this.listener = listener;
	this.client = new Client();
    }

    private void enqueueRequest(PendingRequest pr) {
	queue.add(pr);
	// notify consumer thread
	synchronized (this) {
	    notifyAll();
	}
    }

    public void setValue(int value) {
	enqueueRequest(new PendingRequest(PendingRequest.Type.SET, value));
    }

    public void addValue(int value) {
	enqueueRequest(new PendingRequest(PendingRequest.Type.ADD, value));
    }

    public void subValue(int value) {
	enqueueRequest(new PendingRequest(PendingRequest.Type.SUB, value));
    }

    public void readValue() {
	enqueueRequest(new PendingRequest(PendingRequest.Type.READ));
    }

    public void disconnect() {
	enqueueRequest(new PendingRequest(PendingRequest.Type.END));
    }

    @Override
    public void run() {

	boolean shallRun = true;

	try {
	    client.connect();
	} catch (UnknownHostException e) {
	    listener.onClientError(e);
	} catch (IOException e) {
	    listener.onClientError(e);
	}

	while (shallRun) {

	    // block until thread has something to eat
	    synchronized (this) {
		while (queue.size() == 0)
		    try {
			wait();
		    } catch (InterruptedException e) {
			log.warn("thread interrupted", e);
		    }
	    }

	    PendingRequest pr = queue.remove();
	    try {
		switch (pr.type) {
		case SET:
		    client.set(pr.arg);
		    break;

		case ADD:
		    client.add(pr.arg);
		    break;

		case SUB:
		    client.sub(pr.arg);
		    break;

		case READ:
		    int result = client.get();
		    listener.onResult(result);
		    break;

		case END:
		    client.disconnect();
		    shallRun = false;
		    break;
		}
	    } catch (ServerException e) {
		listener.onClientError(e);
	    } catch (IOException e) {
		listener.onClientError(e);
	    }
	}
    }
}
