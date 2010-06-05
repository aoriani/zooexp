package br.unicamp.ic.zooexp.tests;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

/**
 * Why am I doing the conversion int to byte[] in the most stupid way? Creating
 * two objects if I can play around with the bits
 * 
 * 
 */

public class ByteArray2Int {

    byte[] int2byteArray(int i) {
	// People told me that byte casting takes care of masking
	return new byte[] { (byte) (i >>> 24), (byte) (i >>> 16),
		(byte) (i >>> 8), (byte) (i) };
    }

    int byteArray2int(byte[] b) {
	return ((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16)
		| ((b[2] & 0xff) << 8) | (b[3] & 0xff);
    }

    @Test
    public void testFixedValues() {
	byte[] bytes = int2byteArray(Integer.MIN_VALUE);
	int integer = byteArray2int(bytes);
	assertEquals("Testing lower int bound", Integer.MIN_VALUE, integer);

	bytes = int2byteArray(Integer.MAX_VALUE);
	integer = byteArray2int(bytes);
	assertEquals("Testing upper int bound", Integer.MAX_VALUE, integer);

	bytes = int2byteArray(-1);
	integer = byteArray2int(bytes);
	assertEquals("Testing upper -1", -1, integer);

	bytes = int2byteArray(0);
	integer = byteArray2int(bytes);
	assertEquals("Testing upper 0", 0, integer);

    }

    /**
     * A stress test to ensure the correction of conversion
     */
    @Test(timeout = 10000)
    public void testRandomValues() {
	Random randGenerator = new Random(System.currentTimeMillis());

	for (int i = 0; i < 100000; i++) {
	    int testValue = randGenerator.nextInt();
	    byte[] bytes = int2byteArray(testValue);
	    int resultValue = byteArray2int(bytes);
	    assertEquals("Testing for " + testValue, testValue, resultValue);
	}

    }

}
