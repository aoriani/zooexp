package br.unicamp.ic.zooexp.server.passive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.recipes.lock.LockListener;
import org.apache.zookeeper.recipes.lock.WriteLock;

import br.unicamp.ic.zooexp.core.Configuration;
import br.unicamp.ic.zooexp.core.Operation;
import br.unicamp.ic.zooexp.core.server.Data;
import br.unicamp.ic.zooexp.server.passive.ServerContext.State;
import br.unicamp.ic.zooexp.server.passive.trasactions.GetChildrenTransaction;
import br.unicamp.ic.zooexp.server.passive.trasactions.GetDataTransaction;
import br.unicamp.ic.zooexp.server.passive.trasactions.SetDataTransaction;
import br.unicamp.ic.zooexp.server.passive.utils.OperationZnodeComparator;

/**
 * <p><b>The Backup State</b><p>
 * <p>
 * This is the state responsible for :
 * <ul>
 * <li> catch up the current state by retrieving the operations from log</li>
 * <li> put the server in the ZooKeeper group so he can be a candidate for primary node</li>
 * <li> listen to updates on operation log (a ordered set of znodes) and update its own state</li>
 * <li> ensure it has the latest state before changing to PRIMARY state </li>
 * </ul>
 * </p>
 *
 */
public class BackupState implements ServerState, LockListener, Watcher {

    /** Logger */
    private static final Logger log = Logger.getLogger(ServerContext.class);


    final private ServerContext context;
    final private ZooKeeper zoo;
    private String lastAppliedOp;
    private volatile Semaphore waitForEvent = new Semaphore(0);
    private volatile boolean iamLeader = false;


    public BackupState(ServerContext context){
        this.context = context;
        this.zoo = context.getZookeeper();
    }


    @Override
    public void execute() throws Throwable{

        log.info("Entering BACKUP state");

        try {
            //Be a candidate
            WriteLock election  = new WriteLock(zoo, Configuration.getServerZnodeGroup(), null, this);
            election.lock();

            //Keep listening for updates
            while(!iamLeader){
                retrieveOperations(true);
                waitForEvent.acquire();
            }

            //We are the leader now. Update znode so clients can found us
            String ourAddress = InetAddress.getLocalHost().getHostAddress();
                       ourAddress += ":" + Configuration.getServerPort();
            (new SetDataTransaction(zoo,Configuration.getServerZnodeGroup(),
                    ourAddress.getBytes("UTF-8"),-1)).invoke();


            //Ensure that we have the latest state before moving on
            retrieveOperations(false);
        } catch (UnknownHostException e){
            log.fatal("Could resolve the ip address for this server, giving up ...",e);
            throw e;
        } catch (KeeperException e) {
            log.fatal("Some error occurred while in BACKUP state",e);
            throw e;
        }

    }

    private void retrieveOperations(boolean setWatcher) throws KeeperException, InterruptedException {

        //Sanity check: ensure we are in the correct state.
        //As we cannot uninstall watchers, they may be called on other states
        if(context.getCurrentState() != State.BACKUP) {
            log.warn("Trying  to retrieve operations on wrong state :" + context.getCurrentState());
            return ;
        }

        final String znodeOpLog = Configuration.getOpLogZnode();
        final Data data = context.getData();

        //Retrieve operation
        List<String> ops = (new GetChildrenTransaction(zoo,Configuration.getOpLogZnode(),
                setWatcher?this:null)).invoke();

        //Order operations
        TreeSet<String> opSet = new TreeSet<String>( new OperationZnodeComparator());
        opSet.addAll(ops);

        //Discover pending operations
        NavigableSet<String> notYetAppliedOps = lastAppliedOp == null ? opSet
                : opSet.tailSet(lastAppliedOp, false);

        //Apply pending operations
        for (String opZnode : notYetAppliedOps) {
            byte[] opData = (new GetDataTransaction(zoo,znodeOpLog + "/" + opZnode)).invoke();
            Operation operation = new Operation();
            operation.fromByteArray(opData);
            data.executeOperation(operation);
            lastAppliedOp = opZnode;
        }
    }


    /**
     * Process updates of operations
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    @Override
    public void process(WatchedEvent event) {
        if( event.getType() == EventType.NodeChildrenChanged){
            log.info("Update received");
            waitForEvent.release();
        }
    }



    /**
     * Process leadership acquisition
     * @see org.apache.zookeeper.recipes.lock.LockListener#lockAcquired()
     */
    @Override
    public void lockAcquired() {
        log.info("Leadership acquired");
        iamLeader = true;
        waitForEvent.release();

    }


    @Override
    public void lockReleased() {
        //do nothing

    }


}
