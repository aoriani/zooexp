package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.Arrays;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * Resilient {@link ZooKeeper#setData}
 */
public class SetDataTransaction extends Transaction <Stat>{

    private byte[] data;
    private int version;

    /**
     *
     * @param conn conn ZooKeeper connection
     * @param path the path to znode you want to set data
     * @param data the data for the  znode
     * @param version the version of znode to set like in {@link ZooKeeper#setData}
     */
    public SetDataTransaction(ZooKeeper conn, String path, byte[] data, int version){
        super(conn,path);
        this.data = data;
        this.version = version;
    }


    @Override
    public String toString() {
        return "SetDataTransaction [data=" + Arrays.toString(data) + ", result=" + result
                + ", version=" + version + ", path=" + path + ", zooConn="
                + zooConn + "]";
    }



    @Override
    protected void trasactionBody() throws KeeperException,
            InterruptedException {
        result = zooConn.setData(path, data, version);
    }

}
