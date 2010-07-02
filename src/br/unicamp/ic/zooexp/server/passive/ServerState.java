/**
 *
 */
package br.unicamp.ic.zooexp.server.passive;


/**
 * Basic Interface for implementing the states the server can be
 *
 */
public interface ServerState {

    /**
     * <p>Runs the state</p>
     * <p><b>Note:</b> It is advisable to catch and log the exception inside the method then
     * throw the exception again, since Throwable is too much generic to provide
     * details of what happened</p>
     * @throws Throwable because it may run any statements any exception may be throw
     */
    public void execute() throws Throwable;
}
