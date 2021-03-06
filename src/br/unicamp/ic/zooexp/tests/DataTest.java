package br.unicamp.ic.zooexp.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import br.unicamp.ic.zooexp.core.Operation;
import br.unicamp.ic.zooexp.core.Reply;
import br.unicamp.ic.zooexp.core.server.Data;

public class DataTest {

    Operation readOp = new Operation(Operation.READ_OP, 0);

    @Test
    public void testExecuteOperation() {
	Data data = new Data();

	// Read Data to ensure it is initialized correct
	Reply reply = data.executeOperation(readOp);
	assertTrue("A value is expected as reply ",
		reply.getType() == Reply.REPLY_VALUE);
	assertEquals("Data should be initialized as zero", 0, reply
		.getReturnValue());

	// Set it to 34
	Operation set34 = new Operation(Operation.SET_OP, 34);
	reply = data.executeOperation(set34);
	assertTrue("Expecting success when setting to 34",
		reply.getType() == Reply.REPLY_SUCCESS);
	reply = data.executeOperation(readOp);
	assertEquals("Data shall be 34 now", 34, reply.getReturnValue());

	// add 23
	Operation add23 = new Operation(Operation.ADD_OP, 23);
	reply = data.executeOperation(add23);
	assertTrue("Expecting success when adding 23",
		reply.getType() == Reply.REPLY_SUCCESS);
	reply = data.executeOperation(readOp);
	assertEquals("Data shall be 57 now", 57, reply.getReturnValue());

	// subtracting 7
	Operation sub7 = new Operation(Operation.SUB_OP, 7);
	reply = data.executeOperation(sub7);
	assertTrue("Expecting success when subtracting 7",
		reply.getType() == Reply.REPLY_SUCCESS);
	reply = data.executeOperation(readOp);
	assertEquals("Data shall be 50 now", 50, reply.getReturnValue());

	// Invalid Operation
	Operation invalid = new Operation(Operation.INVALID_OP, 0);
	reply = data.executeOperation(invalid);
	assertTrue("Expecting failure with invalid operation",
		reply.getType() == Reply.REPLY_FAILURE);
    }

}
