package de.nierbeck.cassandra.embedded;

/**
 * This service interface is for communicating with the embedded Cassandra
 * instance. This instance is registered as service.
 * 
 * @author anierbeck
 *
 */
public interface CassandraService {

	/**
	 * @return - true or false depending on the state of the embedded Cassandra
	 *         instance.
	 */
	boolean isRunning();
	
	/**
	 * @return - the port the embedded Cassandra is using. 
	 */
	Integer getPort();

	/**
	 * stops the embedded Cassandra instance if running.
	 */
	void stop();

	/**
	 * starts the embedded Cassndra instance if it isn't running.
	 */
	void start();

	/**
	 * cleans the keyspaces available to the embedded instance. Beware data is
	 * not available after cleaning the keyspaces.
	 */
	void cleanUp();

}
