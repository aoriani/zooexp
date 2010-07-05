package br.unicamp.ic.zooexp.server.passive.trasactions;

import java.util.Arrays;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

/**
 * Resilient {@link ZooKeeper#create}
 */
public class CreateNodeTransaction extends Transaction<String> {

    private byte[] data;
    private List<ACL> acl;
    private CreateMode mode;

    /**
     * <p>Creates a znode</p>
     * <p><b>Important:</b> The creation of znode must be idempotent in the
     * context it is used</p>
     *
     * @param conn the zookeeper connection
     * @param nodePath the path of node being created
     * @param data initial data for node
     * @param acl ACL permissions
     * @param createMod creation mode for node
     */
    public CreateNodeTransaction(ZooKeeper conn,String nodePath,byte[] data, List<ACL> acl, CreateMode createMod){
        super(conn,nodePath);
        this.data = data;
        this.acl = acl ;
        this.mode = createMod;

    }

    @Override
    protected void trasactionBody() throws KeeperException,
            InterruptedException {
        result = zooConn.create(path, data, acl, mode);
    }

    @Override
    public String toString() {
        return "CreateNodeTransaction [acl=" + Arrays.toString(acl.toArray()) + ", data="
                + Arrays.toString(data)
                + ", mode=" + mode + ", path=" + path + ", result=" + result
                + ", zooConn=" + zooConn + "]";
    }



}
