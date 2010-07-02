package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Resilient {@link ZooKeeper#getChildren}
 */
public class GetChildrenTransaction extends Transaction <List<String>>{

    private Watcher watcher;

    /**
     * @param conn ZooKeeper connection
     * @param nodePath the path to znode you want to get data
     * @param watcherZnode a watcher to be set on the znode
     */
    public GetChildrenTransaction(ZooKeeper conn, String nodePath, Watcher watcherZnode){
        super(conn,nodePath);
        watcher = watcherZnode;
    }

    @Override
    public String toString() {
        return "GetChildrenTransaction [result=" + result + ", watcher=" + watcher
                + ", path=" + path + ", zooConn=" + zooConn + "]";
    }

    @Override
    protected void trasactionBody() throws KeeperException,
            InterruptedException {
        //Ensure we have the latest list
        zooConn.sync(path, null, null);
        result = zooConn.getChildren(path, watcher);
    }

}
