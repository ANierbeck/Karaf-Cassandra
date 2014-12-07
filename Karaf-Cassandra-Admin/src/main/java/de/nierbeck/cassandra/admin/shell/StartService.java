package de.nierbeck.cassandra.admin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.nierbeck.cassandra.embedded.CassandraService;

@Command(scope = "cassandra-admin", name = "start", description = "Connect to cassandra server")
@Service
public class StartService implements Action {

	@Reference
	CassandraService cassandraServer;

	@Override
	public Object execute() throws Exception {
		if (cassandraServer.isRunning()) {
			System.err.println("Embedded Cassandra is already started");
			return null;
		}

		cassandraServer.start();
		System.out.println("Embedded Cassandra started.");
		return null;
	}

}
