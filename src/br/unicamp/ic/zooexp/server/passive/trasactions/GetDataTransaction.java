package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.Arrays;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;


/**
 * Resilient {@link ZooKeeper#getData}
 */
public final class GetDataTransaction extends Transaction<byte[]> {

    /**
     * @param conn ZooKeeper connection
     * @param nodePath the path to znode you want to get data
     */
    public GetDataTransaction(ZooKeeper conn,String nodePath){
        super(conn,nodePath);
    }



    @Override
    public String toString() {
        return "GetDataTransaction [result=" + Arrays.toString(result) + ", path=" + path
                + ", zooConn=" + zooConn + "]";
    }

    @Override
    protected void trasactionBody() throws KeeperException, InterruptedException{
        result = zooConn.getData(path, null, null);
    }
}
