package br.unicamp.ic.zooexp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Reply {

    //Types
    private static final int REPLY_ACK = 1;
    private static final int REPLY_VALUE = 2;

    private int type;
    private int returnValue = 0;
    
    // disallow instances
    private Reply(int type, int returnValue ){ 
	this.type = type;
	this.returnValue = returnValue;
    }
     
    public static Reply createAck(){
	return new Reply(REPLY_ACK,0);
    }
    
    public static Reply createReturn(int returnValue){
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
    
    public int returnValue(){
	if(type != REPLY_ACK) 
	    throw new UnsupportedOperationException("Acknowledges do not have return values");
	return returnValue;
    }

    
}
