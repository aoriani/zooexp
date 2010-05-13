package br.unicamp.ic.zooexp.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import br.unicamp.ic.zooexp.Operation;

public class OperationTest {

    @Test
    public void testParse() throws IOException {
	
	//The value to modified by operations
	int value = 0;
	
	Operation set15  = new Operation(Operation.SET_OP,15);
	Operation add32  = new Operation(Operation.ADD_OP,32);
	Operation readop = new Operation(Operation.READ_OP,3);
	Operation sub3   = new Operation(Operation.SUB_OP,3);
	
	// write  to log 
	ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(bufferOut);
	
	set15.serialize(out);
	add32.serialize(out);
	readop.serialize(out);
	sub3.serialize(out);
	
	out.flush();
	out.close();
	
	// retrieve log
	byte[] log = bufferOut.toByteArray();
	
	// read from log
	ByteArrayInputStream bufferInput = new ByteArrayInputStream(log);
	DataInputStream in = new DataInputStream(bufferInput);
	
	Operation currentOp  = Operation.parse(in);
	value = currentOp.apply(value);
	assertEquals("Current operation shall set value to 15",15,value);
	
	currentOp  = Operation.parse(in);
	value = currentOp.apply(value);
	assertEquals("Current operation shall set value to 47",47,value);
	
	currentOp  = Operation.parse(in);
	value = currentOp.apply(value);
	assertEquals("Current operation shall modify value",47,value);
	
	currentOp  = Operation.parse(in);
	value = currentOp.apply(value);
	assertEquals("Current operation shall set value to 44",44,value);
	
	assertTrue("We shall reach the end of log", in.available() == 0);
	
	in.close();
	
    }

}
