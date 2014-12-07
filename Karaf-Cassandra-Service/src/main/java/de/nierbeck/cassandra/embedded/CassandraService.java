package de.nierbeck.cassandra.embedded;

public interface CassandraService {

	boolean isRunning();

	void stop();

	void start();

}
