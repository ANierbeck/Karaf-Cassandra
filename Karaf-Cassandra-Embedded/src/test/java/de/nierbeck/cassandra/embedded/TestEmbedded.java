package de.nierbeck.cassandra.embedded;

import java.io.IOException;

import de.nierbeck.cassandra.embedded.impl.OsgiEmbeddedCassandra;

public class TestEmbedded {

	private static OsgiEmbeddedCassandra cassandra;

	public static void main(String[] args) throws IOException {
		cassandra = new OsgiEmbeddedCassandra();
		cassandra.start();

		System.out.println("started");

		cassandra.stop();

		System.out.println("stopped");

		System.out.println("going for restaret");

		cassandra.start();

		System.out.println("restarted");

		System.exit(1);

	}

}
