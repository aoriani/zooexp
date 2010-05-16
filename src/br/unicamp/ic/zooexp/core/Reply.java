package br.unicamp.ic.zooexp.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Reply {

    //Types
    public static final int REPLY_FAILURE = -1;
    public static final int REPLY_SUCCESS = 1;
    public static final int REPLY_VALUE = 2;

    private int type;
    private int returnValue = 0;
    
    // disallow instances
    private Reply(int type, int returnValue ){ 
	this.type = type;
	this.returnValue = returnValue;
    }
     
    public static Reply createSuccessReply(){
	return new Reply(REPLY_SUCCESS,0);
    }
    
    public static Reply createFailureReply(){
	return new Reply(REPLY_FAILURE,0);
    }
    
    public static Reply createReturnReply(int returnValue){
	 return new Reply(REPLY_VALUE,returnValue);
    }
    
    
    public void serialize(DataOutputStream out) throws IOException {
	out.writeInt(type);
	out.writeInt(returnValue);
    }
    
    public static Reply parse(DataInputStream in) throws IOException {
	int type = in.readInt();
	int value = in.readInt();
	return new Reply(type,value);
    }
    
    public int getType(){
	return type;
    }
    
    public int getReturnValue(){
	if(type != REPLY_VALUE) 
	    throw new UnsupportedOperationException("Acknowledges do not have return values");
	return returnValue;
    }

    
}
