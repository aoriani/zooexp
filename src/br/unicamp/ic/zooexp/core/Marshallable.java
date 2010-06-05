package br.unicamp.ic.zooexp.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Base class for all that shall be packed to be transfered over the net
 * 
 */
public abstract class Marshallable {

    /**
     * This method is used by operations that need to know the struct size
     * 
     * @see #parse(InputStream)
     * @return the numbers of necessary bytes to serialize the struct
     */
    protected abstract int sizeof();

    /**
     * Serializes the struct to a byte array
     * 
     * @return array the bytes that represent the struct
     */
    public abstract byte[] toByteArray();

    /**
     * Reconstruct the struct from a byte array
     * 
     * @param bytes
     *            the array of bytes that carry a representation of struct
     */
    public abstract void fromByteArray(byte[] bytes);

    /**
     * Write the struct to a stream
     * 
     * @param out
     *            the stream to be written to
     * @throws IOException
     *             in case of network error
     */
    public void serialize(OutputStream out) throws IOException {
	byte[] bytes = toByteArray();
	out.write(bytes);
    }

    /**
     * Reads the struct from a stream
     * 
     * @param in
     *            the stream to be read from
     * @throws EOFException
     *             if stream does not have enough data to read the struct
     * @throws IOException
     *             if network error happens
     */
    public void parse(InputStream in) throws EOFException, IOException {
	byte[] bytes = new byte[sizeof()];
	if (in.read(bytes) < sizeof())
	    throw new EOFException("Could read Operation from stream");
	fromByteArray(bytes);
    }
}
