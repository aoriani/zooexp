package br.unicamp.ic.zooexp.core.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

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

    private String serverAddress;
    private int serverPort;
    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private Socket socket;
    private Random random = new Random();

    public Client() {
        serverAddress = Configuration.getServerAddress();
        serverPort = Configuration.getServerPort();
    }

    public void connect() throws UnknownHostException, IOException {

        socket = new Socket(InetAddress.getByName(serverAddress),
                serverPort);
        log.info("Connected to " + serverAddress + ":" + serverPort);

        toServer = new DataOutputStream(socket.getOutputStream());
        fromServer = new DataInputStream(socket.getInputStream());
    }

    public void disconnect() {
        if (socket != null) {
            try {
                toServer.close();
                fromServer.close();
                socket.close();
            } catch (IOException e) {
                log.warn("Failed to close connection to server", e);
            } finally{
                socket = null;
                fromServer = null;
                toServer = null;
            }
        }
    }


    private void trySendOpAndGetReply(Operation op, Reply rep)
            throws UnknownHostException, IOException, InterruptedException{
        /*
         * Here we get a lot of trouble. You can't know whether the server has
         * really closed the connection until you try to send or receive something.
         * So we may still have a Socket object, but it will throw SocketExceptions
         * at 1st use. We also have failover, so client will not be able to connect
         * to server during some time. So we have to retry to send the operation
         * like in the Transaction class.
         *
         * FIXME: We have the partial failure here if server receives the operation
         * but does not send a reply. At the moment I don't know how to detect
         * and handle it properly
         */

        final int MAX_RETRIES = 5;
        final long RETRY_DELAY_FACTOR = 500L;
        SocketException socketException = null;


        for(int i = 1 ; i <= MAX_RETRIES ; i++){
            try{
                if(socket == null){
                    connect();
                }
                op.serialize(toServer);
                toServer.flush();
                rep.parse(fromServer);
                //If we get here , we can return safely
                return;
            } catch (SocketException e){
                log.warn("Could not connect to server at attemp #"+i,e);
                disconnect();
                socketException = e;
                //Exponential back off
                Thread.sleep(RETRY_DELAY_FACTOR * random.nextInt(1 << i));
            }
        }

        //If after MAX_RETRIES we could not connect, throw the saved exception
        //All other exceptions have been already thrown
        throw socketException;
    }

    private Reply sendOperationAndGetReply(Operation op)
            throws ServerException, IOException {
        Reply result = new Reply();

        try {
            trySendOpAndGetReply(op, result);

            if (result.getType() == Reply.REPLY_FAILURE) {
                throw new ServerException(
                        "Server failed to process request. OP:"
                                + op.getType()
                                + ((op.getType() != Operation.READ_OP) ? (" ARG: " + op
                                        .getArg())
                                        : ""));
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
        } catch (InterruptedException e) {
           log.error("Interrupted before getting operation sent", e);
           throw new IOException("Failed to send operation: Interrupted");
        }
        return result;
    }

    public void set(int value) throws ServerException, IOException {
        log.info("Sending SET request with value " + value);
        Operation setop = new Operation(Operation.SET_OP, value);
        sendOperationAndGetReply(setop);
    }

    public void add(int value) throws ServerException, IOException {
        log.info("Sending ADD request with value " + value);
        Operation addop = new Operation(Operation.ADD_OP, value);
        sendOperationAndGetReply(addop);
    }

    public void sub(int value) throws ServerException, IOException {
        log.info("Sending SUB request with value " + value);
        Operation subop = new Operation(Operation.SUB_OP, value);
        sendOperationAndGetReply(subop);
    }

    public int get() throws ServerException, IOException {
        log.info("Sending READ request");
        Operation addop = new Operation(Operation.READ_OP, 0);
        Reply reply = sendOperationAndGetReply(addop);
        return reply.getReturnValue();
    }

}
