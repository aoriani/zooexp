package br.unicamp.ic.zooexp.server;

import br.unicamp.ic.zooexp.Operation;
import br.unicamp.ic.zooexp.Reply;

/**
 * Class responsible to execute the operations on the shared data.
 * All methods are synchronized in the hope that a single thread
 * access will yield a sequential consistency.
 * 
 *  This class is a singleton 
 *
 */
public class Data {
    
    /** The single instance */
    volatile static Data instance;
    
    /** The data to be manipulated */
    volatile int data;
    
    //Hide constructor
    protected Data(){
	data = 0;
    }
    
    public static Data getInstance(){
	//Double check pattern for singleton is fixed on JDK5
	if(instance == null ){
	    synchronized(Data.class){
		if(instance == null){
		    instance = new Data();
		}
	    }
	}
	
	return instance;
    }
    
    
    public synchronized Reply executeOperation(Operation op){
	switch(op.getType()){
	case Operation.READ_OP:
	    return Reply.createReturnReply(data);
	
	case Operation.ADD_OP:
	case Operation.SUB_OP:
	case Operation.SET_OP:
	    data = op.apply(data);
	    return Reply.createSuccessReply();
	default:
	    return Reply.createFailureReply();
	}
    }
    
    
}
