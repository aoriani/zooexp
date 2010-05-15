package br.unicamp.ic.zooexp.tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import mockit.Mock;
import mockit.MockClass;
import mockit.UsingMocksAndStubs;

import org.junit.Test;

import br.unicamp.ic.zooexp.Operation;
import br.unicamp.ic.zooexp.Reply;
import br.unicamp.ic.zooexp.server.Data;
import br.unicamp.ic.zooexp.server.WorkerThread;
import br.unicamp.ic.zooexp.tests.WorkerThreadTest.*;


@UsingMocksAndStubs({MockedSocket.class})
public class WorkerThreadTest {

    /**
     * A mocked Socket class where is possible to replace the 
     * stream to simulate a TCP connection
     *
     */
    @MockClass(realClass = Socket.class, stubs = "<clinit>")
    public static final class MockedSocket{
	static InputStream input;
	static OutputStream output;
	
	static void setInput(InputStream in ){input = in;}
	static void setOutput(OutputStream out ){output = out;}
	
	@Mock
	public InputStream getInputStream() throws IOException{
	    return input;
	}
	
	@Mock
	public OutputStream getOutputStream() throws IOException{
	    return output;
	    
	}
	
    }
    
    
    private InputStream prepareInputStreamFortestRun() throws IOException{
	
	//Write operations to array
	ByteArrayOutputStream record = new ByteArrayOutputStream();
	DataOutputStream encapsulatedRecord = new DataOutputStream(record);
	
	Operation set15   = new Operation(Operation.SET_OP,15);
	Operation add32   = new Operation(Operation.ADD_OP,32);
	Operation readop  = new Operation(Operation.READ_OP,3);
	Operation sub3    = new Operation(Operation.SUB_OP,3);
	Operation invalid = new Operation(-1,0);
	
	set15.serialize(encapsulatedRecord);
	add32.serialize(encapsulatedRecord);
	readop.serialize(encapsulatedRecord);
	sub3.serialize(encapsulatedRecord);
	invalid.serialize(encapsulatedRecord);
	
	encapsulatedRecord.flush();
	encapsulatedRecord.close();
	
	byte[] input = record.toByteArray();
	
	//create the input stream
	return new ByteArrayInputStream(input);
    }
    
    
    @Test(timeout=10000)
    public void testRun() throws IOException {
	
	//Setup Socket Mock
	ByteArrayOutputStream serverOutput = new ByteArrayOutputStream();
	MockedSocket.setOutput(serverOutput);
	MockedSocket.setInput(prepareInputStreamFortestRun());
	Socket socket = new Socket();
	
	//Run work thread in current thread to make testing easier
	Data data = Data.getInstance();
	WorkerThread workThread = new WorkerThread(socket, data);
	workThread.run();
	
	//verify Data state
	assertEquals("After all operations data shall be 44",44,data.getValue());
	
	//verify server replies
	DataInputStream serverReplies = new DataInputStream(new ByteArrayInputStream(serverOutput.toByteArray()));
	
	Reply reply = Reply.parse(serverReplies);
	assertTrue("Expected SUCCESS for OP SET 15", reply.getType() == Reply.REPLY_SUCCESS);
	
	reply = Reply.parse(serverReplies);
	assertTrue("Expected SUCCESS for OP ADD 32", reply.getType() == Reply.REPLY_SUCCESS);
	
	reply = Reply.parse(serverReplies);
	assertTrue("Expected VALUE for READ OP", reply.getType() == Reply.REPLY_VALUE);
	assertEquals("Expected a return value of 47",47, reply.getReturnValue());
	
	reply = Reply.parse(serverReplies);
	assertTrue("Expected SUCCESS for OP SUB 3", reply.getType() == Reply.REPLY_SUCCESS);

	reply = Reply.parse(serverReplies);
	assertTrue("Expected FAILURE for INVALID OP", reply.getType() == Reply.REPLY_FAILURE);
	
	assertEquals("No more data from server was expected",0,serverReplies.available());
	
    }

}
