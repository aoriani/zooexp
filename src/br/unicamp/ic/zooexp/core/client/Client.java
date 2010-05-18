package br.unicamp.ic.zooexp.core.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.core.Configuration;
import br.unicamp.ic.zooexp.core.Operation;
import br.unicamp.ic.zooexp.core.Reply;
import br.unicamp.ic.zooexp.core.server.Data;

/**
 * The client library for our server
 *
 */
public class Client {
    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);
    
    
    boolean isConnected;
    String serverAddress;
    int serverPort;
    DataOutputStream toServer;
    DataInputStream fromServer;
    Socket socket;
    
    public Client(){
	isConnected = false;
	serverAddress = Configuration.getServerAddress();
	serverPort = Configuration.getServerPort();
    }
    
    public void connect() throws UnknownHostException, IOException{
	if(!isConnected){
	    socket = new Socket(InetAddress.getByName(serverAddress),serverPort);
	    log.info("Connected to "+serverAddress+":"+serverPort);
	    
	    toServer = new DataOutputStream(socket.getOutputStream());
	    fromServer = new DataInputStream(socket.getInputStream());
	    
	    isConnected = true;
	}
    }
    
    public void disconnect(){
	if(isConnected){
        	try {
		    toServer.close();
		    fromServer.close();
		    socket.close();
		} catch (IOException e) {
		    log.warn("Failed to close connection to server", e);
		}
        	isConnected = false;
	}
    }
    
    private Reply sendOperationAndGetReply(Operation op) throws ServerException, IOException{
	Reply result = null;
	
	try {
	    if(!isConnected){
	        connect();
	    }
	    op.serialize(toServer);
	    result = Reply.parse(fromServer);
	    toServer.flush();
	    
	    if (result.getType() == Reply.REPLY_FAILURE) {
	        throw new ServerException("Server failed to process request. OP:"
	    	    + op.getType()
	    	    + ((op.getType() != Operation.READ_OP) ? (" ARG: " + op.getArg()) : ""));
	    }
	} catch (UnknownHostException e) {
	    log.error("Could not locate server", e);
	    disconnect();
	    throw e;
	} catch (IOException e) {
	    log.error("Some problem when sending request", e);
	    disconnect();
	    throw e;
	} catch (ServerException e) {
	    log.error("Server did not liked the request", e);
	    disconnect();
	    throw e;
	}
	return result;
    }
    
    
    public void set(int value) throws ServerException, IOException{
	log.info("Sending SET request with value " + value);
	Operation setop = new Operation(Operation.SET_OP,value);
	sendOperationAndGetReply(setop);
    }
    
    public void add(int value) throws ServerException, IOException{
	log.info("Sending ADD request with value " + value);
	Operation addop = new Operation(Operation.ADD_OP,value);
	sendOperationAndGetReply(addop);
    }
    
    public void sub(int value) throws ServerException, IOException{
	log.info("Sending SUB request with value " + value);
	Operation subop = new Operation(Operation.SUB_OP,value);
	sendOperationAndGetReply(subop);
    }
    
    public int get() throws ServerException, IOException{
	log.info("Sending READ request");
	Operation addop = new Operation(Operation.READ_OP,0);
	Reply reply = sendOperationAndGetReply(addop);
	return reply.getReturnValue();
    }

}
