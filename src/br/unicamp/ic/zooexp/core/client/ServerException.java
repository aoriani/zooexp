package br.unicamp.ic.zooexp.core.client;

/**
 * Exception when server return failure for a operation
 * 
 */
public class ServerException extends Exception {

    private static final long serialVersionUID = 1L;

    public ServerException() {
	super();
    }

    public ServerException(String msg) {
	super(msg);
    }

}
