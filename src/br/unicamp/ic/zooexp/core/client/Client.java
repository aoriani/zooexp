package br.unicamp.ic.zooexp.core.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import br.unicamp.ic.zooexp.core.Configuration;
import br.unicamp.ic.zooexp.core.Operation;
import br.unicamp.ic.zooexp.core.Reply;
import br.unicamp.ic.zooexp.core.server.Data;
import br.unicamp.ic.zooexp.core.trasactions.GetDataTransaction;

/**
 * The client library for our server
 *
 */
public class Client {
    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);

    private DataOutputStream toServer;
    private DataInputStream fromServer;
    private Socket socket;
    private Random random = new Random();
    private ServerDiscover serverDiscover;

    public Client() {
        if (Configuration.shallRetrieveServerFromZooKeeper()){
            try {
                serverDiscover = new ServerDiscover();
            } catch (IOException e) {
                log.warn("Could not connect to Zookeeper()",e);
            } catch (InterruptedException e) {
                log.warn("Got interrupted", e);
            }
        }
    }

    public void connect() throws UnknownHostException, IOException {

        //Ensure it is clean before doing something
        disconnect();

        String serverAddress = getServerAddress();
        int serverPort = getServerPort();

        socket = new Socket(InetAddress.getByName(serverAddress),
                serverPort);
        log.info("Connected to " + serverAddress + ":" + serverPort);

        toServer = new DataOutputStream(socket.getOutputStream());
        fromServer = new DataInputStream(socket.getInputStream());
    }

    private String getServerAddress() {
        try{
            return serverDiscover.getServerAddress();
        }catch(Exception e){
            log.warn("Fallbacking to default server address", e);
            return Configuration.getServerAddress();
        }
    }

    private int getServerPort() {
        try{
            return serverDiscover.getServerPort();
        }catch(Exception e){
            log.warn("Fallbacking to default server port", e);
            return Configuration.getServerPort();
        }
    }

    private void disconnect() {
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

    public void shutdown() throws InterruptedException{
        disconnect();
        if(serverDiscover != null){
            serverDiscover.shutdown();
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
         *
         * TODO: Consider for refactoring since we are using the same algorithm
         * that Transaction
         */

        final int MAX_RETRIES = 5;
        final long RETRY_DELAY_FACTOR = 500L;
        SocketException socketException = null;


        for(int i = 1 ; i <= MAX_RETRIES ; i++){
            try{
                if(socket == null || (serverDiscover != null && serverDiscover.hasChanged())){
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
            throws ServerException, IOException, InterruptedException {
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
        }

        return result;
    }

    public void set(int value) throws ServerException, IOException, InterruptedException {
        log.info("Sending SET request with value " + value);
        Operation setop = new Operation(Operation.SET_OP, value);
        sendOperationAndGetReply(setop);
    }

    public void add(int value) throws ServerException, IOException, InterruptedException {
        log.info("Sending ADD request with value " + value);
        Operation addop = new Operation(Operation.ADD_OP, value);
        sendOperationAndGetReply(addop);
    }

    public void sub(int value) throws ServerException, IOException, InterruptedException {
        log.info("Sending SUB request with value " + value);
        Operation subop = new Operation(Operation.SUB_OP, value);
        sendOperationAndGetReply(subop);
    }

    public int get() throws ServerException, IOException, InterruptedException {
        log.info("Sending READ request");
        Operation addop = new Operation(Operation.READ_OP, 0);
        Reply reply = sendOperationAndGetReply(addop);
        return reply.getReturnValue();
    }


    private static class ServerDiscover implements Watcher{


        private ZooKeeper zookeeper;
        private CountDownLatch connectedSignal;
        private boolean changed;
        private String connectionString;

        public ServerDiscover() throws IOException, InterruptedException{
            connect();
        }


        private void connect() throws IOException, InterruptedException{
            connectedSignal = new CountDownLatch(1);
            changed = true;
            zookeeper = new ZooKeeper(Configuration.getZooKeeperServerList(),
                    Configuration.getZooTimeout(), this);

            //We are in the process of connecting. Wait it to finish
            connectedSignal.await();

        }


        public synchronized void shutdown() throws InterruptedException{
            if(zookeeper != null){
                zookeeper.close();
            }

        }

        private synchronized void retrieveConfig() throws KeeperException, InterruptedException{
            if(changed){
                byte[] data = (new GetDataTransaction(zookeeper,Configuration.getServerZnodeGroup(),this)).invoke();
                connectionString = new String(data, Charset.forName("UTF-8"));
                log.info("Server retrieved from Zookeeper => " + connectionString);
            }
            changed = false;
        }

        public synchronized String getServerAddress() throws KeeperException, InterruptedException{
            retrieveConfig();
            int index = connectionString.indexOf(":");
            return connectionString.substring(0, index);
        }

        public synchronized int getServerPort() throws KeeperException, InterruptedException{
            retrieveConfig();
            int index = connectionString.indexOf(":");
            String portString = connectionString.substring(index + 1, connectionString.length());
            return Integer.parseInt(portString);
        }

        public synchronized boolean hasChanged(){
            return changed;
        }

        @Override
        public synchronized void process(WatchedEvent event) {

            //If we are waiting for connection
            if((event.getState() == KeeperState.SyncConnected) &&
                    (connectedSignal.getCount() > 0)){
                connectedSignal.countDown();

            }else if(event.getState() == KeeperState.Expired){
                try {
                    connect();
                } catch (IOException e) {
                    log.error("Could not reconnect to Zookeeper",e);
                } catch (InterruptedException e) {
                    log.error("Got Interrupted", e);
                }
            } else if(event.getType() == EventType.NodeDataChanged){
                log.info("Server has changed");
                changed  = true;
            }

        }
    }

}


