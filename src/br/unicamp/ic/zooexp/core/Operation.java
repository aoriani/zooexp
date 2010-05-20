package br.unicamp.ic.zooexp.core;


public class Operation extends Marshallable{
    // Operations
    public static final byte INVALID_OP = -1;
    public static final byte SET_OP = 1;
    public static final byte ADD_OP = 2;
    public static final byte SUB_OP = 3;
    public static final byte READ_OP = 4;
    
   
    private byte type;
    private int arg;

    public Operation(){
	this(INVALID_OP,0);
    }
    
    public Operation(byte type, int argument) {
	this.type = type;
	this.arg = argument;
    }
    
    public byte getType(){
	return type;
    }
    
    public int getArg(){
	if(type == READ_OP || type == INVALID_OP)
	    throw new UnsupportedOperationException("Operation do not take arguments");
	
	return arg;
    }
    
    @Override
    protected int sizeof() {
	return 5;
    }

    
    public byte[] toByteArray(){
	return new byte[] { type,
		            (byte) (arg >>> 24),
		            (byte) (arg >>> 16),
		            (byte) (arg >>> 8),
		            (byte) arg 
		            };
	
    }
    
    public void fromByteArray(byte [] bytes){
	if (bytes.length != sizeof()) 
	    throw new IllegalArgumentException("byte array shall have size " + sizeof());
	type = bytes[0];
	arg = ((bytes[1] & 0xff) << 24) |
		((bytes[2] & 0xff) << 16) |
		((bytes[3] & 0xff) << 8) |
		(bytes[4] & 0xff);
    }

 
    public int apply(int currentValue) {

	int result = currentValue;

	// Polymorphism
	switch (type) {
	case SET_OP:
	    result = arg;
	    break;
	case ADD_OP:
	    result += arg;
	    break;
	case SUB_OP:
	    result -= arg;
	    break;
	case READ_OP:
	default:
	    // do nothing
	}
	return result;
    }

}
