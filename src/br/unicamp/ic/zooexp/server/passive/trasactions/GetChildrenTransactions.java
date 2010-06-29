package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Resilient {@link ZooKeeper#getChildren}
 */
public class GetChildrenTransactions extends Transaction {

    private Watcher watcher;
    private List<String> result;

    /**
     * @param conn ZooKeeper connection
     * @param nodePath the path to znode you want to get data
     * @param watcherZnode a watcher to be set on the znode
     */
    public GetChildrenTransactions(ZooKeeper conn, String nodePath, Watcher watcherZnode){
        super(conn,nodePath);
        watcher = watcherZnode;
    }

    @Override
    public String toString() {
        return "GetChildrenTransactions [result=" + result + ", watcher=" + watcher
                + ", path=" + path + ", zooConn=" + zooConn + "]";
    }

    @Override
    protected void trasactionBody() throws KeeperException,
            InterruptedException {
        //Ensure we have the latest list
        zooConn.sync(path, null, null);
        result = zooConn.getChildren(path, watcher);
    }

    /**
     * Gets the children list for node
     * @return a list of children for the selected node
     * @throws KeeperException
     * @throws InterruptedException
     */
    List<String> getChildren() throws KeeperException, InterruptedException{
        execute();
        return result;
    }

}
