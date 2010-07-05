package br.unicamp.ic.zooexp.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import mockit.Mock;
import mockit.MockClass;
import mockit.UsingMocksAndStubs;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import br.unicamp.ic.zooexp.tests.TransactionTest.MockedRandom;
import br.unicamp.ic.zooexp.core.trasactions.Transaction;

@UsingMocksAndStubs( { MockedRandom.class })
public class TransactionTest {

    private class MockedTransaction<T> extends Transaction<T>{

        private String description;
        private KeeperException exception;
        private int executionCount;


        MockedTransaction(String description, KeeperException exceptionToBeThown){
            super(null,null);
            this.description = description;
            this.exception = exceptionToBeThown;
            this.executionCount = 0;
        }

        @Override
        protected void trasactionBody() throws KeeperException,
                InterruptedException {
            ++executionCount;
            if(exception != null)
                throw exception;

        }

        @Override
        public String toString() {
            return description;
        }

        public int getExecutionCount(){
            return executionCount;
        }

        public void executeTransaction() throws KeeperException, InterruptedException{
            execute();
        }

    }

    @MockClass(realClass = Random.class, stubs = "<clinit>")
    public static class MockedRandom{

        @Mock
        public int nextInt(int n){
            //we want the worst case delay
            return (n-1);
        }
    }

    @Test
    public void testExecuteSucess() throws KeeperException, InterruptedException {
        MockedTransaction t = new MockedTransaction("testing successful transaction",null);
        t.executeTransaction();
        assertTrue("Expected just one attempt",t.getExecutionCount() == 1);

    }

    @Test (expected = KeeperException.SessionExpiredException.class)
    public void testExecuteSessionExpired() throws KeeperException, InterruptedException{
        KeeperException exception = new KeeperException.SessionExpiredException();
        MockedTransaction t = new MockedTransaction("testing transaction with expired connection",exception);
        try {
            t.executeTransaction();
        } catch (KeeperException.SessionExpiredException e) {
            assertTrue("Expected just one attempt",t.getExecutionCount() == 1);
            throw e;
        }
        fail("KeeperException.SessionExpiredException expected");

    }

    @Test (expected = KeeperException.ConnectionLossException.class, timeout = 1000000)
    public void testExecuteConnectionLost() throws KeeperException, InterruptedException{
        KeeperException exception = new KeeperException.ConnectionLossException();
        MockedTransaction t = new MockedTransaction("testing transaction with connection loss",exception);

        try{
            t.executeTransaction();
        }catch (KeeperException.ConnectionLossException e) {
            assertTrue("Expected just one attempt",t.getExecutionCount() == 10);
            throw e;
        }
        fail("KeeperException.ConnectionLossException expected");

    }

}
