
package br.unicamp.ic.zooexp.server.passive;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

import br.unicamp.ic.zooexp.core.Configuration;
import br.unicamp.ic.zooexp.core.server.Data;
import br.unicamp.ic.zooexp.server.passive.trasactions.CreateNodeTransaction;

public class ServerContext implements Watcher {
    /** Logger */
    private static final Logger log = Logger.getLogger(ServerContext.class);

    public enum State {INIT,BACKUP, PRIMARY};


    private ZooKeeper zooConnection;
    private Data data = new Data();
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private State currentState = State.INIT;

    private void connectZooKeeper() throws InterruptedException {
        try {
            zooConnection = new ZooKeeper(Configuration.getZooKeeperServerList(),
                    Configuration.getZooTimeout(), this);
        } catch (IOException e) {
           log.fatal("Could not connect to Zookeeper Ensemble",e);
           System.exit(1);
        }

        //We are in the process of connecting. Wait it to finish
        connectedSignal.await();
    }


    private ServerState nextState(){
        ServerState newState = null;

        switch(currentState){
            case INIT:
                currentState = State.BACKUP;
                newState = new BackupState(this);
            break;

            case BACKUP:
                currentState = State.PRIMARY;
                newState = new PrimaryState(this);
            break;

            case PRIMARY:
            default:
                //do nothing, null is already the answer
        }

        return newState;
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

    public ZooKeeper getZookeeper() {
        return zooConnection;
    }

    public Data getData() {
        return data;
    }

    public State getCurrentState(){
        return currentState;
    }

    public static void main(String[] args)  {

        try{
            //Connect to Zookeeper
            ServerContext context= new ServerContext();
            context.connectZooKeeper();

            //Ensure we have the operation log
            try{
              (new CreateNodeTransaction(context.getZookeeper(),
                      Configuration.getOpLogZnode(),null,
                      Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)).invoke();
            }catch(KeeperException.NodeExistsException e ){
                //It is okay, node already exist
            }

            //Transition from state to state
            ServerState state = context.nextState();
            while(state != null){
                state.execute();
                state = context.nextState();
            }
        } catch (InterruptedException e){
            log.warn("Server got interrupted",e);
        } catch (KeeperException e ){
            log.fatal("Some problem when interationg with ZooKeeper", e);
            System.exit(1);
        } catch (Throwable t ){
            log.fatal("Some very bad thing happened",t);
            System.exit(1);
        }
    }

}
