package br.unicamp.ic.zooexp.core;


public class Reply extends Marshallable{

    //Types
    public static final byte REPLY_FAILURE = -1;
    public static final byte REPLY_SUCCESS = 1;
    public static final byte REPLY_VALUE = 2;

    private byte type;
    private int returnValue = 0;
    
    
    public Reply(){
	this(REPLY_FAILURE,0);
    }
    
    private Reply(byte type, int returnValue ){ 
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

    
    public byte getType(){
	return type;
    }
    
    public int getReturnValue(){
	if(type != REPLY_VALUE ) 
	    throw new UnsupportedOperationException("Acknowledges do not have return values");
	return returnValue;
    }

    @Override
    protected int sizeof() {
	return 5;
    }
    
    @Override
    public void fromByteArray(byte[] bytes) {
	if (bytes.length != sizeof()) 
	    throw new IllegalArgumentException("byte array shall have size " + sizeof());
	type = bytes[0];
	returnValue = ((bytes[1] & 0xff) << 24) |
        	      ((bytes[2] & 0xff) << 16) |
        	      ((bytes[3] & 0xff) << 8) |
        	      (bytes[4] & 0xff);
	
    }

 

    @Override
    public byte[] toByteArray() {
	return new byte[] { type,
	            (byte) (returnValue >>> 24),
	            (byte) (returnValue >>> 16),
	            (byte) (returnValue >>> 8),
	            (byte) returnValue 
	            };
    }

    
}
