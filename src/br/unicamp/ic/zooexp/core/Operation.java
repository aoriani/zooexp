package br.unicamp.ic.zooexp.core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Operation {
    // Operations
    public static final int SET_OP = 1;
    public static final int ADD_OP = 2;
    public static final int SUB_OP = 3;
    public static final int READ_OP = 4;

    private int type;
    private int arg;

    public Operation(int type, int argument) {
	this.type = type;
	this.arg = argument;
    }
    
    public int getType(){
	return type;
    }
    
    public int getArg(){
	if(type == READ_OP)
	    throw new UnsupportedOperationException("Read operation do not take arguments");
	
	return arg;
    }

    public void serialize(DataOutputStream out) throws IOException {
	out.writeInt(type);
	out.writeInt(arg);
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

    public static Operation parse(DataInputStream in) throws IOException {
	int type = in.readInt();
	int arg = in.readInt();
	return new Operation(type, arg);
    }

}
