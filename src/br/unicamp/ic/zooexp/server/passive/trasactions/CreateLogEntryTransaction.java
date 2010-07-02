package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;

/**
 * Create a persistent and sequential znode under path to log a operation
 * We use the combination <session_id>-<local_seq_number> to uniquely identify the node
 * in case of partial failure.
 *
 */
public class CreateLogEntryTransaction extends Transaction<String> {

    /** We use the session id and a local sequential number to detect partial failure*/
    static volatile AtomicInteger localSeqNumber = new AtomicInteger();
    private String parentNode;
    private byte[] data;
    private boolean firstAttempt;

    /**
     * Created a persistent non-idempotent node with data set under an other znode
     * @param conn ZooKeeper connection
     * @param parentNode The znode under which the new znode will be created
     * @param data the data for the newly created znode
     */
    public CreateLogEntryTransaction(ZooKeeper conn, String parentNode, byte[] data){
        super(conn, conn.getSessionId() + "-" + localSeqNumber.incrementAndGet() + "-");
        this.parentNode = parentNode;
        this.data = data;
        this.firstAttempt = true;
    }


    @Override
    protected void trasactionBody() throws KeeperException,
            InterruptedException {

        //If it is not our first try , check for partial failure
        if(!firstAttempt){
            zooConn.sync(parentNode,null,null); //Ensure we have the latest view
            List<String> children = zooConn.getChildren(parentNode, false);
            for(String child:children){
                if(child.startsWith(path))
                    //Partial failure detected. We've already created the log entry
                    return;
            }

        }

        //Create the sequential node
        result = zooConn.create(parentNode + "/" + path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

        //After leaving the method , we  are no longer at the fist attempt
        firstAttempt = false;
    }


    @Override
    public String toString() {
        return "CreateLogEntryTransaction [data=" + Arrays.toString(data) + ", firstAttempt="
                + firstAttempt + ", parentNode=" + parentNode + ", result="
                + result + ", path=" + path + ", zooConn=" + zooConn + "]";
    }

}
