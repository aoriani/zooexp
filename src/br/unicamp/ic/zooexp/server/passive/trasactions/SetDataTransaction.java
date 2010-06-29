package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.Arrays;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * Resilient {@link ZooKeeper#setData}
 */
public class SetDataTransaction extends Transaction {

    private byte[] data;
    private int version;
    private Stat result;

    /**
     * @param conn conn ZooKeeper connection
     * @param path the path to znode you want to set data
     * @param data the data for the  znode
     * @param version the version of znode to set like in {@link ZooKeeper#setData}
     */
    public SetDataTransaction(ZooKeeper conn, String path, byte[] data, int version){
        super(conn,path);
        this.data = Arrays.copyOf(data, data.length);
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


    public Stat setData() throws KeeperException, InterruptedException{
        execute();
        return result;
    }

}
