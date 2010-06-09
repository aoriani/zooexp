package br.unicamp.ic.zooexp.tests;

import static org.junit.Assert.*;
import org.junit.Test;

import br.unicamp.ic.zooexp.server.passive.utils.OperationZnodeComparator;


public class OperationZnodeComparatorTest {

    @Test
    public void testComparator(){
        OperationZnodeComparator comp = new OperationZnodeComparator();
        assertTrue("The znodes should be equal", comp.compare("/a/b-sessionid-0001", "/a/c-sessionid2-001") == 0);
        assertTrue("The znodes should be in correct order", comp.compare("/a/b-sessionid-0001", "/a/c-sessionid2-21") < 0);
        assertTrue("The znodes should be in inverse order", comp.compare("/a/b-sessionid-101", "/a/c-sessionid2-0021") > 0);

        try{
            comp.compare("/a/b-session123", "/a/c-session4");
            fail("Invalid znode should throw exception");
        }
        catch(Exception e){
            //Do nothing test succeed
        }
    }

}
