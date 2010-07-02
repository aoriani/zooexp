package br.unicamp.ic.zooexp.server.passive;

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
public class PrimaryState implements ServerState {

    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);
    private volatile ServerContext context;

    public PrimaryState(ServerContext context){
        this.context = context;
    }

    @Override
    public void execute () throws Throwable {

        log.info("Starting PRIMARY state");

        final int port = Configuration.getServerPort();
        final int maxconn = Configuration.getServerMaxConn();


            try {
                ServerSocket serverSocket = new ServerSocket(port, maxconn);

                log.info("Started server at "
                        + InetAddress.getLocalHost().getHostAddress() + ":" + port);

                int consecutiveFailures = 0;

                while (true) {
                    Socket clientSocket;
                    String clientId;

                    try{
                        log.info("Waiting for connections...");
                        clientSocket = serverSocket.accept();
                        clientId = clientSocket.getInetAddress().getHostAddress()
                                + ":" + clientSocket.getPort();
                        log.info("Connected to " + clientId);

                        // set client timeout
                        clientSocket.setSoTimeout(Configuration
                                .getServerClientTimeout());

                        // Dispatching to a work thread.
                        // We use IP:port to identify client of worker thread
                        Thread worker = new Thread(
                                new WorkerThread(clientSocket, context), clientId);
                        worker.start();

                    }
                    catch(IOException e){
                        /*
                         * We assume that until 10 consecutive failures, the problem is
                         * at clients. For more than 10 failures we assume that the problem
                         * is at server, so it has to be shut down
                         */
                        ++consecutiveFailures;
                        log.warn("Some failure when connecting to client. Consecutive failures: "
                                + consecutiveFailures , e);
                        if(consecutiveFailures > 10 ) {
                            throw e;
                        }else{
                            continue; //skip the reset of counter
                        }
                    }

                    //reset consecutive failures
                    consecutiveFailures = 0;
                }
            } catch (UnknownHostException e) {
                log.error("Some problem trying to discover own IP",e);
                throw e;
            } catch (IOException e) {
                log.error("Some problem to connect to client while in BACKUP state", e);
                throw e ;
            }
    }

}
