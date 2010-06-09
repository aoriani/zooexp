
package br.unicamp.ic.zooexp.server.passive;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;

import br.unicamp.ic.zooexp.core.Configuration;
import br.unicamp.ic.zooexp.core.server.Data;

public class ServerContext implements Watcher {
    /** Logger */
    private static final Logger log = Logger.getLogger(ServerContext.class);

    private ZooKeeper zooConnection;
    private Data data = new Data();
    private CountDownLatch connectedSignal = new CountDownLatch(1);

    private void connectToZooKeeper() {

        // Attempt 10 times to connect to Zookeeper
        for (int i = 1;; i++) {
            try {
                zooConnection = new ZooKeeper(Configuration.getZooKeeperServerList(),
                                                Configuration.getZooTimeout(), this);
                connectedSignal.await();
            } catch (IOException e) {
                log.warn("Attemp #" + i + " to connect to ZooKeeper failed", e);

                if (i == 10) {
                    log.fatal("Maximum attemps to connect to ZooKeeper. Shutting down!");
                    System.exit(1);
                }
                // Back off
                try {
                    Thread.sleep(1000L * 10L * i);
                } catch (InterruptedException e1) {
                    log.error("Back off interrupted", e1);
                }
                continue; // retry again
            } catch (InterruptedException e) {
                log.error("Interrupted when wainting for connection", e);
            }

            // We connected, get out the loop
            break;
        }
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getState()) {
        case SyncConnected:
            log.info("Connected to ZooKeeper");
            connectedSignal.countDown();
            break;
        case Expired:
            log.fatal("ZooKeeper Session expired."+
                    " Commiting suicide since others believe I am dead");
            if (zooConnection != null)
                try {
                    zooConnection.close();
                } catch (InterruptedException e) {
                    log.error("Got interrupted when closing after expiration");
                }
            System.exit(1);

        }

    }

    public static void main(String[] args) {
        ServerContext s = new ServerContext();
        s.connectToZooKeeper();

        //create nodes
        try {
            s.zooConnection.create(Configuration.getOpLogZnode(), null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException e1) {
            if(e1.code() != Code.NODEEXISTS)
                    e1.printStackTrace();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Thread state1 = new Thread (new BackupState(s));
        state1.start();
        try {
            state1.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread state2 = new Thread (new PrimaryState(s));
        state2.start();
        try {
            state2.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public ZooKeeper getZookeeper() {
        return zooConnection;
    }

    public Data getData() {
        return data;
    }

}
