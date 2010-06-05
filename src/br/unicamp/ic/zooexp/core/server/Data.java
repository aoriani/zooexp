package br.unicamp.ic.zooexp.core.server;

import org.apache.log4j.Logger;

import br.unicamp.ic.zooexp.core.Operation;
import br.unicamp.ic.zooexp.core.Reply;

/**
 * Class responsible to execute the operations on the shared data. All methods
 * are synchronized in the hope that a single thread access will yield a
 * sequential consistency.
 * 
 * This class is a singleton
 * 
 */
public class Data {
    /** Logger */
    private static final Logger log = Logger.getLogger(Data.class);

    /** The data to be manipulated */
    private volatile int data;

    /** The data id */
    private final String id;

    public Data(String id) {
	this.id = id;
	this.data = 0;
    }

    public Data() {
	this("Data-TEST");
    }

    public synchronized Reply executeOperation(Operation op) {
	switch (op.getType()) {
	case Operation.READ_OP:
	    log.info(id + ":Executed OP:READ => DATA:" + data);
	    return Reply.createReturnReply(data);

	case Operation.ADD_OP:
	case Operation.SUB_OP:
	case Operation.SET_OP:
	    data = op.apply(data);
	    log.info(id + ":Executed OP:" + op.getType() + " ARG:"
		    + op.getArg() + " => DATA:" + data);
	    return Reply.createSuccessReply();
	default:
	    log.warn(id + ":Executed OP:INVALID");
	    return Reply.createFailureReply();
	}

    }

    public synchronized int getValue() {
	return data;
    }

}
