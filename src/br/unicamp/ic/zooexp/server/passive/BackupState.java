package br.unicamp.ic.zooexp.server.passive;

import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

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
import br.unicamp.ic.zooexp.server.passive.utils.OperationZnodeComparator;

public class BackupState implements ServerState, LockListener, Watcher {

    private ServerContext context;

    CountDownLatch latch = new CountDownLatch(1);

    private String lastAppliedOp;

    public BackupState(ServerContext context){
        this.context = context;
    }


    @Override
    public void run() {
        retrieveOperations();
        //Be a candidate
        WriteLock leaderElection = new WriteLock(context.getZookeeper(), Configuration.getServerZnodeGroup(), null, this);
        try {
            leaderElection.lock();
        } catch (KeeperException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        //Wait for your term
        try {
            latch.await();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //ensure we have the lastest state
        retrieveOperations();
    }


    @Override
    public void lockAcquired() {
        latch.countDown();
    }


    @Override
    public void lockReleased() {

    }

    private void retrieveOperations(){
        final ZooKeeper zoo = context.getZookeeper();
        final String znodeOpLog = Configuration.getOpLogZnode();
        final Data data = context.getData();

        try {

            List<String> ops = zoo.getChildren(Configuration.getOpLogZnode(),
                    false);
            TreeSet<String> opSet = new TreeSet<String>(
                    new OperationZnodeComparator());
            opSet.addAll(ops);
            NavigableSet<String> notYetAppliedOps = lastAppliedOp == null ? opSet
                    : opSet.tailSet(lastAppliedOp, false);
            for (String opZnode : notYetAppliedOps) {
                byte[] opData = zoo.getData(znodeOpLog + "/" + opZnode, false,
                        null);
                Operation operation = new Operation();
                operation.fromByteArray(opData);
                data.executeOperation(operation);
                lastAppliedOp = opZnode;
            }
            //set watcher if we are no yet the leader
            if (latch.getCount() == 1)
                zoo.getChildren(znodeOpLog, this);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    @Override
    public void process(WatchedEvent event) {
        if( event.getType() == EventType.NodeChildrenChanged){
            retrieveOperations();
        }

    }

}
